package com.github.leeonky.util;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class BeanClass<T> {
    private final static Map<Class<?>, BeanClass<?>> instanceCache = new ConcurrentHashMap<>();
    private final Map<String, PropertyReader<T>> readers = new LinkedHashMap<>();
    private final Map<String, PropertyWriter<T>> writers = new LinkedHashMap<>();
    private final Class<T> type;
    private final Converter converter = Converter.createDefault();

    private BeanClass(Class<T> type) {
        this.type = type;
        Map<String, Field> addedReaderFields = new HashMap<>();
        Map<String, Field> addedWriterFields = new HashMap<>();

        for (Field field : type.getFields()) {
            Field addedReaderField = addedReaderFields.get(field.getName());
            if (addedReaderField == null || addedReaderField.getType().equals(type)) {
                addReader(new FieldPropertyReader<>(this, field));
                addedReaderFields.put(field.getName(), field);
            }
            Field addedWriterField = addedWriterFields.get(field.getName());
            if (addedWriterField == null || addedWriterField.getType().equals(type)) {
                addWriter(new FieldPropertyWriter<>(this, field));
                addedWriterFields.put(field.getName(), field);
            }
        }
        for (Method method : type.getMethods()) {
            if (MethodPropertyReader.isGetter(method))
                addReader(new MethodPropertyReader<>(this, method));
            if (MethodPropertyWriter.isSetter(method))
                addWriter(new MethodPropertyWriter<>(this, method));
        }
    }

    public static String getClassName(Object object) {
        return object == null ? null : object.getClass().getName();
    }

    @SuppressWarnings("unchecked")
    public static <T> BeanClass<T> create(Class<T> type) {
        return (BeanClass<T>) instanceCache.computeIfAbsent(type, BeanClass::new);
    }

    @SuppressWarnings("unchecked")
    public static <T> T newInstance(Class<T> type, Object... args) {
        List<Constructor<?>> constructors = Stream.of(type.getConstructors())
                .filter(c -> isProperConstructor(c, args))
                .collect(Collectors.toList());
        if (constructors.size() != 1)
            throw new IllegalArgumentException(String.format("No appropriate %s constructor for params [%s]",
                    type.getName(), toString(args)));
        try {
            return (T) constructors.get(0).newInstance(args);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

    private static boolean isProperConstructor(Constructor<?> constructor, Object[] parameters) {
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        return parameterTypes.length == parameters.length && IntStream.range(0, parameterTypes.length)
                .allMatch(i -> parameterTypes[i].isInstance(parameters[i]));
    }

    private static String toString(Object[] parameters) {
        return Stream.of(parameters)
                .map(o -> o == null ? "null" : o.getClass().getName() + ":" + o)
                .collect(Collectors.joining(", "));
    }

    @SuppressWarnings("unchecked")
    public static <E> Stream<E> arrayCollectionToStream(Object collection) {
        if (collection != null) {
            Class<?> collectionType = collection.getClass();
            if (collectionType.isArray())
                return IntStream.range(0, Array.getLength(collection)).mapToObj(i -> (E) Array.get(collection, i));
            else if (collection instanceof Iterable)
                return StreamSupport.stream(((Iterable<E>) collection).spliterator(), false);
        }
        throw new IllegalArgumentException("`" + collection + "` is not collection or array");
    }

    public static List<Object> toChainNodes(String chain) {
        return Arrays.stream(chain.split("[\\[\\].]")).filter(s -> !s.isEmpty()).map(s -> {
            try {
                return Integer.valueOf(s);
            } catch (Exception ignore) {
                return s;
            }
        }).collect(Collectors.toList());
    }

    public Converter getConverter() {
        return converter;
    }

    public Class<T> getType() {
        return type;
    }

    public String getName() {
        return type.getName();
    }

    public String getSimpleName() {
        return type.getSimpleName();
    }

    public Map<String, PropertyReader<T>> getPropertyReaders() {
        return readers;
    }

    public Map<String, PropertyWriter<T>> getPropertyWriters() {
        return writers;
    }

    private void addReader(PropertyReader<T> reader) {
        readers.put(reader.getName(), reader);
    }

    private void addWriter(PropertyWriter<T> writer) {
        writers.put(writer.getName(), writer);
    }

    public Object getPropertyValue(T bean, String property) {
        return getPropertyReader(property).getValue(bean);
    }

    public PropertyReader<T> getPropertyReader(String property) {
        return readers.computeIfAbsent(property, k -> {
            throw new IllegalArgumentException("No available property reader for " + type.getSimpleName() + "." + property);
        });
    }

    public BeanClass<T> setPropertyValue(T bean, String property, Object value) {
        getPropertyWriter(property).setValue(bean, value);
        return this;
    }

    public PropertyWriter<T> getPropertyWriter(String property) {
        return writers.computeIfAbsent(property, k -> {
            throw new IllegalArgumentException("No available property writer for " + type.getSimpleName() + "." + property);
        });
    }

    public T newInstance(Object... args) {
        return newInstance(type, args);
    }

    @SuppressWarnings("unchecked")
    public Object createCollection(List<?> elements) {
        if (getType().isArray()) {
            Object array = Array.newInstance(getType().getComponentType(), elements.size());
            for (int i = 0; i < elements.size(); i++)
                Array.set(array, i, elements.get(i));
            return array;
        }
        if (getType().isInterface()) {
            if (Set.class.isAssignableFrom(getType()))
                return new LinkedHashSet<>(elements);
            if (Iterable.class.isAssignableFrom(getType()))
                return new ArrayList<>(elements);
        } else {
            if (Collection.class.isAssignableFrom(getType())) {
                Collection<Object> collection = (Collection<Object>) newInstance();
                collection.addAll(elements);
                return collection;
            }
        }
        throw new IllegalStateException(String.format("Cannot create instance of collection type %s", getName()));
    }

    public Object getPropertyChainValue(T object, String chain) {
        return getPropertyChainValue(object, toChainNodes(chain));
    }

    public Object getPropertyChainValue(T object, List<Object> chain) {
        return getPropertyChainValueInner(chain, 0, object, new LinkedList<>(chain));
    }

    @SuppressWarnings("unchecked")
    private Object getPropertyChainValueInner(List<Object> originalChain, int level, T object, LinkedList<Object> chain) {
        if (chain.isEmpty())
            return object;
        if (object == null)
            throw new NullPointerInChainException(originalChain, level);
        Object p = chain.removeFirst();
        if (p instanceof Integer) {
            Object[] array = BeanClass.arrayCollectionToStream(object).toArray();
            if ((int) p >= array.length)
                throw new NullPointerInChainException(originalChain, level);
            Object element = array[(int) p];
            if (chain.isEmpty())
                return element;
            if (element == null)
                throw new NullPointerInChainException(originalChain, level + 1);
            return ((BeanClass) BeanClass.create(element.getClass())).getPropertyChainValueInner(originalChain, level + 1, element, chain);
        } else {
            PropertyReader propertyReader = getPropertyReader((String) p);
            return propertyReader.getPropertyTypeWrapper().getPropertyChainValueInner(originalChain, level + 1, propertyReader.getValue(object), chain);
        }
    }

    public PropertyReader<?> getPropertyChainReader(String chain) {
        return getPropertyChainReader(toChainNodes(chain));
    }

    public PropertyReader<?> getPropertyChainReader(List<Object> chain) {
        return getPropertyChainReaderInner(new LinkedList<>(chain));
    }

    public PropertyReader<?> getPropertyChainReaderInner(LinkedList<Object> chain) {
        return getPropertyReader((String) chain.removeFirst()).getPropertyChainReader(chain);
    }

    @SuppressWarnings("unchecked")
    public T createDefault() {
        return (T) Array.get(Array.newInstance(getType(), 1), 0);
    }
}
