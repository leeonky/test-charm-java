package org.testcharm.dal.runtime.schema;

import org.testcharm.dal.compiler.Compiler;
import org.testcharm.dal.format.Formatter;
import org.testcharm.dal.format.Type;
import org.testcharm.dal.format.Value;
import org.testcharm.dal.runtime.DALRuntimeException;
import org.testcharm.dal.runtime.Data;
import org.testcharm.dal.runtime.IllegalTypeException;
import org.testcharm.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;
import org.testcharm.dal.type.Schema;
import org.testcharm.dal.type.SubType;
import org.testcharm.util.BeanClass;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static org.testcharm.dal.runtime.schema.Verification.buildError;
import static org.testcharm.util.Classes.getClassName;

public class Actual {
    private final String property;
    private final Data<?> actual;

    public Actual(String property, Data<?> actual) {
        this.property = property;
        this.actual = actual;
    }

    public static Actual actual(Data<?> data) {
        return new Actual("", data);
    }

    public Actual sub(Object property) {
        return new Actual(this.property + "." + property, actual.property(property));
    }

    public boolean isNull() {
        return actual.isNull();
    }

    private final static Compiler compiler = new Compiler();

    @SuppressWarnings("unchecked")
    public Class<Object> polymorphicSchemaType(Class<?> schemaType) {
        return ofNullable(schemaType.getAnnotation(SubType.class)).map(subType -> {
            Object subTypeProperty = actual.property(compiler.toChainNodes(subType.property())).value();
            return (Class<Object>) Stream.of(subType.types()).filter(t -> t.value().equals(subTypeProperty))
                    .map(SubType.Type::type).findFirst().orElseThrow(() -> new DALRuntimeException(
                            format("Cannot guess sub type through property type value[%s]", subTypeProperty)));
        }).orElse((Class<Object>) schemaType);
    }

    public IllegalStateException invalidGenericType() {
        return new IllegalStateException(format("%s should specify generic type", property));
    }

    public void convertAble(BeanClass<?> type, String inspect) {
        if (isNull())
            throw buildError("Can not convert null field `%s` to %s, " +
                    "use @AllowNull to verify nullable field", property, inspect);
        try {
            actual.convert(type.getType());
        } catch (Exception ignore) {
            throw buildError("Can not convert field `%s` (%s: %s) to %s", property,
                    getClassName(actual.value()), actual.value(), inspect);
        }
    }

    public void verifyValue(Value<Object> value, BeanClass<?> type) {
        if (!value.verify(value.convertAs(actual, type)))
            throw buildError(value.errorMessage(property, actual.value()));
    }

    public Stream<?> fieldNames() {
        return actual.fieldNames().stream();
    }

    public Stream<Actual> subElements() {
        return actual.list().wraps().stream().map(data -> new Actual(property + "[" + data.index() + "]", data.value()));
    }

    public void verifyFormatter(Formatter<Object, Object> formatter) {
        if (!formatter.isValid(actual.value()))
            throw buildError("Expected field `%s` to be formatter `%s`\nActual: %s", property,
                    formatter.getFormatterName(), actual.dump());
    }

    void verifySize(Function<Actual, Stream<?>> actualStream, int expectSize) {
        if (actualStream.apply(this).count() != expectSize)
            throw buildError("Expected field `%s` to be size <%d>, but was size <%d>",
                    property, expectSize, actualStream.apply(this).count());
    }

    public IllegalTypeException moreExpectSizeError(int size) {
        return buildError("Collection Field `%s` size was only <%d>, expected too more",
                property, size);
    }

    public IllegalTypeException lessExpectSizeError(int size) {
        return buildError("Expected collection field `%s` to be size <%d>, but too many elements", property, size);
    }

    void verifyType(Type<Object> expect) {
        if (!expect.verify(actual.value()))
            throw buildError(expect.errorMessage(property, actual.value()));
    }

    void inInstanceOf(BeanClass<?> type) {
        if (!type.isInstance(actual.value()))
            throw buildError(String.format("Expected field `%s` to be %s\nActual: %s", property,
                    type.getName(), actual.dump()));
    }

    public void equalsExpect(Object expect, DALRuntimeContext runtimeContext) {
        if (!Objects.equals(expect, actual.value()))
            throw buildError(format("Expected field `%s` to be %s\nActual: %s", property,
                    runtimeContext.data(expect).dump(), actual.dump()));
    }

    public void verifySchema(Schema expect) {
        try {
            expect.verify(actual);
        } catch (Throwable throwable) {
            throw buildError(throwable.getMessage());
        }
    }
}
