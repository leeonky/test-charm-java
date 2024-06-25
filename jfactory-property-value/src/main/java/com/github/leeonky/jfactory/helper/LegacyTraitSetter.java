package com.github.leeonky.jfactory.helper;

public class LegacyTraitSetter {
    private final ObjectReference objectReference;

    public LegacyTraitSetter(ObjectReference objectReference) {
        this.objectReference = objectReference;
    }

    public void addTraitSpec(String traitSpec) {
        traitSpec = traitSpec.trim();
        objectReference.addTraitSpec(traitSpec.substring(1, traitSpec.length() - 1));
    }
}
