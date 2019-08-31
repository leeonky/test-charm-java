package com.github.leeonky.map;

import com.github.leeonky.util.Property;
import ma.glasnost.orika.metadata.ClassMapBuilder;

abstract class PropertyNonDefaultMapping {
    protected final Property<?> property;
    final Mapper mapper;

    PropertyNonDefaultMapping(Mapper mapper, Property<?> property) {
        this.mapper = mapper;
        this.property = property;
    }

    static PropertyNonDefaultMapping create(Mapper mapper, Property<?> property) {
        MappingView mappingView = property.getAnnotation(MappingView.class);
        FromProperty fromProperty = property.getAnnotation(FromProperty.class);
        if (mappingView != null) {
            if (fromProperty != null) {
                FromPropertyWrapper fromPropertyWrapper = new FromPropertyWrapper(fromProperty);
                if (fromPropertyWrapper.isFlatToCollection())
                    return new MapThroughViewAndFromPropertyToCollection(mapper, property, mappingView, fromPropertyWrapper);
                else if (fromPropertyWrapper.isFlatToMap())
                    return new MapThroughViewAndFromPropertyToMap(mapper, property, mappingView, fromPropertyWrapper);
            } else
                return new MapThroughView(mapper, property, mappingView);
        } else {
            if (fromProperty != null) {
                FromPropertyWrapper fromPropertyWrapper = new FromPropertyWrapper(fromProperty);
                if (fromPropertyWrapper.isFlatToCollection())
                    return new MapThroughFromPropertyToCollection(mapper, property, fromPropertyWrapper);
                else if (fromPropertyWrapper.isFlatToMap())
                    return new MapThroughFromPropertyToMap(mapper, property, fromPropertyWrapper);
                else
                    return new MapThroughFromProperty(mapper, property, fromPropertyWrapper);
            }
        }
        return null;
    }

    public abstract ClassMapBuilder<?, ?> configMapping(ClassMapBuilder<?, ?> classMapBuilder);
}


class MapThroughView extends PropertyNonDefaultMapping {
    private final MappingView mappingView;

    MapThroughView(Mapper mapper, Property<?> property, MappingView mappingView) {
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

class MapThroughFromProperty extends PropertyNonDefaultMapping {
    protected final FromPropertyWrapper fromPropertyWrapper;

    MapThroughFromProperty(Mapper mapper, Property<?> property, FromPropertyWrapper fromPropertyWrapper) {
        super(mapper, property);
        this.fromPropertyWrapper = fromPropertyWrapper;
    }

    @Override
    public ClassMapBuilder<?, ?> configMapping(ClassMapBuilder<?, ?> classMapBuilder) {
        return classMapBuilder.field(fromPropertyWrapper.value.original(), property.getName());
    }
}

class MapThroughFromPropertyToCollection extends MapThroughFromProperty {

    MapThroughFromPropertyToCollection(Mapper mapper, Property<?> property, FromPropertyWrapper fromPropertyWrapper) {
        super(mapper, property, fromPropertyWrapper);
    }

    @Override
    public ClassMapBuilder<?, ?> configMapping(ClassMapBuilder<?, ?> classMapBuilder) {
        return classMapBuilder.fieldMap(fromPropertyWrapper.value.name, property.getName())
                .converter(mapper.registerConverter(fromPropertyWrapper.createListPropertyConverter(mapper, property.getName())))
                .add();
    }
}

class MapThroughFromPropertyToMap extends MapThroughFromProperty {

    MapThroughFromPropertyToMap(Mapper mapper, Property<?> property, FromPropertyWrapper fromPropertyWrapper) {
        super(mapper, property, fromPropertyWrapper);
    }

    @Override
    public ClassMapBuilder<?, ?> configMapping(ClassMapBuilder<?, ?> classMapBuilder) {
        return classMapBuilder.fieldMap(fromPropertyWrapper.value.name, property.getName())
                .converter(mapper.registerConverter(fromPropertyWrapper.createMapPropertyConverter(mapper, property.getName())))
                .add();
    }
}

class MapThroughViewAndFromProperty extends MapThroughFromProperty {
    protected final MappingView mappingView;

    MapThroughViewAndFromProperty(Mapper mapper, Property<?> property, MappingView mappingView, FromPropertyWrapper fromPropertyWrapper) {
        super(mapper, property, fromPropertyWrapper);
        this.mappingView = mappingView;
    }
}

class MapThroughViewAndFromPropertyToCollection extends MapThroughViewAndFromProperty {
    MapThroughViewAndFromPropertyToCollection(Mapper mapper, Property<?> property, MappingView mappingView, FromPropertyWrapper fromPropertyWrapper) {
        super(mapper, property, mappingView, fromPropertyWrapper);
    }

    @Override
    public ClassMapBuilder<?, ?> configMapping(ClassMapBuilder<?, ?> classMapBuilder) {
        return classMapBuilder.fieldMap(fromPropertyWrapper.value.name, property.getName())
                .converter(mapper.registerConverter(fromPropertyWrapper.createViewListConverter(mapper, mappingView.value())))
                .add();
    }

}

class MapThroughViewAndFromPropertyToMap extends MapThroughViewAndFromPropertyToCollection {

    MapThroughViewAndFromPropertyToMap(Mapper mapper, Property<?> property, MappingView mappingView, FromPropertyWrapper fromPropertyWrapper) {
        super(mapper, property, mappingView, fromPropertyWrapper);
    }

    @Override
    public ClassMapBuilder<?, ?> configMapping(ClassMapBuilder<?, ?> classMapBuilder) {
        if (fromPropertyWrapper.isDifferentSourceProperty())
            throw new IllegalArgumentException("Key and Value source property should be same");
        return classMapBuilder.fieldMap(fromPropertyWrapper.value.name, property.getName())
                .converter(mapper.registerConverter(fromPropertyWrapper.createViewMapConverter(mapper, mappingView.value())))
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

    boolean isFlatToCollection() {
        return value.elementName != null && (key == null || key.elementName == null);
    }

    boolean isFlatToMap() {
        return value.elementName != null && key != null && key.elementName != null;
    }

    boolean isDifferentSourceProperty() {
        return !value.name.equals(key.name);
    }

    ViewMapConverter createViewMapConverter(Mapper mapper, Class<?> view) {
        return new ViewMapConverter(mapper, view, key.elementName, value.elementName);
    }

    ViewListConverter createViewListConverter(Mapper mapper, Class<?> view) {
        return new ViewListConverter(mapper, view, value.elementName);
    }

    ListConverter createListPropertyConverter(Mapper mapper, String desName) {
        return new ListConverter(mapper, value.elementName, desName);
    }

    MapConverter createMapPropertyConverter(Mapper mapper, String desName) {
        return new MapConverter(mapper, key.elementName, value.elementName, desName);
    }
}
