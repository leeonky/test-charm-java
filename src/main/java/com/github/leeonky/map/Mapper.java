package com.github.leeonky.map;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.metadata.ClassMapBuilder;
import org.reflections.Reflections;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Mapper {
    private Map<Class<?>, Map<Class<?>, Map<Class<?>, Class<?>>>> mappings = new HashMap<>();
    private MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
    private Class<?> scope = void.class;

    public Mapper(String... packages) {
        Reflections reflections = new Reflections((Object[]) packages);
        reflections.getTypesAnnotatedWith(Mapping.class).forEach(this::scanClass);
        reflections.getTypesAnnotatedWith(MappingFrom.class).forEach(this::scanClass);
    }

    private void scanClass(Class<?> clazz) {
        Mapping mapping = clazz.getAnnotation(Mapping.class);
        MappingFrom mappingFrom = clazz.getAnnotation(MappingFrom.class);
        MappingView mappingView = clazz.getAnnotation(MappingView.class);
        MappingScope mappingScope = clazz.getAnnotation(MappingScope.class);
        Class<?>[] froms = mapping == null ? mappingFrom.value() : mapping.from();
        Class<?> view = mappingView != null ? mappingView.value() : (mapping != null ? mapping.view() : clazz);
        Class<?> scope = mappingScope != null ? mappingScope.value() : (mapping == null ? void.class : mapping.scope());
        for (Class<?> from : froms) {
            mappings.computeIfAbsent(from, f -> new HashMap<>())
                    .computeIfAbsent(view, f -> new HashMap<>())
                    .put(scope, clazz);
            configMapping(from, clazz);
        }
    }

    private void configMapping(Class<?> from, Class<?> to) {
        List<Field> nestedField = Stream.of(to.getFields()).filter(f -> f.getAnnotation(MappingView.class) != null)
                .collect(Collectors.toList());
        if (!nestedField.isEmpty()) {
            ClassMapBuilder<?, ?> classMapBuilder = mapperFactory.classMap(from, to);
            nestedField.forEach(f -> {
                Class<?> view = f.getAnnotation(MappingView.class).value();
                String converterId = String.format("%s[%d]", view.getName(), hashCode());
                if (mapperFactory.getConverterFactory().getConverter(converterId) == null)
                    mapperFactory.getConverterFactory().registerConverter(converterId, new ViewConverter(this, view));
                classMapBuilder.fieldMap(f.getName(), f.getName()).converter(converterId).add();
            });
            classMapBuilder.byDefault().register();
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T map(Object from, Class<?> view) {
        return (T) mapperFactory.getMapperFacade().map(from, getTargetClass(from, view));
    }

    private Class<?> getTargetClass(Object from, Class<?> view) {
        Map<Class<?>, Class<?>> scopeMapping = mappings.get(from.getClass()).get(view);
        Class<?> to = scopeMapping.get(scope);
        if (to == null)
            to = scopeMapping.get(void.class);
        return to;
    }

    public void setScope(Class<?> scope) {
        this.scope = scope;
    }
}
