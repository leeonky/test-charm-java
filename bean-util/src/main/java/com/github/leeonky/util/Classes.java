package com.github.leeonky.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.github.leeonky.util.function.Extension.getFirstPresent;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class Classes {
    public static List<Class<?>> allTypesIn(String packageName) {
        return new ArrayList<>(new HashSet<Class<?>>() {{
            try {
                Enumeration<URL> resources = getClassLoader().getResources(packageName.replaceAll("[.]", "/"));
                while (resources.hasMoreElements())
                    addAll(getClasses(packageName, resources.nextElement()));
            } catch (Exception ignore) {
            }
        }});
    }

    private static ClassLoader getClassLoader() {
        return getFirstPresent(() -> classLoader(Thread.currentThread()::getContextClassLoader),
                () -> classLoader(Classes.class::getClassLoader),
                () -> classLoader(ClassLoader::getSystemClassLoader))
                .orElseThrow(IllegalStateException::new);
    }

    private static Optional<ClassLoader> classLoader(Supplier<ClassLoader> factory) {
        ClassLoader classLoader = null;
        try {
            classLoader = factory.get();
        } catch (Throwable ignore) {
        }
        return Optional.ofNullable(classLoader);
    }

    private static List<Class<?>> getClasses(String packageName, URL resource) {
        try {
            if ("jar".equals(resource.getProtocol()))
                return ((JarURLConnection) resource.openConnection()).getJarFile().stream()
                        .map(jarEntry -> jarEntry.getName().replace('/', '.'))
                        .filter(name -> name.endsWith(".class") && name.startsWith(packageName))
                        .map(name -> Sneaky.get(() -> Class.forName(name.substring(0, name.length() - 6))))
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
        } catch (Exception ignore) {
            return emptyList();
        }
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
    public static <T> Class<? super T> named(Class<T> type) {
        return type.getInterfaces().length > 0 && (type.isAnonymousClass() && type.getSuperclass() == Object.class
                || type.isSynthetic()) ? (Class<? super T>) type.getInterfaces()[0] : type;
    }
}
