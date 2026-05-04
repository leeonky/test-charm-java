package org.testcharm.cucumber.swarm;

import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Pickle;
import org.testcharm.cucumber.swarm.repo.Repository;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class EntityMapper {
    private final Repository<String, Pickle> pickleRepository = new Repository<>(this::pickleKey);
    private final Repository<String, Feature> featureRepository = new Repository<>(this::featureKey);
    private final List<URI> featurePaths;

    public EntityMapper(List<URI> featurePaths) {
        this.featurePaths = featurePaths;
    }

    private String relativeUri(URI fileUri) {
        Path file = Paths.get(fileUri).normalize();
        return featurePaths.stream()
                .filter(Objects::nonNull)
                .map(Paths::get)
                .map(Path::normalize)
                .filter(dir -> file.startsWith(dir) && !file.equals(dir))
                .max(Comparator.comparingInt(Path::getNameCount))
                .map(dir -> dir.relativize(file).toString())
                .orElseThrow(() -> new IllegalArgumentException("Cannot relativize file: " + fileUri + " from " + featurePaths));
    }

    public String pickleKey(Pickle pickle) {
        return relativeUri(pickle.getUri()) + ":" + pickle.getLocation().getLine();
    }

    public String featureKey(Feature feature) {
        return relativeUri(feature.getUri());
    }

    public void mapGherkinFeatures(List<Feature> features) {
        features.forEach(this::mapGherkinFeature);
    }

    private void mapGherkinFeature(Feature feature) {
        featureRepository.save(feature);
        feature.getPickles().forEach(this::mapGherkinPickle);
    }

    private void mapGherkinPickle(Pickle instance) {
        pickleRepository.save(instance);
    }

    public Pickle pickle(String key) {
        return pickleRepository.findById(key);
    }
}
