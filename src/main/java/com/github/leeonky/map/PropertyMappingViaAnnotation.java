package com.github.leeonky.map;

import com.github.leeonky.util.Property;
import ma.glasnost.orika.metadata.ClassMapBuilder;

abstract class PropertyMappingViaAnnotation {
    protected final Property<?> property;
    final Mapper mapper;

    PropertyMappingViaAnnotation(Mapper mapper, Property<?> property) {
        this.mapper = mapper;
        this.property = property;
    }

    static PropertyMappingViaAnnotation create(Mapper mapper, Property<?> property) {
        MappingView mappingView = property.getAnnotation(MappingView.class);
        FromProperty fromProperty = property.getAnnotation(FromProperty.class);
        if (mappingView != null) {
            if (fromProperty != null) {
                FromPropertyWrapper fromPropertyWrapper = new FromPropertyWrapper(fromProperty);
                if (fromPropertyWrapper.isFlatFromList())
                    return new MappingThroughViewAndFromPropertyToElement(mapper, property, mappingView, fromPropertyWrapper);
                else if (fromPropertyWrapper.isFlatFromMap())
                    return new MappingThroughViewAndFromPropertyToMap(mapper, property, mappingView, fromPropertyWrapper);
            } else
                return new MappingThroughView(mapper, property, mappingView);
        } else {
            if (fromProperty != null) {
                FromPropertyWrapper fromPropertyWrapper = new FromPropertyWrapper(fromProperty);
                if (fromPropertyWrapper.isFlatFromList())
                    return new MappingThroughFromPropertyToElement(mapper, property, fromPropertyWrapper);
                else if (fromPropertyWrapper.isFlatFromMap())
                    return new MappingThroughFromPropertyToMap(mapper, property, fromPropertyWrapper);
                else
                    return new MappingThroughFromProperty(mapper, property, fromPropertyWrapper);
            }
        }
        return null;
    }

    public abstract ClassMapBuilder<?, ?> configMapping(ClassMapBuilder<?, ?> classMapBuilder);
}


class MappingThroughView extends PropertyMappingViaAnnotation {
    private final MappingView mappingView;

    MappingThroughView(Mapper mapper, Property<?> property, MappingView mappingView) {
        super(mapper, property);
        this.mappingView = mappingView;
    }

    @Override
    public ClassMapBuilder<?, ?> configMapping(ClassMapBuilder<?, ?> classMapBuilder) {
        return classMapBuilder.fieldMap(property.getName(), property.getName())
                .converter(mapper.registerConverter(new ViewConverter(mapper, mappingView.value())))
                .add();
    }

}

class MappingThroughFromProperty extends PropertyMappingViaAnnotation {
    protected final FromPropertyWrapper fromPropertyWrapper;

    MappingThroughFromProperty(Mapper mapper, Property<?> property, FromPropertyWrapper fromPropertyWrapper) {
        super(mapper, property);
        this.fromPropertyWrapper = fromPropertyWrapper;
    }

    @Override
    public ClassMapBuilder<?, ?> configMapping(ClassMapBuilder<?, ?> classMapBuilder) {
        return classMapBuilder.field(fromPropertyWrapper.value.original(), property.getName());
    }
}

class MappingThroughFromPropertyToElement extends MappingThroughFromProperty {

    MappingThroughFromPropertyToElement(Mapper mapper, Property<?> property, FromPropertyWrapper fromPropertyWrapper) {
        super(mapper, property, fromPropertyWrapper);
    }

    @Override
    public ClassMapBuilder<?, ?> configMapping(ClassMapBuilder<?, ?> classMapBuilder) {
        return classMapBuilder.field(fromPropertyWrapper.value.original(), property.getName() + "{}");
    }
}

class MappingThroughFromPropertyToMap extends MappingThroughFromProperty {

    MappingThroughFromPropertyToMap(Mapper mapper, Property<?> property, FromPropertyWrapper fromPropertyWrapper) {
        super(mapper, property, fromPropertyWrapper);
    }

    @Override
    public ClassMapBuilder<?, ?> configMapping(ClassMapBuilder<?, ?> classMapBuilder) {
        return classMapBuilder.field(fromPropertyWrapper.key.original(), property.getName() + "{key}")
                .field(fromPropertyWrapper.value.original(), property.getName() + "{value}");
    }
}

class MappingThroughViewAndFromProperty extends MappingThroughFromProperty {
    protected final MappingView mappingView;

    MappingThroughViewAndFromProperty(Mapper mapper, Property<?> property, MappingView mappingView, FromPropertyWrapper fromPropertyWrapper) {
        super(mapper, property, fromPropertyWrapper);
        this.mappingView = mappingView;
    }
}

class MappingThroughViewAndFromPropertyToElement extends MappingThroughViewAndFromProperty {
    MappingThroughViewAndFromPropertyToElement(Mapper mapper, Property<?> property, MappingView mappingView, FromPropertyWrapper fromPropertyWrapper) {
        super(mapper, property, mappingView, fromPropertyWrapper);
    }

    @Override
    public ClassMapBuilder<?, ?> configMapping(ClassMapBuilder<?, ?> classMapBuilder) {
        return classMapBuilder.fieldMap(fromPropertyWrapper.value.name, property.getName())
                .converter(mapper.registerConverter(fromPropertyWrapper.createViewListPropertyConverter(mapper, mappingView.value())))
                .add();
    }

}

class MappingThroughViewAndFromPropertyToMap extends MappingThroughViewAndFromPropertyToElement {

    MappingThroughViewAndFromPropertyToMap(Mapper mapper, Property<?> property, MappingView mappingView, FromPropertyWrapper fromPropertyWrapper) {
        super(mapper, property, mappingView, fromPropertyWrapper);
    }

    @Override
    public ClassMapBuilder<?, ?> configMapping(ClassMapBuilder<?, ?> classMapBuilder) {
        if (fromPropertyWrapper.isDifferentSourceProperty())
            throw new IllegalArgumentException("Key and Value source property should be same");
        return classMapBuilder.fieldMap(fromPropertyWrapper.value.name, property.getName())
                .converter(mapper.registerConverter(fromPropertyWrapper.createViewMapPropertyConverter(mapper, mappingView.value())))
                .add();
    }

}

class ElementProperty {
    final String name, elementName;

    ElementProperty(String content) {
        String[] strings = content.split("\\{");
        name = strings[0].trim();
        elementName = strings.length == 2 ? strings[1].replace("}", "").trim() : null;
    }

    String original() {
        return elementName == null ? name : name + "{" + elementName + "}";
    }
}

class FromPropertyWrapper {
    final ElementProperty key, value;

    FromPropertyWrapper(FromProperty fromProperty) {
        value = new ElementProperty(fromProperty.value());
        key = fromProperty.key().isEmpty() ? null : new ElementProperty(fromProperty.key());
    }

    boolean isFlatFromList() {
        return value.elementName != null && (key == null || key.elementName == null);
    }

    boolean isFlatFromMap() {
        return value.elementName != null && key != null && key.elementName != null;
    }

    boolean isDifferentSourceProperty() {
        return !value.name.equals(key.name);
    }

    ViewMapPropertyConverter createViewMapPropertyConverter(Mapper mapper, Class<?> view) {
        return new ViewMapPropertyConverter(mapper, view, key.elementName, value.elementName);
    }

    ViewListPropertyConverter createViewListPropertyConverter(Mapper mapper, Class<?> view) {
        return new ViewListPropertyConverter(mapper, view, value.elementName);
    }
}
