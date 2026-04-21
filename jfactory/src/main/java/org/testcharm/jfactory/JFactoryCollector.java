package org.testcharm.jfactory;

import org.testcharm.util.Collector;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class JFactoryCollector extends Collector {
    protected final JFactory jFactory;
    protected Class<?> defaultType;
    protected String[] traitsSpec;
    private boolean isSpecifySpec = false;
    private boolean raw = false;
    private boolean intently = false;

    protected JFactoryCollector(JFactory jFactory, Class<?> defaultType) {
        this.defaultType = defaultType;
        this.jFactory = jFactory;
    }

    protected JFactoryCollector(JFactory jFactory, String... traitsSpec) {
        this(jFactory, Object.class);
        if (traitsSpec.length == 0)
            throw new IllegalArgumentException("Traits spec cannot be empty");
        this.traitsSpec = traitsSpec;
    }

    @Override
    public Object build() {
        if (traitsSpec == null || raw) {
            if (defaultType.equals(Object.class) || raw) {
                return super.build();
            }
        }
        return (traitsSpec != null ? jFactory.spec(traitsSpec) : jFactory.type(defaultType)).properties(properties()).create();
    }

    @Override
    protected Collector createSubCollector() {
        return jFactory.collector();
    }

    @SuppressWarnings("unchecked")
    public Map<String, ?> properties() {
        Object o = objectValue();
        return o instanceof FlatAble ? ((FlatAble) o).flat() : (Map<String, ?>) o;
    }

    public Collector traitsSpec(String[] traitsSpec) {
        isSpecifySpec = true;
        this.traitsSpec = Arrays.copyOf(traitsSpec, traitsSpec.length);
        return this;
    }

    public Collector traitsSpec(String traitsSpec) {
        return traitsSpec(traitsSpec.trim().split(", |,| "));
    }

    public Collector defaultType(Class<?> defaultType) {
        this.defaultType = defaultType;
        return this;
    }

    public void raw() {
        if (isSpecifySpec)
            throw new IllegalStateException("Cannot create raw Map/List when traits were specified");
        raw = true;
    }

    public void intently() {
        intently = true;
    }

    private Object objectValue() {
        if (raw)
            return build();
        switch (type()) {
            case VALUE:
                return value();
            case LIST:
                return new ObjectValue(elements(), k -> "[" + k + "]") {
                    @Override
                    public boolean isList() {
                        return true;
                    }
                };
            default:
                return new ObjectValue(fields(), Function.identity());
        }
    }

    interface FlatAble {

        default Map<String, Object> flat() {
            LinkedHashMap<String, Object> map = new LinkedHashMap<>();
            forEach((key, value) -> {
                if (value instanceof FlatAble)
                    ((FlatAble) value).flatSub(map, key);
                else if (value instanceof Map && ((Map<?, ?>) value).isEmpty())
                    map.put(key + "(EMPTY_MAP)", value);
                else
                    map.put(key, value);
            });
            return map;
        }

        default String buildPropertyName(String property) {
            return property;
        }

        void forEach(BiConsumer<? super String, ? super Object> action);

        default void flatSub(LinkedHashMap<String, Object> result, String key) {
            Map<String, Object> flat = flat();
            if (flat.isEmpty()) {
                if (isList()) {
//                    TODO missing test break rules: = [] : []
                    result.put(buildPropertyName(key), Collections.emptyList());
                } else {
                    result.put(buildPropertyName(key), flat);
                }
            } else
                flat.forEach((subKey, subValue) -> result.put(buildPropertyName(key) +
                        (subKey.startsWith("[") ? subKey : "." + subKey), subValue));
        }

        default boolean isList() {
            return false;
        }
    }

    class ObjectValue extends LinkedHashMap<String, Object> implements FlatAble {
        public <K> ObjectValue(Map<K, ? extends Collector> data, Function<K, String> keyMapper) {
            data.forEach((key, value) -> put(keyMapper.apply(key), ((JFactoryCollector) value).objectValue()));
        }

        @Override
        public String buildPropertyName(String property) {
            if (traitsSpec != null)
                property += "(" + String.join(" ", traitsSpec) + ")";
            if (intently)
                property += "!";
            return property;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<Integer, ? extends JFactoryCollector> elements() {
        return (Map<Integer, ? extends JFactoryCollector>) super.elements();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, ? extends JFactoryCollector> fields() {
        return (Map<String, ? extends JFactoryCollector>) super.fields();
    }
}
