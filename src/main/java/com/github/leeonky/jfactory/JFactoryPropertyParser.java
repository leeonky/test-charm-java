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
import com.github.leeonky.jfactory.JFactoryPropertyParser.ObjectReference.ObjectValue;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.github.leeonky.dal.runtime.NodeType.LIST_SCOPE;
import static com.github.leeonky.dal.runtime.NodeType.OBJECT_SCOPE;
import static com.github.leeonky.jfactory.JFactoryPropertyParser.ObjectReference.RawType.*;
import static java.util.Collections.*;

public class JFactoryPropertyParser {
    private static final DAL DAL = getDal();

    //TODO expression is list
    //TODO property is not ""
    public static PropertyValue given(String expression) {
        return new PropertyValue() {
            @Override
            public <T> Builder<T> setToBuilder(String property, Builder<T> builder) {
                Object value = parseProperties(expression);
                if (value instanceof ObjectValue)
                    value = ((ObjectValue) value).flat();
                return builder.properties((Map<String, ?>) value);
            }
        };
    }

    public static Object parseProperties(String expression) {
        String prefix = guessPrefix(expression);
        ObjectReference objectReference = new ObjectReference();
        try {
            DAL.evaluateAll(objectReference, prefix + expression);
        } catch (InterpreterException e) {
            throw new IllegalArgumentException("\n" + e.show(prefix + expression, prefix.length()) + "\n\n" + e.getMessage());
        }
        return objectReference.value();
    }

    private static String guessPrefix(String expression) {
        String prefix = "";
        if (expression.trim().startsWith("{") || expression.trim().startsWith("|") || expression.trim().startsWith("["))
            prefix = ":";
        return prefix;
    }

    public static Specs specs(String expression) {
        String prefix = guessPrefix(expression);
        Specs specs = new Specs();
        try {
            DAL.evaluateAll(specs, prefix + expression);
        } catch (InterpreterException e) {
            throw new IllegalArgumentException("\n" + e.show(prefix + expression, prefix.length()) + "\n\n" + e.getMessage());
        }
        return specs;
    }

    private static DAL getDal() {
        DAL dal = new DAL().extend();
        dal.getRuntimeContextBuilder().checkerSetForEqualing()
                .register((expected, actual) -> {
                    if (actual.instance() instanceof ObjectReference) {
                        if (OBJECT_SCOPE.equals(expected.instance())) {
                            return Optional.of(new ObjectScopeChecker() {
                                @Override
                                public boolean failed(CheckingContext context) {
                                    ((ObjectReference) context.getActual().instance()).rawType(RAW_OBJECT);
                                    return false; // always pass
                                }
                            });
                        }
                        if (LIST_SCOPE.equals(expected.instance())) {
                            return Optional.of(new ObjectScopeChecker() {
                                @Override
                                public boolean failed(CheckingContext context) {
                                    ((ObjectReference) context.getActual().instance()).rawType(RAW_LIST);
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
                                ((ObjectReference) context.getActual().instance()).setValue(context.getExpected().instance());
                                return false; // always pass
                            }
                        });
                    }
                    return Optional.empty();
                });
        dal.getRuntimeContextBuilder().checkerSetForMatching()
                .register((expected, actual) -> {
                    if (actual.instance() instanceof ObjectReference) {
                        if (OBJECT_SCOPE.equals(expected.instance())) {
                            return Optional.of(new MatchesChecker() {
                                @Override
                                public Data transformActual(Data actual, Data expected, RuntimeContextBuilder.DALRuntimeContext context) {
                                    return actual; //do not convert
                                }

                                @Override
                                public boolean failed(CheckingContext context) {
                                    ((ObjectReference) context.getActual().instance()).rawType(OBJECT);
                                    return false; // always pass
                                }
                            });
                        } else if (LIST_SCOPE.equals(expected.instance())) {
                            return Optional.of(new MatchesChecker() {
                                @Override
                                public Data transformActual(Data actual, Data expected, RuntimeContextBuilder.DALRuntimeContext context) {
                                    return actual; //do not convert
                                }

                                @Override
                                public boolean failed(CheckingContext context) {
                                    ((ObjectReference) context.getActual().instance()).rawType(LIST);
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
                                ((ObjectReference) context.getActual().instance()).setValue(context.getExpected().instance());
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
                                .wrap(((ObjectReference) data.instance()).value())))

                .registerPropertyAccessor(Specs.class, new PropertyAccessor<Specs>() {
                    @Override
                    public Object getValue(Specs specs, Object property) {
                        return specs.addData((String) property).getData();
                    }

                    @Override
                    public Set<Object> getPropertyNames(Specs instance) {
                        return emptySet();
                    }
                });

        dal.getRuntimeContextBuilder().registerDataRemark(ObjectReference.class, remarkData -> {
            ObjectReference objectReference = (ObjectReference) remarkData.data().instance();
            objectReference.addTraitSpec(remarkData.remark());
            return remarkData.data();
        });
        return dal;
    }

    public static class ObjectReference {
        private final LinkedHashMap<String, ObjectReference> fields = new LinkedHashMap<>();
        private final LinkedHashMap<Integer, ObjectReference> elements = new LinkedHashMap<>();
        private Object value;
        private RawType rawType = null;
        private String traitSpec;

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

        public void addTraitSpec(String traitSpec) {
            this.traitSpec = traitSpec;
        }

        public enum RawType {
            RAW_OBJECT, RAW_LIST, LIST, OBJECT
        }

        public class ObjectValue extends LinkedHashMap<String, Object> implements FlatAble {

            @Override
            public String buildPropertyName(String property) {
                if (traitSpec != null)
                    property += "(" + traitSpec + ")";
                return property;
            }
        }
    }

    public interface FlatAble {
        default Map<String, Object> flat() {
            LinkedHashMap<String, Object> result = new LinkedHashMap<>();
            forEach((key, value) -> {
                if (value instanceof FlatAble) {
                    ((FlatAble) value).flatSub(result, key);
                } else
                    result.put(key, value);
            });
            return result;
        }

        default String buildPropertyName(String property) {
            return property;
        }

        void forEach(BiConsumer<? super String, ? super Object> action);

        default void flatSub(LinkedHashMap<String, Object> result, String key) {
            for (Map.Entry<String, Object> entry : flat().entrySet())
                result.put(buildPropertyName(key) +
                        (entry.getKey().startsWith("[") ? entry.getKey() : "." + entry.getKey()), entry.getValue());
        }
    }

    public static class ListValue extends ArrayList<Object> implements FlatAble {
        @Override
        public void forEach(BiConsumer<? super String, ? super Object> action) {
            for (int i = 0; i < size(); i++)
                action.accept("[" + i + "]", get(i));
        }
    }

    public static class Specs extends ArrayList<Specs.SpecData> {

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
}
