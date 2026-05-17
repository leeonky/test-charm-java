package org.testcharm.cucumber.swarm;

import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.messages.types.StepDefinition;
import io.cucumber.plugin.event.TestCase;
import org.testcharm.cucumber.swarm.repo.Repository;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DataMapper {
    private final Repository<String, Pickle> pickleRepository = new Repository<>(this::pickleKey);
    private final Repository<String, Feature> featureRepository = new Repository<>(this::featureKey);
    private final List<Path> featurePaths;

    public DataMapper(List<URI> featurePaths) {
        this.featurePaths = featurePaths.stream().filter(Objects::nonNull)
                .map(Paths::get).map(Path::normalize).collect(Collectors.toList());
    }

    private String relativeUri(URI fileUri) {
        Path file = Paths.get(fileUri).normalize();
        return featurePaths.stream().filter(file::equals).findFirst().map(path -> path.getFileName().toString())
                .orElseGet(() -> featurePaths.stream()
                        .filter(dir -> file.startsWith(dir) && !file.equals(dir))
                        .max(Comparator.comparingInt(Path::getNameCount))
                        .map(dir -> dir.relativize(file).toString())
                        .orElseThrow(() -> new IllegalArgumentException("Cannot relativize file: " + fileUri + " from " + featurePaths)));
    }

    public String pickleKey(Pickle pickle) {
        return relativeUri(pickle.getUri()) + ":" + pickle.getLocation().getLine();
    }

    public String featureKey(Feature feature) {
        return relativeUri(feature.getUri());
    }

    public void mapGherkinFeature(Feature feature) {
        featureRepository.save(feature);
        feature.getPickles().forEach(this::mapGherkinPickle);
    }

    public void mapGherkinPickle(Pickle instance) {
        pickleRepository.save(instance);
    }

    public Pickle pickle(String key) {
        return pickleRepository.findByKey(key);
    }

    public Collection<Pickle> pickles() {
        return pickleRepository.findAll();
    }

    public String testCaseKey(TestCase testCase) {
        return relativeUri(testCase.getUri()) + ":" + testCase.getLocation().getLine();
    }

    public Pickle pickleById(String pickleId) {
        return pickleRepository.findBy(p -> p.getId().equals(pickleId)).get();
    }

    public String stepDefinitionKey(StepDefinition stepDefinition) {
        return stepDefinition.getPattern().toString() + stepDefinition.getSourceReference().toString();
    }
}
