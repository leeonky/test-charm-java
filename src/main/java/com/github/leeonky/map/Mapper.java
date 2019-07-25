package com.github.leeonky.map;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.metadata.ClassMapBuilder;
import ma.glasnost.orika.metadata.MapperKey;
import org.reflections.Reflections;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        classes.forEach(this::register);
    }

    private void register(Class<?> clazz) {
        Mapping mapping = clazz.getAnnotation(Mapping.class);
        MappingFrom mappingFrom = clazz.getAnnotation(MappingFrom.class);
        MappingView mappingView = clazz.getAnnotation(MappingView.class);
        MappingScope mappingScope = clazz.getAnnotation(MappingScope.class);
        Class<?>[] froms = mappingFrom != null ? mappingFrom.value() : mapping.from();
        Class<?> view = mappingView != null ? mappingView.value() : (mapping != null ? mapping.view() : clazz);
        Class<?>[] scopes = mappingScope != null ? mappingScope.value() : (mapping == null ? null : mapping.scope());
        if (scopes == null || scopes.length == 0)
            scopes = new Class<?>[]{void.class};
        for (Class<?> from : froms) {
            Map<Class<?>, Class<?>> sourceViewMap = sourceViewScopeMappingMap.computeIfAbsent(from, f -> new HashMap<>())
                    .computeIfAbsent(view, f -> new HashMap<>());
            for (Class<?> scope : scopes)
                sourceViewMap.put(scope, clazz);

            Map<Class<?>, List<Class<?>>> viewMap = viewScopeMappingListMap.computeIfAbsent(view, f -> new HashMap<>());
            for (Class<?> scope : scopes)
                viewMap.computeIfAbsent(scope, f -> new ArrayList<>()).add(clazz);
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
            registerAllSuppers(from, to.getSuperclass());
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

    private void registerAllSuppers(Class<?> from, Class<?> to) {
        if (Stream.of(annotations).anyMatch(a -> to.getAnnotation(a) != null)) {
            if (mapperFactory.getClassMap(new MapperKey(valueOf(from), valueOf(to))) == null)
                mapperFactory.classMap(from, to).byDefault().register();
            registerAllSuppers(from, to.getSuperclass());
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T map(Object source, Class<?> view) {
        return findMapping(source, view).map(t -> (T) mapTo(source, t)).orElse(null);
    }

    public <T> T mapTo(Object source, Class<T> t) {
        return mapperFactory.getMapperFacade().map(source, t);
    }

    public Optional<Class<?>> findMapping(Object from, Class<?> view) {
        if (from == null)
            return Optional.empty();
        Map<Class<?>, Class<?>> scopeMapping = sourceViewScopeMappingMap.getOrDefault(from.getClass(), new HashMap<>())
                .getOrDefault(view, new HashMap<>());
        Class<?> to = scopeMapping.get(scope);
        if (to == null)
            to = scopeMapping.get(void.class);
        return Optional.ofNullable(to);
    }

    public void setScope(Class<?> scope) {
        this.scope = scope;
    }

    public List<Class<?>> findSubMappings(Class<?> baseMapping, Class<?> view) {
        Map<Class<?>, List<Class<?>>> scopeDestListMap = viewScopeMappingListMap.getOrDefault(view, new HashMap<>());
        return Stream.concat(scopeDestListMap.getOrDefault(scope, new ArrayList<>()).stream(),
                scopeDestListMap.getOrDefault(void.class, new ArrayList<>()).stream())
                .filter(baseMapping::isAssignableFrom)
                .collect(Collectors.toList());
    }
}
