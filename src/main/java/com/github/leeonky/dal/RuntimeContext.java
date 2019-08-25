package com.github.leeonky.dal;

import com.github.leeonky.dal.ast.Node;
import com.github.leeonky.dal.format.Formatter;
import com.github.leeonky.dal.type.AllowNull;
import com.github.leeonky.dal.util.ListAccessor;
import com.github.leeonky.dal.util.PropertyAccessor;
import com.github.leeonky.dal.util.TypeData;
import com.github.leeonky.dal.util.WrappedObject;
import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.GenericType;
import com.github.leeonky.util.PropertyReader;

import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public class RuntimeContext {
    private final TypeData<PropertyAccessor> propertyAccessors;
    private final TypeData<ListAccessor> listAccessors;
    private final LinkedList<Object> wrappedValueStack = new LinkedList<>();
    private final Map<String, Constructor> constructors;
    private final Set<Class<?>> schemas;

    public RuntimeContext(Object inputValue, TypeData<PropertyAccessor> propertyAccessors,
                          Map<String, Constructor> constructors, TypeData<ListAccessor> listAccessors, Set<Class<?>> schemas) {
        this.schemas = schemas;
        wrappedValueStack.push(inputValue);
        this.constructors = constructors;
        this.propertyAccessors = propertyAccessors;
        this.listAccessors = listAccessors;
    }

    public Object getInputValue() {
        return wrappedValueStack.getFirst();
    }

    public Object wrapInputValueAndEvaluate(Object value, Node node) {
        try {
            wrappedValueStack.push(value);
            return node.evaluate(this);
        } finally {
            wrappedValueStack.pop();
        }
    }

    public Optional<Constructor> searchConstructor(String type) {
        return Optional.ofNullable(constructors.get(type));
    }

    public Optional<ListAccessor> searchListAccessor(Object object) {
        return listAccessors.getData(object);
    }

    public WrappedObject wrap(Object instance) {
        return new WrappedObject(instance, propertyAccessors, listAccessors);
    }

    public boolean verifySchema(Class<?> schemaType, WrappedObject wrappedObject, String subPrefix) {
        BeanClass<?> beanClass = wrappedObject.getPolymorphicSchemaType(schemaType);
        Set<String> propertyReaderNames = wrappedObject.getPropertyReaderNames();

        return noMoreUnexpectedField(beanClass, beanClass.getPropertyReaders().keySet(), propertyReaderNames)
                && allMandatoryPropertyShouldBeExist(beanClass, propertyReaderNames)
                && allPropertyValueShouldBeValid(wrappedObject, subPrefix, beanClass);
    }

    private boolean allPropertyValueShouldBeValid(WrappedObject wrappedObject, String subPrefix, BeanClass<?> beanClass) {
        return beanClass.getPropertyReaders().values().stream()
                .noneMatch(propertyReader -> {
                    WrappedObject propertyValueWrapper = wrappedObject.getPropertyValueWrapper(propertyReader.getName());
                    if (isAllowNull().test(propertyReader) && propertyValueWrapper.isNull())
                        return false;
                    return !verifySchemaInGenericType(subPrefix + "." + propertyReader.getName(), propertyValueWrapper, propertyReader.getGenericType());
                });
    }

    private boolean allMandatoryPropertyShouldBeExist(BeanClass<?> beanClass, Set<String> actualFields) {
        return beanClass.getPropertyReaders().values().stream()
                .filter(isAllowNull().negate())
                .filter(propertyReader -> !actualFields.contains(propertyReader.getName()))
                .peek(propertyReader -> System.err.printf("Expected field `%s` for type %s[%s], but does not exist\n", propertyReader.getName(), beanClass.getSimpleName(), beanClass.getName()))
                .count() == 0;
    }

    private boolean noMoreUnexpectedField(BeanClass beanClass, Set<String> expectedFields, Set<String> actualFields) {
        return actualFields.stream()
                .filter(f -> !expectedFields.contains(f))
                .peek(f -> System.err.printf("Unexpected field `%s` for type %s[%s]\n", f, beanClass.getSimpleName(), beanClass.getName()))
                .count() == 0;
    }

    private Predicate<PropertyReader<?>> isAllowNull() {
        return propertyReader -> propertyReader.getAnnotation(AllowNull.class) != null;
    }

    private boolean verifySchemaInGenericType(String subPrefix, WrappedObject wrapperObject, GenericType genericType) {
        Class<?> fieldType = genericType.getRawType();
        if (Formatter.class.isAssignableFrom(fieldType))
            return verifyFormatterValue(subPrefix, wrapperObject, fieldType);
        else if (schemas.contains(fieldType))
            return verifySchema(fieldType, wrapperObject, subPrefix);
        else if (Iterable.class.isAssignableFrom(fieldType))
            return verifyList(subPrefix, wrapperObject, genericType);
        else if (Map.class.isAssignableFrom(fieldType))
            return verifyMap(subPrefix, wrapperObject, genericType);
        return true;
    }

    private boolean verifyMap(String subPrefix, WrappedObject wrapperObject, GenericType genericType) {
        GenericType subGenericType = genericType.getGenericTypeParameter(1).orElseThrow(() ->
                new IllegalArgumentException(subPrefix + " should be generic type"));
        for (String key : wrapperObject.getPropertyReaderNames())
            if (!verifySchemaInGenericType(subPrefix + "." + key, wrapperObject.getPropertyValueWrapper(key), subGenericType))
                return false;
        return true;
    }

    private boolean verifyList(String subPrefix, WrappedObject wrapperObject, GenericType genericType) {
        int index = 0;
        GenericType subGenericType = genericType.getGenericTypeParameter(0).orElseThrow(() ->
                new IllegalArgumentException(subPrefix + " should be generic type"));
        for (WrappedObject wrappedElement : wrapperObject.getWrappedList())
            if (!verifySchemaInGenericType(String.format("%s[%d]", subPrefix, index++), wrappedElement, subGenericType))
                return false;
        return true;
    }

    @SuppressWarnings("unchecked")
    private boolean verifyFormatterValue(String subPrefix, WrappedObject wrapperObject, Class<?> fieldType) {
        Formatter formatter = (Formatter) BeanClass.newInstance(fieldType);
        if (!formatter.isValidValue(wrapperObject.getValue())) {
            System.err.printf("Expected field `%s` should be in %s, but was [%s]\n",
                    subPrefix, formatter.getFormatterName(), wrapperObject.getValue());
            return false;
        }
        return true;
    }
}
