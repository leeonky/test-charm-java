package com.github.leeonky.jfactory;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.InfiniteDALCollection;
import com.github.leeonky.dal.runtime.PropertyAccessor;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder;
import com.github.leeonky.dal.runtime.checker.CheckingContext;
import com.github.leeonky.dal.runtime.checker.EqualsChecker;
import com.github.leeonky.dal.runtime.checker.MatchesChecker;
import com.github.leeonky.dal.runtime.checker.ObjectScopeChecker;
import com.github.leeonky.interpreter.InterpreterException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.github.leeonky.dal.runtime.NodeType.LIST_SCOPE;
import static com.github.leeonky.dal.runtime.NodeType.OBJECT_SCOPE;
import static com.github.leeonky.jfactory.DALHelper.ObjectReference.RawType.*;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

public class DALHelper {

    public static PropertyValue given(String dalExpression) {
        return new PropertyValue() {
            @Override
            public <T> Builder<T> setToBuilder(String property, Builder<T> builder) {
                Object value = dalParse(dalExpression);
                if (value instanceof ObjectValue)
                    value = ((ObjectValue) value).flat();
                return builder.properties((Map<String, ?>) value);
            }
        };
    }

    public static Object dalParse(String dalExpression) {
        String prefix = "";
        if (dalExpression.trim().startsWith("{") || dalExpression.trim().startsWith("|"))
            prefix = ":";
        DAL dal = getDal();
        ObjectReference objectReference = new ObjectReference();
        try {
            dal.evaluateAll(objectReference, prefix + dalExpression);
        } catch (InterpreterException e) {
            throw new IllegalArgumentException("\n" + e.show(prefix + dalExpression, prefix.length()) + "\n\n" + e.getMessage());
        }
        return objectReference.value();
    }

    private static DAL getDal() {
        DAL dal = new DAL().extend();
        dal.getRuntimeContextBuilder().checkerSetForEqualing()
                .register((expected, actual) -> {
                    if (actual.getInstance() instanceof ObjectReference) {
                        if (OBJECT_SCOPE.equals(expected.getInstance())) {
                            return Optional.of(new ObjectScopeChecker() {
                                @Override
                                public boolean failed(CheckingContext context) {
                                    ((ObjectReference) context.getActual().getInstance()).rawType(RAW_OBJECT);
                                    return false; // always pass
                                }
                            });
                        }
                        if (LIST_SCOPE.equals(expected.getInstance())) {
                            return Optional.of(new ObjectScopeChecker() {
                                @Override
                                public boolean failed(CheckingContext context) {
                                    ((ObjectReference) context.getActual().getInstance()).rawType(RAW_LIST);
                                    return false; // always pass
                                }
                            });
                        }

                        return Optional.of(new EqualsChecker() {
                            @Override
                            public Data transformActual(Data actual, Data expected, RuntimeContextBuilder.DALRuntimeContext context) {
                                return actual; //do not convert
                            }

                            @Override
                            public boolean failed(CheckingContext context) {
                                ((ObjectReference) context.getActual().getInstance()).setValue(context.getExpected().getInstance());
                                return false; // always pass
                            }
                        });
                    }
                    return Optional.empty();
                });
        dal.getRuntimeContextBuilder().checkerSetForMatching()
                .register((expected, actual) -> {
                    if (actual.getInstance() instanceof ObjectReference) {
                        if (OBJECT_SCOPE.equals(expected.getInstance())) {
                            return Optional.of(new MatchesChecker() {
                                @Override
                                public Data transformActual(Data actual, Data expected, RuntimeContextBuilder.DALRuntimeContext context) {
                                    return actual; //do not convert
                                }

                                @Override
                                public boolean failed(CheckingContext context) {
                                    ((ObjectReference) context.getActual().getInstance()).rawType(OBJECT);
                                    return false; // always pass
                                }
                            });
                        } else if (LIST_SCOPE.equals(expected.getInstance())) {
                            return Optional.of(new MatchesChecker() {
                                @Override
                                public Data transformActual(Data actual, Data expected, RuntimeContextBuilder.DALRuntimeContext context) {
                                    return actual; //do not convert
                                }

                                @Override
                                public boolean failed(CheckingContext context) {
                                    ((ObjectReference) context.getActual().getInstance()).rawType(LIST);
                                    return false; // always pass
                                }
                            });
                        }
                        return Optional.of(new MatchesChecker() {
                            @Override
                            public Data transformActual(Data actual, Data expected, RuntimeContextBuilder.DALRuntimeContext context) {
                                return actual; //do not convert
                            }

                            @Override
                            public boolean failed(CheckingContext context) {
                                ((ObjectReference) context.getActual().getInstance()).setValue(context.getExpected().getInstance());
                                return false; // always pass
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
                        })
                .registerDumper(ObjectReference.class, _ignore -> (data, dumpingBuffer) ->
                        dumpingBuffer.dump(dumpingBuffer.getRuntimeContext()
                                .wrap(((ObjectReference) data.getInstance()).value())))
        ;
        return dal;
    }

    public static class ObjectReference {
        private final LinkedHashMap<String, ObjectReference> fields = new LinkedHashMap<>();
        private final LinkedHashMap<Integer, ObjectReference> elements = new LinkedHashMap<>();
        private Object value;
        private RawType rawType = null;

        public void setValue(Object value) {
            this.value = value;
        }

        public ObjectReference add(String property) {
            return fields.computeIfAbsent(property, k -> new ObjectReference());
        }

        private Map<String, Object> map() {
            Map<String, Object> result = RAW_OBJECT == rawType ? new LinkedHashMap<>() : new ObjectValue();
            fields.forEach((k, v) -> result.put(k, v.value()));
            return result;
        }

        private static final ObjectReference EMPTY_REFERENCE = new ObjectReference();

        private Object list() {
            int maxIndex = elements.keySet().stream().max(Integer::compare).orElse(-1);
            if (RAW_LIST == rawType) {
                return IntStream.range(0, maxIndex + 1).mapToObj(i -> elements.getOrDefault(i, EMPTY_REFERENCE))
                        .map(ObjectReference::value).collect(Collectors.toList());
            }
            if (maxIndex + 1 == elements.size()) {
                return IntStream.range(0, maxIndex + 1).mapToObj(elements::get)
                        .map(ObjectReference::value).collect(Collectors.toCollection(ListValue::new));
            }
            Map<String, Object> result = new ObjectValue();
            elements.forEach((k, v) -> result.put("[" + k + "]", v.value()));
            return result;
        }

        public Object value() {
            if (fields.size() > 0 || RAW_OBJECT == rawType)
                return map();
            if (elements.size() > 0 || RAW_LIST == rawType)
                return list();
            if (LIST == rawType)
                return emptyList();
            if (OBJECT == rawType)
                return emptyMap();
            return value;
        }

        public ObjectReference touchElement(int position, ObjectReference touched) {
            elements.put(position, touched);
            return touched;
        }

        public void rawType(RawType type) {
            rawType = type;
        }

        public enum RawType {
            RAW_OBJECT, RAW_LIST, LIST, OBJECT
        }
    }

    public interface FlatAble {
        Map<String, Object> flat();
    }

    public static class ObjectValue extends LinkedHashMap<String, Object> implements FlatAble {
        @Override
        public Map<String, Object> flat() {
            LinkedHashMap<String, Object> result = new LinkedHashMap<>();
            forEach((key, value) -> {
                if (value instanceof FlatAble)
                    ((FlatAble) value).flat().forEach((subKey, subValue) -> result.put(key + "." + subKey, subValue));
                else
                    result.put(key, value);
            });
            return result;
        }
    }

    public static class ListValue extends ArrayList<Object> implements FlatAble {
        @Override
        public Map<String, Object> flat() {
            LinkedHashMap<String, Object> result = new LinkedHashMap<>();
            for (int i = 0; i < size(); i++) {
                Object value = get(i);
                if (value instanceof FlatAble)
                    for (Map.Entry<String, ?> entry : ((FlatAble) value).flat().entrySet())
                        result.put("[" + i + "]." + entry.getKey(), entry.getValue());
                else
                    result.put("[" + i + "]", value);
            }
            return result;
        }
    }
}
