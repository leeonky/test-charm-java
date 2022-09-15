package com.github.leeonky.dal.runtime.verifier;

import com.github.leeonky.dal.compiler.Compiler;
import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.IllegalTypeException;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;
import com.github.leeonky.dal.runtime.Schema;
import com.github.leeonky.dal.runtime.SchemaAssertionFailure;
import com.github.leeonky.dal.type.AllowNull;
import com.github.leeonky.dal.type.Partial;
import com.github.leeonky.dal.type.SubType;
import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.PropertyReader;

import java.util.Set;
import java.util.stream.Stream;

import static com.github.leeonky.dal.runtime.verifier.field.Factory.createFieldSchema;
import static java.lang.String.format;
import static java.util.stream.Collectors.toSet;

public class SchemaVerifier {

    //    TODO private
    public final Data object;
    //    TODO private
    public final DALRuntimeContext runtimeContext;
    private static final Compiler compiler = new Compiler();

    public SchemaVerifier(DALRuntimeContext runtimeContext, Data object) {
        this.runtimeContext = runtimeContext;
        this.object = object;
    }

    @SuppressWarnings("unchecked")
    public <T> BeanClass<T> getPolymorphicSchemaType(Class<?> superSchemaType) {
        Class<?> type = superSchemaType;
        SubType subType = superSchemaType.getAnnotation(SubType.class);
        if (subType != null) {
            Object value = object.getValue(compiler.toChainNodes(subType.property())).getInstance();
            type = Stream.of(subType.types()).filter(t -> t.value().equals(value)).map(SubType.Type::type)
                    .findFirst().orElseThrow(() -> new IllegalStateException(
                            format("Cannot guess sub type through property type value[%s]", value)));
        }
        return (BeanClass<T>) BeanClass.create(type);
    }

    public boolean verify(Class<?> clazz, Object schemaInstance, String subPrefix) {
        Set<String> propertyReaderNames = object.getFieldNames().stream().filter(String.class::isInstance)
                .map(Object::toString).collect(toSet());
        BeanClass<Object> schemaType = getPolymorphicSchemaType(clazz);
        Object schema = schemaInstance == null ? schemaType.newInstance() : schemaInstance;
        return (clazz.getAnnotation(Partial.class) != null ||
                noMoreUnexpectedField(schemaType, schemaType.getPropertyReaders().keySet(), propertyReaderNames))
                && allMandatoryPropertyShouldBeExist(schemaType, propertyReaderNames)
                && allPropertyValueShouldBeValid(subPrefix, schemaType, schema)
                && schemaVerificationShouldPass(schema);
    }

    public boolean schemaVerificationShouldPass(Object schema) {
        if (schema instanceof Schema) {
            try {
                ((Schema) schema).verify(object);
            } catch (SchemaAssertionFailure schemaAssertionFailure) {
                return errorLog(schemaAssertionFailure.getMessage());
            }
        }
        return true;
    }

    public <T> boolean noMoreUnexpectedField(BeanClass<T> polymorphicBeanClass, Set<String> expectedFields, Set<String> actualFields) {
        return actualFields.stream().allMatch(f -> shouldNotContainsUnexpectedField(polymorphicBeanClass, expectedFields, f));
    }

    public <T> boolean allMandatoryPropertyShouldBeExist(BeanClass<T> polymorphicBeanClass, Set<String> actualFields) {
        return polymorphicBeanClass.getPropertyReaders().values().stream()
                .filter(propertyReader -> propertyReader.getAnnotation(AllowNull.class) == null)
                .allMatch(propertyReader -> shouldContainsField(actualFields, polymorphicBeanClass, propertyReader));
    }

    public <T> boolean allPropertyValueShouldBeValid(String subPrefix, BeanClass<T> polymorphicBeanClass, T schemaInstance) {
        return polymorphicBeanClass.getPropertyReaders().values().stream().allMatch(propertyReader -> {
            Data fieldValue = object.getValue(propertyReader.getName());
            SchemaVerifier schemaVerifier = fieldValue.createSchemaVerifier();
            return allowNullAndIsNull(propertyReader, fieldValue)
                    || createFieldSchema(subPrefix + "." + propertyReader.getName(), propertyReader.getType(),
                    propertyReader.getValue(schemaInstance), schemaVerifier.runtimeContext, schemaVerifier.object)
                    .verify(schemaVerifier.runtimeContext);

        });
    }

    public static <T> boolean allowNullAndIsNull(PropertyReader<T> propertyReader, Data propertyValueWrapper) {
        return propertyReader.getAnnotation(AllowNull.class) != null && propertyValueWrapper.isNull();
    }

    public static boolean shouldNotContainsUnexpectedField(BeanClass<?> polymorphicBeanClass, Set<String> expectedFields, String f) {
        return expectedFields.contains(f) || errorLog("Unexpected field `%s` for schema %s[%s]", f,
                polymorphicBeanClass.getSimpleName(), polymorphicBeanClass.getName());
    }

    public static boolean shouldContainsField(Set<String> actualFields, BeanClass<?> polymorphicBeanClass, PropertyReader<?> propertyReader) {
        return actualFields.contains(propertyReader.getName())
                || errorLog("Expecting field `%s` to be in type %s[%s], but does not exist", propertyReader.getName(),
                polymorphicBeanClass.getSimpleName(), polymorphicBeanClass.getName());
    }

    public static boolean errorLog(String format, Object... params) {
        throw new IllegalTypeException(String.format(format, params));
    }
}
