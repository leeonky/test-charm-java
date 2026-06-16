package org.testcharm.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.testcharm.util.Suppressor.getIgnoring;
import static org.testcharm.util.Suppressor.runIgnoring;
import static org.testcharm.util.function.Extension.getFirstPresent;

public class Classes {
    public static List<Class<?>> allTypesIn(String packageName) {
        return new ArrayList<>(new HashSet<Class<?>>() {{
            runIgnoring(() -> {
                Enumeration<URL> resources = getClassLoader().getResources(packageName.replaceAll("[.]", "/"));
                while (resources.hasMoreElements())
                    addAll(getClasses(packageName, resources.nextElement()));
            });
        }});
    }

    private static ClassLoader getClassLoader() {
        return getFirstPresent(classLoader2(Thread.currentThread()::getContextClassLoader),
                classLoader2(Classes.class::getClassLoader),
                classLoader2(ClassLoader::getSystemClassLoader))
                .orElseThrow(IllegalStateException::new);
    }

    private static Supplier<Optional<ClassLoader>> classLoader2(Supplier<ClassLoader> factory) {
        return () -> getIgnoring(() -> ofNullable(factory.get()), empty());
    }

    private static List<Class<?>> getClasses(String packageName, URL resource) {
        return getIgnoring(() -> {
            if ("jar".equals(resource.getProtocol()))
                return ((JarURLConnection) resource.openConnection()).getJarFile().stream()
                        .map(jarEntry -> jarEntry.getName().replace('/', '.'))
                        .filter(name -> name.endsWith(".class") && name.startsWith(packageName))
                        .map(Sneaky.sneakyGet(name -> Class.forName(name.substring(0, name.length() - 6))))
                        .collect(toList());
            else {
                InputStream stream = resource.openStream();
                List<String> lines = stream == null ? emptyList()
                        : new BufferedReader(new InputStreamReader(stream)).lines().collect(toList());
                return Stream.concat(lines.stream().filter(line -> !line.endsWith(".class"))
                        .map(subPackage -> allTypesIn(packageName + "." + subPackage))
                        .flatMap(List::stream), lines.stream().filter(line -> line.endsWith(".class"))
                        .map(line -> toClass(line, packageName))).collect(toList());
            }
        }, emptyList());
    }

    private static Class<?> toClass(String className, String packageName) {
        return Sneaky.get(() -> Class.forName(packageName + "." + className.substring(0, className.lastIndexOf('.'))));
    }

    public static <T> List<Class<? extends T>> subTypesOf(Class<T> superClass, String packageName) {
        return assignableTypesOf(superClass, packageName).stream().filter(c -> !superClass.equals(c))
                .map(c -> (Class<? extends T>) c)
                .collect(toList());
    }

    @SuppressWarnings("unchecked")
    public static <T> List<Class<? extends T>> assignableTypesOf(Class<T> superClass, String packageName) {
        return allTypesIn(packageName).stream().filter(superClass::isAssignableFrom)
                .map(c -> (Class<? extends T>) c)
                .collect(toList());
    }

    public static int compareByExtends(Class<?> type1, Class<?> type2) {
        return type1.equals(type2) ? 0 : type1.isAssignableFrom(type2) ? 1 : -1;
    }

    @SuppressWarnings("unchecked")
    public static <T> T newInstance(Class<T> type, Object... args) {
        return Sneaky.get(() -> (T) chooseConstructor(type, args).newInstance(args));
    }

    private static <T> Constructor<?> chooseConstructor(Class<T> type, Object[] args) {
        List<Constructor<?>> constructors = Stream.of(type.getConstructors())
                .filter(c -> isProperConstructor(c, args))
                .collect(toList());
        if (constructors.size() != 1)
            throw new NoAppropriateConstructorException(type, args);
        return constructors.get(0);
    }

    private static boolean isProperConstructor(Constructor<?> constructor, Object[] parameters) {
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        return parameterTypes.length == parameters.length && IntStream.range(0, parameterTypes.length)
                .allMatch(i -> parameterTypes[i].isInstance(parameters[i]));
    }

    public static String getClassName(Object object) {
        return object == null ? null : object.getClass().getName();
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<T> named(Class<T> type) {
        return type.getInterfaces().length > 0 && (type.isAnonymousClass() && type.getSuperclass() == Object.class
                || type.isSynthetic()
                || Proxy.isProxyClass(type)) ? (Class<T>) type.getInterfaces()[0] : type;
    }

    public static boolean isReflective(Class<?> type) {
        if (!Modifier.isPublic(type.getModifiers()) || type.isAnonymousClass() || type.isLocalClass() || type.isSynthetic())
            return false;
        Class<?> enclosing = type.getEnclosingClass();
        return enclosing == null || isReflective(enclosing);
    }

    public static <T, X> boolean equals(T self, Object other, Class<X> type, BiPredicate<T, X> predicate) {
        return self == other || type.isInstance(other) && predicate.test(self, type.cast(other));
    }
}
