package com.github.leeonky.util;

import java.lang.reflect.Array;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class CollectionHelper {

    @SuppressWarnings("unchecked")
    public static <E> Stream<E> toStream(Object collection) {
        if (collection != null) {
            Class<?> collectionType = collection.getClass();
            if (collectionType.isArray())
                return IntStream.range(0, Array.getLength(collection)).mapToObj(i -> (E) Array.get(collection, i));
            else if (collection instanceof Iterable)
                return StreamSupport.stream(((Iterable<E>) collection).spliterator(), false);
        }
        throw new CannotToStreamException(collection);
    }

    public static <T> T convert(Object collection, BeanClass<T> toType) {
        return convert(collection, toType, Converter.getInstance());
    }

    public static <T> T convert(Object collection, BeanClass<T> toType, Converter elementConverter) {
        if (collection != null) {
            Class<?> elementType = toType.getElementType().getType();
            return (T) toType.createCollection((toStream(collection).map(o ->
                    elementConverter.convert(elementType, o))).collect(Collectors.toList()));
        }
        return null;
    }
}
