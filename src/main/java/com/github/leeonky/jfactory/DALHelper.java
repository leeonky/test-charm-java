package com.github.leeonky.jfactory;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.InfiniteDALCollection;
import com.github.leeonky.dal.runtime.PropertyAccessor;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder;
import com.github.leeonky.dal.runtime.checker.CheckingContext;
import com.github.leeonky.dal.runtime.checker.MatchesChecker;
import com.github.leeonky.interpreter.InterpreterException;

import java.util.*;

public class DALHelper {

    public static PropertyValue given(String dalExpression) {
        return new PropertyValue() {
            @Override
            public <T> Builder<T> setToBuilder(String property, Builder<T> builder) {
                String prefix = "";
                if (dalExpression.trim().startsWith("{"))
                    prefix = ":";
                ObjectReference objectReference = collectData(prefix, dalExpression);
                return builder.properties(objectReference.map().flat());
            }
        };
    }

    private static ObjectReference collectData(String prefix, String dalExpression) {
        DAL dal = getDal();
        ObjectReference objectReference = new ObjectReference();
        try {
            dal.evaluateAll(objectReference, prefix + dalExpression);
        } catch (InterpreterException e) {
            throw new IllegalArgumentException("\n" + e.show(prefix + dalExpression, prefix.length()) + "\n\n" + e.getMessage());
        }
        return objectReference;
    }

    private static DAL getDal() {
        DAL dal = new DAL().extend();
        dal.getRuntimeContextBuilder().checkerSetForMatching()
                .register((expected, actual) -> {
                    if (actual.getInstance() instanceof ObjectReference) {
                        return Optional.of(new MatchesChecker() {
                            @Override
                            public Data transformActual(Data actual, Data expected, RuntimeContextBuilder.DALRuntimeContext context) {
                                return actual;
                            }

                            @Override
                            public boolean failed(CheckingContext checkingContext) {
                                ((ObjectReference) checkingContext.getActual().getInstance())
                                        .setValue(checkingContext.getExpected().getInstance());
                                return false;
                            }
                        });
                    }
                    return Optional.empty();
                });

        dal.getRuntimeContextBuilder().registerPropertyAccessor(ObjectReference.class, new PropertyAccessor<ObjectReference>() {
//            TODO mv processor to dal

                    @Override
                    public Object getValue(ObjectReference builder, Object property) {
                        return builder.add((String) property);
                    }

                    @Override
                    public Set<Object> getPropertyNames(ObjectReference instance) {
                        return Collections.emptySet();
                    }
                })
                .registerDALCollectionFactory(ObjectReference.class, reference ->
                        new InfiniteDALCollection<ObjectReference>(ObjectReference::new) {

                            @Override
                            protected ObjectReference getByPosition(int position) {
                                return reference.touchElement(position, super.getByPosition(position));
                            }
                        });
        return dal;
    }

    public static class ObjectReference {
        private final LinkedHashMap<String, ObjectReference> fields = new LinkedHashMap<>();
        private final LinkedHashMap<String, ObjectReference> elements = new LinkedHashMap<>();
        private Object value;

        public void setValue(Object value) {
            this.value = value;
        }

        public ObjectReference add(String property) {
            return fields.computeIfAbsent(property, k -> new ObjectReference());
        }

        private ObjectValue map() {
            ObjectValue result = new ObjectValue();
            fields.forEach((k, v) -> result.put(k, v.getValue()));
            return result;
        }

        public Object getValue() {
            if (fields.size() > 0)
                return map();
            return value;
        }

        public ObjectReference touchElement(int position, ObjectReference touched) {
            fields.put("[" + position + "]", touched);
            return touched;
        }
    }

    public static class ObjectValue extends LinkedHashMap<String, Object> {
        public Map<String, ?> flat() {
            LinkedHashMap<String, Object> result = new LinkedHashMap<>();
            forEach((key, value) -> {
                if (value instanceof ObjectValue) {
                    Map<String, ?> sub = ((ObjectValue) value).flat();
                    sub.forEach((subKey, subValue) ->
                            result.put(key + "." + subKey, subValue));
                } else
                    result.put(key, value);
            });
            return result;
        }
    }

    public static class ListValue extends ArrayList<Object> {
    }
}
