package com.github.leeonky.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.github.leeonky.util.Suppressor.get;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

public class BeanClass<T> {
    private final static Map<Class<?>, BeanClass<?>> instanceCache = new ConcurrentHashMap<>();
    private static Converter converter = Converter.getInstance();
    private final TypeInfo<T> typeInfo;
    private final Class<T> type;

    protected BeanClass(Class<T> type) {
        this.type = Objects.requireNonNull(type);
        typeInfo = TypeInfo.create(this);
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
        return get(() -> (T) chooseConstructor(type, args).newInstance(args));
    }

    private static <T> Constructor<?> chooseConstructor(Class<T> type, Object[] args) {
        List<Constructor<?>> constructors = Stream.of(type.getConstructors())
                .filter(c -> isProperConstructor(c, args))
                .collect(Collectors.toList());
        if (constructors.size() != 1)
            throw new NoAppropriateConstructorException(type, args);
        return constructors.get(0);
    }

    private static boolean isProperConstructor(Constructor<?> constructor, Object[] parameters) {
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        return parameterTypes.length == parameters.length && IntStream.range(0, parameterTypes.length)
                .allMatch(i -> parameterTypes[i].isInstance(parameters[i]));
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
        throw new CannotToStreamException(collection);
    }

    public static <T> Optional<T> cast(Object value, Class<T> type) {
        return ofNullable(value)
                .filter(type::isInstance)
                .map(type::cast);
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

    public static BeanClass<?> create(GenericType type) {
        if (!type.hasTypeArguments())
            return create(type.getRawType());
        return GenericBeanClass.create(type);
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<T> getClass(T instance) {
        return (Class<T>) Objects.requireNonNull(instance).getClass();
    }

    public static <T> BeanClass<T> createFrom(T instance) {
        return create(getClass(instance));
    }

    public static Converter getConverter() {
        return converter;
    }

    public static void setConverter(Converter converter) {
        BeanClass.converter = converter;
    }

    public static Class<?> boxedClass(Class<?> source) {
        if (source.isPrimitive())
            if (source == char.class)
                return Character.class;
        if (source == int.class)
            return Integer.class;
        else if (source == short.class)
            return Short.class;
        else if (source == long.class)
            return Long.class;
        else if (source == float.class)
            return Float.class;
        else if (source == double.class)
            return Double.class;
        else if (source == boolean.class)
            return Boolean.class;
        return source;
    }

    public static List<Class<?>> allTypesIn(String packageName) {
        return new ArrayList<Class<?>>() {{
            try {
                Enumeration<URL> resources = getClassLoader().getResources(packageName.replaceAll("[.]", "/"));
                while (resources.hasMoreElements())
                    addAll(getClasses(packageName, resources.nextElement()));
            } catch (Exception ignore) {
            }
        }};
    }

    private static ClassLoader getClassLoader() {
        ClassLoader classLoader = null;
        try {
            classLoader = Thread.currentThread().getContextClassLoader();
        } catch (Throwable ignore) {
        }
        if (classLoader == null) {
            classLoader = BeanClass.class.getClassLoader();
            if (classLoader == null) {
                try {
                    classLoader = ClassLoader.getSystemClassLoader();
                } catch (Throwable ignore) {
                }
            }
        }
        return classLoader;
    }

    private static List<Class<?>> getClasses(String packageName, URL resource) {
        try {
            if ("jar".equals(resource.getProtocol()))
                return ((JarURLConnection) resource.openConnection()).getJarFile().stream().map(jarEntry -> jarEntry.getName().replace('/', '.'))
                        .filter(name -> name.endsWith(".class") && name.startsWith(packageName))
                        .map(name -> Suppressor.get(() -> Class.forName(name.substring(0, name.length() - 6))))
                        .collect(Collectors.toList());
            else {
                InputStream stream = resource.openStream();
                return stream == null ? emptyList()
                        : new BufferedReader(new InputStreamReader(stream)).lines()
                        .filter(line -> line.endsWith(".class"))
                        .map(line -> toClass(line, packageName))
                        .collect(Collectors.toList());
            }
        } catch (Exception ignore) {
            return emptyList();
        }
    }

    private static Class<?> toClass(String className, String packageName) {
        return get(() -> Class.forName(packageName + "." + className.substring(0, className.lastIndexOf('.'))));
    }

    public static List<Class<?>> subTypesOf(Class<?> superClass, String packageName) {
        return assignableTypesOf(superClass, packageName).stream().filter(c -> !superClass.equals(c))
                .collect(Collectors.toList());
    }

    public static List<Class<?>> assignableTypesOf(Class<?> superClass, String packageName) {
        return allTypesIn(packageName).stream().filter(superClass::isAssignableFrom).collect(Collectors.toList());
    }

    public static int compareByExtends(Class<?> type1, Class<?> type2) {
        return type1.equals(type2) ? 0 : type1.isAssignableFrom(type2) ? 1 : -1;
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
        return typeInfo.getReaders();
    }

    public Map<String, PropertyWriter<T>> getPropertyWriters() {
        return typeInfo.getWriters();
    }

    public Object getPropertyValue(T bean, String property) {
        return getPropertyReader(property).getValue(bean);
    }

    public PropertyReader<T> getPropertyReader(String property) {
        return typeInfo.getReader(property);
    }

    public BeanClass<T> setPropertyValue(T bean, String property, Object value) {
        getPropertyWriter(property).setValue(bean, value);
        return this;
    }

    public PropertyWriter<T> getPropertyWriter(String property) {
        return typeInfo.getWriter(property);
    }

    public T newInstance(Object... args) {
        return newInstance(type, args);
    }

    @SuppressWarnings("unchecked")
    public Object createCollection(Collection<?> elements) {
        if (getType().isArray()) {
            Object array = Array.newInstance(getType().getComponentType(), elements.size());
            int i = 0;
            for (Object element : elements)
                Array.set(array, i++, element);
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
        }
        PropertyReader propertyReader = getPropertyReader((String) p);
        return propertyReader.getType().getPropertyChainValueInner(originalChain, level + 1, propertyReader.getValue(object), chain);
    }

    public PropertyReader<?> getPropertyChainReader(String chain) {
        return getPropertyChainReader(toChainNodes(chain));
    }

    public PropertyReader<?> getPropertyChainReader(List<Object> chain) {
        return getPropertyChainReaderInner(new LinkedList<>(chain));
    }

    private PropertyReader<?> getPropertyChainReaderInner(LinkedList<Object> chain) {
        return getPropertyReader((String) chain.removeFirst()).getPropertyChainReader(chain);
    }

    @SuppressWarnings("unchecked")
    public T createDefault() {
        return (T) Array.get(Array.newInstance(getType(), 1), 0);
    }

    public boolean hasTypeArguments() {
        return false;
    }

    public Optional<BeanClass<?>> getTypeArguments(int position) {
        return Optional.empty();
    }

    public BeanClass<?> getElementType() {
        if (type.isArray())
            return BeanClass.create(type.getComponentType());
        if (Iterable.class.isAssignableFrom(type))
            return getTypeArguments(0).orElseGet(() -> BeanClass.create(Object.class));
        return null;
    }

    public BeanClass<?> getElementOrPropertyType() {
        BeanClass<?> elementType = getElementType();
        return elementType == null ? this : elementType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(BeanClass.class, type);
    }

    @Override
    public boolean equals(Object obj) {
        return obj.getClass().equals(BeanClass.class) && Objects.equals(((BeanClass) obj).getType(), type);
    }

    @SuppressWarnings("unchecked")
    public <S> BeanClass<S> getSuper(Class<S> target) {
        List<BeanClass> superBeanClasses = supers();
        return (BeanClass<S>) superBeanClasses.stream().filter(beanClass -> beanClass.getType().equals(target))
                .findFirst().orElseGet(() -> (BeanClass<S>) superBeanClasses.stream()
                        .map(beanClass -> beanClass.getSuper(target))
                        .filter(Objects::nonNull).findFirst().orElse(null));
    }

    private List<BeanClass> supers() {
        List<Type> suppers = new ArrayList<>(asList(type.getGenericInterfaces()));
        suppers.add(type.getGenericSuperclass());
        return suppers.stream().filter(Objects::nonNull)
                .map(t -> BeanClass.create(GenericType.createGenericType(t)))
                .collect(Collectors.toList());
    }

    public boolean isCollection() {
        return getType().isArray() || Iterable.class.isAssignableFrom(getType());
    }

    public Map<String, Property<T>> getProperties() {
        return typeInfo.getProperties();
    }

    public Property<T> getProperty(String name) {
        return typeInfo.getProperty(name);
    }
}
