package org.testcharm.util;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static org.testcharm.util.Zipped.zip;

public class MapView implements Map<String, Object> {

    private final Map<String, Object> map;

    public MapView(Map<String, Object> map) {
        this.map = map;
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object o) {
        return map.containsKey(o);
    }

    @Override
    public boolean containsValue(Object o) {
        return map.containsValue(o);
    }

    @Override
    public Object get(Object o) {
        return map.get(o);
    }

    @Override
    public Object put(String k, Object v) {
        return map.put(k, v);
    }

    @Override
    public Object remove(Object o) {
        return map.remove(o);
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> map) {
        this.map.putAll(map);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Set<String> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<Object> values() {
        return map.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return map.entrySet();
    }

    @SuppressWarnings("unchecked")
    public <T, R> R get(String key, Function<T, R> mapper) {
        if (!map.containsKey(key))
            return null;
        return mapper.apply((T) map.get(key));
    }

    public <R> R get(Function<MapView, R> mapper) {
        return mapper.apply(this);
    }

    @SuppressWarnings("unchecked")
    public <R> R get(String key) {
        return (R) map.get(key);
    }

    public static Function<Object, String> string() {
        return value -> (String) value;
    }

    public static Function<Object, Long> toLong() {
        return value -> ((Number) value).longValue();
    }

    public static Function<Object, Integer> toInt() {
        return value -> ((Number) value).intValue();
    }

    public static <E extends Enum<E>> Function<Object, E> enumOf(Class<E> enumType) {
        return value -> Enum.valueOf(enumType, string().apply(value));
    }

    public static <T, R> Function<List<T>, List<R>> list(Function<T, R> mapper) {
        return list -> list.stream().map(mapper).collect(toList());
    }

    public static <T, R> Function<List<T>, List<R>> indexedList(Function<Integer, Function<T, R>> indexedMapper) {
        return list -> zip(Zipped::indexSequence, list).stream().map(zippedEntry ->
                indexedMapper.apply(zippedEntry.left()).apply(zippedEntry.right())).collect(toList());
    }

    public static Function<Map<String, Object>, MapView> map() {
        return MapView::new;
    }

    public static <T1, T2, R> Function<MapView, R> composite(Function<MapView, T1> mapper1, Function<MapView, T2> mapper2, BiFunction<T1, T2, R> merge) {
        return map -> {
            T1 t1 = mapper1.apply(map);
            if (t1 == null)
                return null;
            T2 t2 = mapper2.apply(map);
            if (t2 == null)
                return null;
            return merge.apply(t1, t2);
        };
    }
}
