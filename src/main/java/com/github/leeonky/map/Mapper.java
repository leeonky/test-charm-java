package com.github.leeonky.map;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.metadata.ClassMapBuilder;
import org.reflections.Reflections;

import java.lang.reflect.Field;
import java.util.*;
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
        List<Field> fromPropertyField = Stream.of(to.getFields()).filter(f -> f.getAnnotation(FromProperty.class) != null)
                .collect(Collectors.toList());

        Set<Field> fields = new HashSet<>();
        fields.addAll(nestedField);
        fields.addAll(fromPropertyField);

        if (!fields.isEmpty()) {
            ClassMapBuilder<?, ?> classMapBuilder = mapperFactory.classMap(from, to);
            for (Field field : fields) {
                MappingView mappingView = field.getAnnotation(MappingView.class);
                FromProperty fromProperty = field.getAnnotation(FromProperty.class);
                if (mappingView != null && fromProperty == null) {
                    Class<?> view = mappingView.value();
                    String converterId = String.format("ViewConverter:%s[%d]", view.getName(), hashCode());
                    if (mapperFactory.getConverterFactory().getConverter(converterId) == null)
                        mapperFactory.getConverterFactory().registerConverter(converterId, new ViewConverter(this, view));
                    classMapBuilder = classMapBuilder.fieldMap(field.getName(), field.getName()).converter(converterId).add();
                }
                if (mappingView == null && fromProperty != null) {
                    FromProperty annotation = field.getAnnotation(FromProperty.class);
                    if (annotation.toElement())
                        classMapBuilder = classMapBuilder.field(annotation.value(), field.getName() + "{}");
                    else if (annotation.toMapEntry()) {
                        classMapBuilder = classMapBuilder.field(annotation.key(), field.getName() + "{key}")
                                .field(annotation.value(), field.getName() + "{value}");
                    } else
                        classMapBuilder = classMapBuilder.field(annotation.value(), field.getName());
                }
                if (mappingView != null && fromProperty != null) {
                    Class<?> view = mappingView.value();
                    FromProperty annotation = field.getAnnotation(FromProperty.class);
                    if (annotation.toElement()) {
                        String[] strings = annotation.value().split("\\{");
                        String sourceFieldName = strings[0];
                        String property = strings[1].replace("}", "").trim();
                        String converterId = String.format("ViewListPropertyConverter:%s:%s[%d]", property, view.getName(), hashCode());
                        if (mapperFactory.getConverterFactory().getConverter(converterId) == null)
                            mapperFactory.getConverterFactory().registerConverter(converterId, new ViewListPropertyConverter(this, view, property));
                        classMapBuilder = classMapBuilder.fieldMap(sourceFieldName, field.getName()).converter(converterId).add();
                    } else if (annotation.toMapEntry()) {
                        String[] strings = annotation.value().split("\\{");
                        String valueFieldName = strings[0];
                        String valueProperty = strings[1].replace("}", "").trim();
                        strings = annotation.key().split("\\{");
                        String keyFieldName = strings[0];
                        String keyProperty = strings[1].replace("}", "").trim();
                        String converterId = String.format("ViewMapPropertyConverter:%s:%s:%s[%d]", keyProperty, valueProperty, view.getName(), hashCode());
                        if (!valueFieldName.equals(keyFieldName))
                            throw new IllegalArgumentException("Key and Value source property should be same");
                        if (mapperFactory.getConverterFactory().getConverter(converterId) == null)
                            mapperFactory.getConverterFactory().registerConverter(converterId, new ViewMapPropertyConverter(this, view, keyProperty, valueProperty));
                        classMapBuilder = classMapBuilder.fieldMap(valueFieldName, field.getName()).converter(converterId).add();
                    }
                }
            }
            classMapBuilder.byDefault().register();
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T map(Object from, Class<?> view) {
        Class<?> targetClass = getTargetClass(from, view);
        return targetClass == null ? null : (T) mapperFactory.getMapperFacade().map(from, targetClass);
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
