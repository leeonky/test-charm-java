package com.github.leeonky.map;

import com.github.leeonky.util.BeanClass;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.metadata.ClassMapBuilder;
import ma.glasnost.orika.metadata.MapperKey;
import org.reflections.Reflections;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static ma.glasnost.orika.metadata.TypeFactory.valueOf;

public class Mapper {
    private final Class[] annotations = new Class[]{Mapping.class, MappingFrom.class, MappingView.class, MappingScope.class};
    private Map<Class<?>, Map<Class<?>, Map<Class<?>, Class<?>>>> sourceViewScopeMappingMap = new HashMap<>();
    private Map<Class<?>, Map<Class<?>, List<Class<?>>>> viewScopeMappingListMap = new HashMap<>();
    private MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
    private Class<?> scope = void.class;

    public Mapper(String... packages) {
        Reflections reflections = new Reflections((Object[]) packages);
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Mapping.class);
        classes.addAll(reflections.getTypesAnnotatedWith(MappingFrom.class));
        classes.forEach(this::registerMapping);
    }

    public MapperFactory getMapperFactory() {
        return mapperFactory;
    }

    private void registerMapping(Class<?> clazz) {
        for (Class<?> view : getViews(clazz))
            for (Class<?> from : getFroms(clazz))
                processMappingInScopeAndConfigPropertyMapping(clazz, from,
                        sourceViewScopeMappingMap.computeIfAbsent(from, f -> new HashMap<>()).computeIfAbsent(view, f -> new HashMap<>()),
                        viewScopeMappingListMap.computeIfAbsent(view, f1 -> new HashMap<>()));
    }

    private void processMappingInScopeAndConfigPropertyMapping(Class<?> clazz, Class<?> from, Map<Class<?>, Class<?>> sourceViewMap, Map<Class<?>, List<Class<?>>> viewMap) {
        for (Class<?> scope : getScopes(clazz)) {
            sourceViewMap.put(scope, clazz);
            viewMap.computeIfAbsent(scope, f -> new ArrayList<>()).add(clazz);
        }
        configPropertyMapping(from, clazz);
    }

    private Class<?>[] getScopes(Class<?> clazz) {
        Mapping mapping = clazz.getAnnotation(Mapping.class);
        MappingScope mappingScope = clazz.getAnnotation(MappingScope.class);
        Class<?>[] scopes = mappingScope != null ? mappingScope.value() : (mapping == null ? null : mapping.scope());
        if (scopes == null || scopes.length == 0)
            scopes = new Class<?>[]{void.class};
        return scopes;
    }

    private Class<?>[] getViews(Class<?> clazz) {
        Mapping mapping = clazz.getAnnotation(Mapping.class);
        MappingView mappingView = clazz.getAnnotation(MappingView.class);
        return mappingView != null ? new Class[]{mappingView.value()} : (mapping != null ? mapping.view() : new Class[]{clazz});
    }

    private Class<?>[] getFroms(Class<?> clazz) {
        MappingFrom mappingFrom = clazz.getAnnotation(MappingFrom.class);
        return mappingFrom != null ? mappingFrom.value() : clazz.getAnnotation(Mapping.class).from();
    }

    private void configPropertyMapping(Class<?> from, Class<?> to) {
        List<PropertyMappingViaAnnotation> propertyMappingViaAnnotations = BeanClass.create(to).getPropertyWriters().values().stream()
                .map(property -> PropertyMappingViaAnnotation.create(this, property))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (!propertyMappingViaAnnotations.isEmpty())
            propertyMappingViaAnnotations.stream().reduce(prepareConfigMapping(from, to), (builder, mapping) -> mapping.configMapping(builder), (u1, u2) -> {
                throw new IllegalStateException("Not support parallel stream");
            }).byDefault().register();
    }

    private ClassMapBuilder prepareConfigMapping(Class<?> from, Class<?> to) {
        explicitRegisterSupperClasses(from, to.getSuperclass());
        return mapperFactory.classMap(from, to);
    }

    private void explicitRegisterSupperClasses(Class<?> from, Class<?> to) {
        if (Stream.of(annotations).anyMatch(a -> to.getAnnotation(a) != null)) {
            if (mapperFactory.getClassMap(new MapperKey(valueOf(from), valueOf(to))) == null)
                mapperFactory.classMap(from, to).byDefault().register();
            explicitRegisterSupperClasses(from, to.getSuperclass());
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
        Map<Class<?>, Class<?>> scopeMapping = sourceViewScopeMappingMap.getOrDefault(from.getClass(), emptyMap())
                .getOrDefault(view, emptyMap());
        Class<?> to = scopeMapping.get(scope);
        return Optional.ofNullable(to != null ? to : scopeMapping.get(void.class));
    }

    public void setScope(Class<?> scope) {
        this.scope = scope;
    }

    @Deprecated
    public List<Class<?>> findSubMappings(Class<?> baseMapping, Class<?> view) {
        Map<Class<?>, List<Class<?>>> scopeDestListMap = viewScopeMappingListMap.getOrDefault(view, emptyMap());
        return Stream.concat(scopeDestListMap.getOrDefault(scope, emptyList()).stream(),
                scopeDestListMap.getOrDefault(void.class, emptyList()).stream())
                .filter(baseMapping::isAssignableFrom)
                .collect(Collectors.toList());
    }

    public String registerConverter(ViewConverter converter) {
        String converterId = converter.buildConvertId();
        if (getMapperFactory().getConverterFactory().getConverter(converterId) == null)
            getMapperFactory().getConverterFactory().registerConverter(converterId, converter);
        return converterId;
    }
}
