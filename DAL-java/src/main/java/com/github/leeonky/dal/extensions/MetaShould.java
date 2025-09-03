package com.github.leeonky.dal.extensions;

import com.github.leeonky.dal.runtime.CurryingMethod;
import com.github.leeonky.dal.runtime.DALRuntimeException;
import com.github.leeonky.dal.runtime.MetaData;
import com.github.leeonky.dal.runtime.ProxyObject;

import static com.github.leeonky.dal.runtime.inspector.DumpingBuffer.rootContext;
import static java.lang.String.format;

public class MetaShould implements ProxyObject {
    private final MetaData<?> metaData;
    private final boolean negative;

    public MetaShould(MetaData<?> metaData) {
        this(metaData, false);
    }

    public MetaShould(MetaData<?> metaData, boolean negative) {
        this.metaData = metaData;
        this.negative = negative;
    }

    @Override
    public Object getValue(Object property) {
        return metaData.data().currying(property).map(curryingMethod -> new PredicateMethod(curryingMethod, property))
                .orElseThrow(() -> new DALRuntimeException(format("Predicate method %s not exist in %s",
                        property, metaData.data().dump())));
    }

    public MetaShould negative() {
        return new MetaShould(metaData, !negative);
    }

    public class PredicateMethod implements ProxyObject {
        private final CurryingMethod curryingMethod;
        private final Object methodName;

        public PredicateMethod(CurryingMethod curryingMethod, Object methodName) {
            this.curryingMethod = curryingMethod;
            this.methodName = methodName;
        }

        public boolean should() {
            Object result = curryingMethod.resolve();
            if (result instanceof CurryingMethod)
                throw new DALRuntimeException(rootContext(metaData.runtimeContext())
                        .append("Failed to invoke predicate method `").append(methodName.toString())
                        .append("`, maybe missing parameters, all candidate methods are:")
                        .indent(curryingMethod::dumpCandidates).content());
            if (result instanceof Boolean)
                return negative != (boolean) result;
            throw new DALRuntimeException(rootContext(metaData.runtimeContext())
                    .append("Predicate method `").append(methodName.toString()).append("` should return boolean but ")
                    .dump(metaData.runtimeContext().data(result)).newLine()
                    .append("all candidate methods are:")
                    .indent(curryingMethod::dumpCandidates).content());
        }

        public String errorMessage() {
            return rootContext(metaData.runtimeContext())
                    .append("Expected: ").dump(metaData.data()).newLine()
                    .append("Should").append(negative ? " not" : "").append(" ").append(methodName.toString()).append(":")
                    .indent(curryingMethod.getResolvedMethod()::dumpArguments).content();
        }

        public CurryingMethod curryingMethodGroup() {
            return curryingMethod;
        }

        @Override
        public Object getValue(Object property) {
            return new PredicateMethod(curryingMethod.call(property), methodName);
        }
    }
}
