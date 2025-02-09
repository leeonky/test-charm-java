package com.github.leeonky.dal.extensions.inspector;

import java.util.Objects;

public class Inspector {
    public static InspectorCore inspector;

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