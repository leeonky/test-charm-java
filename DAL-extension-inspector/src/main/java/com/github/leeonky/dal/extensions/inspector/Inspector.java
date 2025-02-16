package com.github.leeonky.dal.extensions.inspector;

import com.github.leeonky.dal.DAL;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class Inspector {
    public static InspectorCore inspector;

    public static final Set<DAL> instances = new LinkedHashSet<>();

    public static Set<DAL> getInstances() {
        return instances;
    }

    public static void launch() {
        if (inspector == null) {
            inspector = new InspectorCore();
        }
    }

    public static void shutdown() {
        if (inspector != null) {
            inspector.exit();
            inspector = null;
        }
    }

    public static InspectorCore inspector() {
        return Objects.requireNonNull(inspector);
    }

    public static void main(String[] args) {
        launch();
    }
}