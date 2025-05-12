package com.github.leeonky.dal.extensions;

import com.github.leeonky.dal.runtime.*;

import static java.lang.String.format;

public class MetaShould implements ProxyObject {
    private final MetaData<?> metaData;

    public MetaShould(MetaData<?> metaData) {
        this.metaData = metaData;
    }

    @Override
    public Object getValue(Object property) {
        return metaData.data().currying(property).map(curryingMethod -> new PredicateMethod(curryingMethod, property))
                .orElseThrow(() -> new DALRuntimeException(format("Predicate method %s not exist in %s",
                        property, metaData.data().dump())));
    }

    public class PredicateMethod implements ProxyObject {
        private final CurryingMethod curryingMethod;
        private final Object method;

        public PredicateMethod(CurryingMethod curryingMethod, Object method) {
            this.curryingMethod = curryingMethod;
            this.method = method;
        }

        public boolean should(Object value) {
            Object result = curryingMethod.call(value).resolve();
            if (result instanceof CurryingMethod)
                throw new DALRuntimeException(format("Failed to invoke predicate method `%s` of %s, " +
                        "maybe missing parameters", method, metaData.data().dump()));
            if (result instanceof Boolean)
                return (boolean) result;
            throw new DALRuntimeException(format("Predicate method `%s` return type should boolean but %s", method,
                    metaData.runtimeContext().data(result).dump()));
        }

        public Data<?> instance() {
            return metaData.data();
        }

        public String errorMessage(Data<?> expected) {
            return format("Expected: %s\nShould %s: %s", instance().dump(), method, expected.dump());
        }

        @Override
        public Object getValue(Object property) {
            return new PredicateMethod(curryingMethod.call(property), method);
        }
    }
}
