package com.github.leeonky.util;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.github.leeonky.util.BeanClass.create;

public class CollectionHelper {

    @SuppressWarnings("unchecked")
    public static <E> Stream<E> toStream(Object collection) {
        if (collection != null) {
            Class<?> collectionType = collection.getClass();
            if (collectionType.isArray())
                return IntStream.range(0, Array.getLength(collection)).mapToObj(i -> (E) Array.get(collection, i));
            else if (collection instanceof Iterable)
                return StreamSupport.stream(((Iterable<E>) collection).spliterator(), false);
            else if (collection instanceof Stream)
                return (Stream<E>) collection;
        }
        throw new CannotToStreamException(collection);
    }

    public static <T> T convert(Object collection, BeanClass<T> toType) {
        return convert(collection, toType, Converter.getInstance());
    }

    @SuppressWarnings("unchecked")
    public static <T> T convert(Object collection, BeanClass<T> toType, Converter elementConverter) {
        if (collection != null) {
            Class<?> elementType = toType.getElementType().getType();
            return (T) toType.createCollection((toStream(collection).map(o ->
                    elementConverter.convert(elementType, o))).collect(Collectors.toList()));
        }
        return null;
    }

    public static Object createCollection(Collection<?> elements, BeanClass<?> type) {
        if (type.getType().isArray())
            return createArray(elements, type);
        if (type.getType().isInterface())
            return createInterfaceCollection(elements, type);
        if (Collection.class.isAssignableFrom(type.getType()))
            return createClassCollection(elements, type);
        throw new IllegalStateException(String.format("Cannot create instance of collection type %s", type.getName()));
    }

    @SuppressWarnings("unchecked")
    private static Collection<Object> createClassCollection(Collection<?> elements, BeanClass<?> type) {
        Collection<Object> collection = (Collection<Object>) type.newInstance();
        collection.addAll(elements);
        return collection;
    }

    private static AbstractCollection<?> createInterfaceCollection(Collection<?> elements, BeanClass<?> type) {
        if (Set.class.isAssignableFrom(type.getType()))
            return new LinkedHashSet<>(elements);
        if (Iterable.class.isAssignableFrom(type.getType()))
            return new ArrayList<>(elements);
        throw new IllegalStateException(String.format("Cannot create instance of collection type %s", type.getName()));
    }

    private static Object createArray(Collection<?> elements, BeanClass<?> type) {
        Object array = Array.newInstance(type.getType().getComponentType(), elements.size());
        int i = 0;
        for (Object element : elements)
            Array.set(array, i++, element);
        return array;
    }

    public static boolean equals(Object obj1, Object obj2) {
        if (obj1 == null || obj2 == null)
            return Objects.equals(obj1, obj2);
        return convert(obj1, create(List.class)).equals(convert(obj2, create(List.class)));
    }

    @SuppressWarnings("unchecked")
    public static <T> BeanClass<T> reify(Class<?> original, Type elementType) {
        if (original.isArray())
            return (BeanClass<T>) create(Array.newInstance((Class<?>) elementType, 0).getClass());
        return GenericBeanClass.create(original, elementType);
    }
}
