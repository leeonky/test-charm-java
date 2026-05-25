package org.testcharm.cucumber.swarm;

import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.messages.types.Hook;
import io.cucumber.messages.types.StepDefinition;
import io.cucumber.plugin.event.TestCase;
import org.testcharm.cucumber.swarm.repo.Repository;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

public class DataMapper {
    private final Repository<String, Pickle> pickleRepository = new Repository<>(this::pickleKey);
    private final Repository<String, Feature> featureRepository = new Repository<>(this::featureKey);
    private final Path workingDir;

    public DataMapper(Path workingDir) {
        this.workingDir = workingDir;
    }

    private String relativeUri(URI fileUri) {
        Path file = Paths.get(fileUri).normalize();
        return workingDir.relativize(file).toString();
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

    public String hookKey(Hook hook) {
        return hook.getName().orElse(null) + hook.getSourceReference().toString() + hook.getTagExpression().orElse(null) + hook.getType().orElse(null);
    }
}
