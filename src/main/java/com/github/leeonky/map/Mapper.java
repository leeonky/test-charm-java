package com.github.leeonky.map;

import com.github.leeonky.util.BeanClass;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.metadata.ClassMapBuilder;
import ma.glasnost.orika.metadata.MapperKey;
import org.reflections.Reflections;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ma.glasnost.orika.metadata.TypeFactory.valueOf;

public class Mapper {
    private static final Class[] EMPTY_CLASS_ARRAY = new Class[0];
    private static final Class<?>[] VOID_SCOPES = {void.class};
    private final Class[] annotations = new Class[]{Mapping.class, MappingFrom.class, MappingView.class, MappingScope.class};
    private MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
    private MappingRegisterData mappingRegisterData = new MappingRegisterData();
    private Class<?> scope = void.class;

    public Mapper(String... packages) {
        collectAllClasses(packages).forEach(this::register);
    }

    static <T> T NotSupportParallelStreamReduce(T u1, T u2) {
        throw new IllegalStateException("Not support parallel stream");
    }

    static Class<?>[] guessValueInSequence(Class<?> mapTo, Class[] defaultReturn, Function<Class<?>, Class<?>[]>... functions) {
        return Stream.<Function<Class<?>, Class<?>[]>>of(functions)
                .map(f -> f.apply(mapTo))
                .filter(froms -> froms.length != 0)
                .findFirst().orElse(defaultReturn);
    }

    private void register(Class<?> mapTo) {
        for (Class<?> view : getViews(mapTo))
            for (Class<?> from : getFroms(mapTo)) {
                for (Class<?> scope : getScopes(mapTo, VOID_SCOPES))
                    mappingRegisterData.register(from, view, scope, mapTo);
                configNonDefaultMapping(from, mapTo);
            }
        for (Class<?> nested : mapTo.getDeclaredClasses())
            register(nested);
    }

    private Set<Class<?>> collectAllClasses(Object[] packages) {
        Reflections reflections = new Reflections(packages);
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Mapping.class);
        classes.addAll(reflections.getTypesAnnotatedWith(MappingFrom.class));
        return classes;
    }

    private Class<?>[] getScopes(Class<?> mapTo, Class<?>[] defaultReturn) {
        return guessValueInSequence(mapTo, defaultReturn,
                this::getScopeFromMappingFrom,
                this::getScopeFromMapping,
                this::getScopeFromDeclaring,
                this::getScopeFromSuper);
    }

    private Class<?>[] getScopeFromMapping(Class<?> mapTo) {
        Mapping declaredMapping = mapTo.getDeclaredAnnotation(Mapping.class);
        if (declaredMapping != null)
            return declaredMapping.scope();
        return EMPTY_CLASS_ARRAY;
    }

    private Class<?>[] getScopeFromMappingFrom(Class<?> mapTo) {
        MappingScope declaredMappingScope = mapTo.getDeclaredAnnotation(MappingScope.class);
        if (declaredMappingScope != null)
            return declaredMappingScope.value();
        return EMPTY_CLASS_ARRAY;
    }

    private Class<?>[] getScopeFromDeclaring(Class<?> mapTo) {
        Class<?> declaringClass = mapTo.getDeclaringClass();
        if (declaringClass != null)
            return getScopes(declaringClass, EMPTY_CLASS_ARRAY);
        return EMPTY_CLASS_ARRAY;
    }

    private Class<?>[] getScopeFromSuper(Class<?> mapTo) {
        Class<?> superclass = mapTo.getSuperclass();
        if (superclass != null)
            return getScopes(superclass, EMPTY_CLASS_ARRAY);
        return EMPTY_CLASS_ARRAY;
    }

    private Class<?>[] getViews(Class<?> mapTo) {
        Mapping mapping = mapTo.getDeclaredAnnotation(Mapping.class);
        MappingView mappingView = mapTo.getDeclaredAnnotation(MappingView.class);
        return mappingView != null ? new Class[]{mappingView.value()} : (mapping != null ? mapping.view() : new Class[]{mapTo});
    }

    private Class<?>[] getFroms(Class<?> mapTo) {
        return guessValueInSequence(mapTo, EMPTY_CLASS_ARRAY,
                this::getFromFromMappingFrom,
                this::getFromFromMapping,
                this::getFromFromDeclaring,
                this::getFromFromSuper);
    }

    private Class<?>[] getFromFromMapping(Class<?> mapTo) {
        Mapping declaredMapping = mapTo.getDeclaredAnnotation(Mapping.class);
        if (declaredMapping != null)
            return declaredMapping.from();
        return EMPTY_CLASS_ARRAY;
    }

    private Class<?>[] getFromFromMappingFrom(Class<?> mapTo) {
        MappingFrom declaredMappingFrom = mapTo.getDeclaredAnnotation(MappingFrom.class);
        if (declaredMappingFrom != null)
            return declaredMappingFrom.value();
        return EMPTY_CLASS_ARRAY;
    }

    private Class<?>[] getFromFromDeclaring(Class<?> mapTo) {
        Class<?> declaringClass = mapTo.getDeclaringClass();
        if (declaringClass != null)
            return getFroms(declaringClass);
        return EMPTY_CLASS_ARRAY;
    }

    private Class<?>[] getFromFromSuper(Class<?> mapTo) {
        Class<?> superclass = mapTo.getSuperclass();
        if (superclass != null)
            return getFroms(superclass);
        return EMPTY_CLASS_ARRAY;
    }

    private void configNonDefaultMapping(Class<?> mapFrom, Class<?> mapTo) {
        List<PropertyNonDefaultMapping> propertyNonDefaultMappings = collectNonDefaultProperties(mapTo);
        if (!propertyNonDefaultMappings.isEmpty())
            propertyNonDefaultMappings.stream()
                    .reduce(prepareConfigMapping(mapFrom, mapTo), (builder, mapping) -> mapping.configMapping(builder),
                            Mapper::NotSupportParallelStreamReduce)
                    .byDefault().register();
    }

    private List<PropertyNonDefaultMapping> collectNonDefaultProperties(Class<?> mapTo) {
        return BeanClass.create(mapTo).getPropertyWriters().values().stream()
                .map(property -> PropertyNonDefaultMapping.create(this, property))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private ClassMapBuilder prepareConfigMapping(Class<?> mapFrom, Class<?> mapTo) {
        explicitRegisterSupperClassesWithDefaultMapping(mapFrom, mapTo.getSuperclass());
        return mapperFactory.classMap(mapFrom, mapTo);
    }

    @SuppressWarnings("unchecked")
    private void explicitRegisterSupperClassesWithDefaultMapping(Class<?> mapFrom, Class<?> mapTo) {
        if (Stream.of(annotations).anyMatch(a -> mapTo.getAnnotation(a) != null)) {
            if (mapperFactory.getClassMap(new MapperKey(valueOf(mapFrom), valueOf(mapTo))) == null)
                mapperFactory.classMap(mapFrom, mapTo).byDefault().register();
            explicitRegisterSupperClassesWithDefaultMapping(mapFrom, mapTo.getSuperclass());
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T map(Object source, Class<?> view) {
        if (source == null) return null;
        return findMapping(source, view).map(t -> (T) mapTo(source, t)).orElse(null);
    }

    public <T> T mapTo(Object source, Class<T> t) {
        return mapperFactory.getMapperFacade().map(source, t);
    }

    public Optional<Class<?>> findMapping(Object from, Class<?> view) {
        return mappingRegisterData.findMapTo(from, view, scope);
    }

    public void setScope(Class<?> scope) {
        this.scope = scope;
    }

    @Deprecated
    public List<Class<?>> findSubMappings(Class<?> mapTo, Class<?> view) {
        return mappingRegisterData.findAllSubMapTo(mapTo, view, scope);
    }

    public String registerConverter(BaseConverter converter) {
        String converterId = converter.buildConvertId();
        if (mapperFactory.getConverterFactory().getConverter(converterId) == null)
            mapperFactory.getConverterFactory().registerConverter(converterId, converter);
        return converterId;
    }
}
