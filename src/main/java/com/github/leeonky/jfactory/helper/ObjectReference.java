package com.github.leeonky.jfactory.helper;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.github.leeonky.jfactory.helper.ObjectReference.RawType.*;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

public class ObjectReference {
    private final LinkedHashMap<String, ObjectReference> fields = new LinkedHashMap<>();
    private final LinkedHashMap<Integer, ObjectReference> elements = new LinkedHashMap<>();
    private Object value;
    private RawType rawType = null;
    private String traitSpec;
    private boolean intently = false;

    public void setValue(Object value) {
        this.value = value;
    }

    public ObjectReference add(String property) {
        return fields.computeIfAbsent(property, k -> new ObjectReference());
    }

    private Map<String, Object> map() {
        Map<String, Object> result = RAW_OBJECT == rawType ? new LinkedHashMap<>() : new TraitSpecObjectValue();
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
        Map<String, Object> result = new TraitSpecObjectValue();
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

    public void intently() {
        intently = true;
    }

    public String getTraitSpec() {
        return traitSpec;
    }

    public enum RawType {
        RAW_OBJECT, RAW_LIST, LIST, OBJECT
    }

    public class TraitSpecObjectValue extends ObjectValue {

        @Override
        public String buildPropertyName(String property) {
            if (traitSpec != null)
                property += "(" + traitSpec + ")";
            if (intently)
                property += "!";
            return property;
        }
    }
}
