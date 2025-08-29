package com.github.leeonky.dal.extensions;

import com.github.leeonky.dal.runtime.*;
import com.github.leeonky.dal.runtime.inspector.DumpingBuffer;

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
        private final CurryingMethodGroup curryingMethodGroup;
        private final Object method;

        public PredicateMethod(CurryingMethodGroup curryingMethodGroup, Object method) {
            this.curryingMethodGroup = curryingMethodGroup;
            this.method = method;
        }

        public boolean should() {
            Object result = curryingMethodGroup.resolve();
            if (result instanceof CurryingMethodGroup)
                throw new DALRuntimeException(DumpingBuffer.rootContext(metaData.runtimeContext())
                        .append("Failed to invoke predicate method `").append(method.toString()).append("` of ")
                        .dump(metaData.data()).append(", maybe missing parameters, all candidate methods are:")
                        .indent(curryingMethodGroup::dumpCandidates).content());
            if (result instanceof Boolean)
                return negative != (boolean) result;
            throw new DALRuntimeException(DumpingBuffer.rootContext(metaData.runtimeContext())
                    .append("Predicate method `").append(method.toString()).append("` should return boolean but ")
                    .dump(metaData.runtimeContext().data(result)).newLine()
                    .append("all candidate methods are:")
                    .indent(curryingMethodGroup::dumpCandidates).content());
//            throw new DALRuntimeException(format("Predicate method `%s` return type should boolean but %s", method,
//                    metaData.runtimeContext().data(result).dump()));
        }

        public String errorMessage(Data<?> expected) {
            DumpingBuffer dumpingBuffer = DumpingBuffer.rootContext(metaData.runtimeContext());
            dumpingBuffer.append("Expected: ").dump(metaData.data()).newLine().append("Should");
            if (negative)
                dumpingBuffer.append(" not");
            dumpingBuffer.append(" ").append(method.toString()).append(":");
            CurryingMethodGroup called = curryingMethodGroup.call(expected.value());
            called.resolve();
            dumpingBuffer.indent(called.getResolvedMethod()::dumpArguments);
            return dumpingBuffer.content();
        }

        @Override
        public Object getValue(Object property) {
            return new PredicateMethod(curryingMethodGroup.call(property), method);
        }
    }
}
