package com.github.leeonky.jfactory.helper;

import com.github.leeonky.dal.runtime.ProxyObject;

import java.util.ArrayList;

import static java.util.Collections.emptyMap;

public class Specs extends ArrayList<Specs.SpecData> implements ProxyObject {

    @Override
    public Object getValue(Object property) {
        return addData((String) property).getData();
    }

    public static class SpecData {
        private final String traitSpec;
        private final ObjectReference data = new ObjectReference();

        public SpecData(String traitSpec) {
            this.traitSpec = traitSpec;
        }

        public ObjectReference getData() {
            return data;
        }

        public Object properties() {
            return data.value() == null ? emptyMap() : data.value();
        }

        public String traitSpec() {
            return getData().getTraitSpec() != null ? getData().getTraitSpec() + " " + traitSpec : traitSpec;
        }
    }

    public SpecData addData(String name) {
        SpecData specData = new SpecData(name);
        add(specData);
        return specData;
    }
}
