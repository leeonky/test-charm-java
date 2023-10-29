package com.github.leeonky.jfactory;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.Data;
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
                ObjectBuilder objectBuilder = collectData(prefix, dalExpression);
                return builder.properties(objectBuilder.map());
            }
        };
    }

    private static ObjectBuilder collectData(String prefix, String dalExpression) {
        DAL dal = getDal();
        ObjectBuilder objectBuilder = new ObjectBuilder();
        try {
            dal.evaluateAll(objectBuilder, prefix + dalExpression);
        } catch (InterpreterException e) {
            throw new IllegalArgumentException("\n" + e.show(prefix + dalExpression, prefix.length()) + "\n\n" + e.getMessage());
        }
        return objectBuilder;
    }

    private static DAL getDal() {
        DAL dal = new DAL().extend();
        dal.getRuntimeContextBuilder().checkerSetForMatching()
                .register((expected, actual) -> {
                    if (actual.getInstance() instanceof ObjectBuilder.EntryBuilder) {
                        return Optional.of(new MatchesChecker() {
                            @Override
                            public Data transformActual(Data actual, Data expected, RuntimeContextBuilder.DALRuntimeContext context) {
                                return actual;
                            }

                            @Override
                            public boolean failed(CheckingContext checkingContext) {
                                ((ObjectBuilder.EntryBuilder) checkingContext.getActual().getInstance())
                                        .setValue(checkingContext.getExpected().getInstance());
                                return false;
                            }
                        });
                    }
                    return Optional.empty();
                });

        dal.getRuntimeContextBuilder().registerPropertyAccessor(ObjectBuilder.class, new PropertyAccessor<ObjectBuilder>() {
//            TODO mv processor to dal

            @Override
            public Object getValue(ObjectBuilder builder, Object property) {
                return builder.add((String) property);
            }

            @Override
            public Set<Object> getPropertyNames(ObjectBuilder instance) {
                return Collections.emptySet();
            }
        });
        return dal;
    }

    public static class ObjectBuilder {
        private final LinkedHashMap<String, EntryBuilder> fields = new LinkedHashMap<>();

        public EntryBuilder add(String property) {
            return fields.computeIfAbsent(property, EntryBuilder::new);
        }

        public Map<String, ?> map() {
            return fields.values().stream().collect(LinkedHashMap::new,
                    (m, f) -> m.put(f.property, f.value), LinkedHashMap::putAll);
        }

        public static class EntryBuilder {
            private final String property;
            private Object value;

            public EntryBuilder(String property) {
                this.property = property;
            }

            public void setValue(Object value) {
                this.value = value;
            }
        }
    }
}
