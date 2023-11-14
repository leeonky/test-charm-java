package com.github.leeonky.jfactory.helper;

import java.util.ArrayList;

public class Specs extends ArrayList<Specs.SpecData> {

    public static class SpecData {
        private final String traitSpec;
        private final ObjectReference data = new ObjectReference();

        public SpecData(String traitSpec) {
            this.traitSpec = traitSpec;
        }

        public ObjectReference getData() {
            return data;
        }

        public String traitSpec() {
            return traitSpec;
        }
    }

    public SpecData addData(String name) {
        SpecData specData = new SpecData(name);
        add(specData);
        return specData;
    }
}
