package org.testcharm.dal.runtime.schema;

import org.testcharm.dal.format.Formatter;
import org.testcharm.dal.format.Type;
import org.testcharm.dal.format.Value;
import org.testcharm.dal.runtime.RuntimeContextBuilder;
import org.testcharm.dal.type.AllowNull;
import org.testcharm.dal.type.Partial;
import org.testcharm.dal.type.Schema;
import org.testcharm.util.*;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.lang.String.valueOf;
import static java.util.stream.Collectors.joining;
import static org.testcharm.dal.runtime.schema.Verification.buildError;

public class Expect {
    protected final BeanClass<Object> type;
    protected final Object expect;

    public Expect(BeanClass<Object> type, Object expect) {
        this.type = type;
        this.expect = expect;
    }

    public boolean isSchema() {
        return type.isInheritedFrom(Schema.class);
    }

    public boolean isFormatter() {
        return type.isInheritedFrom(Formatter.class);
    }

    public boolean isCollection() {
        return type.isCollection();
    }

    public boolean isMap() {
        return type.isInheritedFrom(Map.class);
    }

    public boolean isSchemaValue() {
        return type.isInheritedFrom(Value.class);
    }

    public boolean isSchemaType() {
        return type.isInheritedFrom(Type.class);
    }

    @SuppressWarnings("unchecked")
    public Expect sub(PropertyReader<Object> propertyReader) {
        return new Expect((BeanClass<Object>) propertyReader.getType(),
                expect == null ? null : propertyReader.getValue(expect));
    }

    @SuppressWarnings("unchecked")
    public Expect sub(BeanClass<Object> type, Object key) {
        return new Expect(type, expect == null ? null : ((Map<?, Object>) expect).get(key));
    }

    public Expect sub(int index) {
        return sub(type.getPropertyReader(valueOf(index)));
    }

    public Optional<Schema> getSchema() {
        return BeanClass.cast(expect, Schema.class);
    }

    @SuppressWarnings("unchecked")
    public Formatter<Object, Object> extractFormatter() {
        if (expect == null)
            return (Formatter<Object, Object>) type.getTypeArguments(0)
                    .map(t -> type.newInstance(t.getType())).orElseGet(type::newInstance);
        return (Formatter<Object, Object>) expect;
    }

    public Optional<BeanClass<?>> getGenericType(int typePosition) {
        return type.getTypeArguments(typePosition);
    }

    public String inspectExpectType() {
        return format("type [%s]", type.getName());
    }

    public String inspectFullType() {
        return String.format("%s[%s]", type.getSimpleName(), type.getName());
    }

    @SuppressWarnings("unchecked")
    public void verifyValue(BiConsumer<Value<Object>, BeanClass<?>> predicate) {
        predicate.accept((Value<Object>) expect, getGenericType(0).orElse(null));
    }

    @SuppressWarnings("unchecked")
    @Deprecated
    public int mapKeysSize() {
        return ((Map<?, Object>) expect).size();
    }

    public Stream<Expect> subElements() {
        AtomicInteger index = new AtomicInteger();
        return expect == null ? Stream.generate(() -> sub(index.getAndIncrement()))
                : CollectionHelper.convertToStream(expect).map(e -> sub(index.getAndIncrement()));
    }

    @SuppressWarnings("unchecked")
    public Type<Object> extractType() {
        return (Type<Object>) expect;
    }

    public void isInstanceOf(Actual actual) {
        actual.inInstanceOf(type);
    }

    public void isInstanceType(Actual actual) {
        actual.inInstanceOf(getGenericType(0).orElseThrow(actual::invalidGenericType));
    }

    public void equals(Actual actual, RuntimeContextBuilder.DALRuntimeContext runtimeContext) {
        actual.equalsExpect(expect, runtimeContext);
    }

    public Stream<PropertyReader<Object>> propertyReaders() {
        return type.getPropertyReaders().values().stream();
    }

    public boolean structure() {
        return expect == null;
    }

    SchemaExpect asSchema(Actual actual) {
        return new SchemaExpect(actual.polymorphicSchemaType(type.getType()), expect);
    }

    static class SchemaExpect extends Expect {
        public SchemaExpect(Class<Object> schemaType, Object expect) {
            super(BeanClass.create(schemaType), expect == null ? Classes.newInstance(schemaType) : expect);
        }

        public void noMoreUnexpectedField(Set<String> actualFields) {
            if (type.getAnnotation(Partial.class) != null)
                return;
            Set<String> expectFields = new LinkedHashSet<String>(actualFields) {{
                propertyReaders().map(PropertyAccessor::getName).forEach(this::remove);
            }};
            if (!expectFields.isEmpty())
                throw buildError("Unexpected field %s for schema %s",
                        expectFields.stream().collect(joining("`, `", "`", "`")), inspectFullType());
        }

        public void allMandatoryPropertyShouldBeExist(Set<String> actualFields) {
            propertyReaders().filter(field -> field.getAnnotation(AllowNull.class) == null).forEach(field -> {
                if (!actualFields.contains(field.getName()))
                    throw buildError("Expected field `%s` to be in type %s, but does not exist", field.getName(),
                            inspectFullType());
            });
        }

        public void allPropertyValueShouldBeValid(RuntimeContextBuilder.DALRuntimeContext runtimeContext, Actual actual) {
            propertyReaders().forEach(propertyReader -> {
                Actual subActual = actual.sub(propertyReader.getName());
                if (propertyReader.getAnnotation(AllowNull.class) == null || !subActual.isNull())
                    Verification.expect(sub(propertyReader)).verify(runtimeContext, subActual);
            });
        }

        public void verifySchemaInstance(Actual actual) {
            getSchema().ifPresent(actual::verifySchema);
        }

        public void verify(RuntimeContextBuilder.DALRuntimeContext runtimeContext, Actual actual, Set<String> actualFields) {
            noMoreUnexpectedField(actualFields);
            allMandatoryPropertyShouldBeExist(actualFields);
            allPropertyValueShouldBeValid(runtimeContext, actual);
            verifySchemaInstance(actual);
        }
    }
}
