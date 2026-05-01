package org.testcharm.cucumber.swarm;

import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Pickle;
import org.testcharm.cucumber.swarm.repo.Repository;

import java.net.URI;
import java.util.List;

public class EntityMapper {
    private final Repository<String, Pickle> pickleRepository = new Repository<>(EntityMapper::pickleKey);
    private final Repository<String, Feature> featureRepository = new Repository<>(EntityMapper::featureKey);

    private static String relativeUri(URI uri) {
        return uri.toString();
    }

    public static String pickleKey(Pickle pickle) {
        return relativeUri(pickle.getUri()) + ":" + pickle.getLocation().getLine();
    }

    public static String featureKey(Feature feature) {
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
