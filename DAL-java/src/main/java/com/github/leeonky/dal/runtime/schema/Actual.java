package com.github.leeonky.dal.runtime.schema;

import com.github.leeonky.dal.compiler.Compiler;
import com.github.leeonky.dal.format.Formatter;
import com.github.leeonky.dal.format.Type;
import com.github.leeonky.dal.format.Value;
import com.github.leeonky.dal.runtime.DALRuntimeException;
import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;
import com.github.leeonky.dal.type.Schema;
import com.github.leeonky.dal.type.SubType;
import com.github.leeonky.util.BeanClass;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.github.leeonky.util.Classes.getClassName;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;

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

    public Actual sub(Integer index) {
        return new Actual(property + "[" + index + "]", actual.property(index));
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

    public boolean convertAble(BeanClass<?> type, String inspect) {
        if (isNull())
            return Verification.errorLog("Can not convert null field `%s` to %s, " +
                    "use @AllowNull to verify nullable field", property, inspect);
        try {
            actual.convert(type.getType());
            return true;
        } catch (Exception ignore) {
            return Verification.errorLog("Can not convert field `%s` (%s: %s) to %s", property,
                    getClassName(actual.value()), actual.value(), inspect);
        }
    }

    public boolean verifyValue(Value<Object> value, BeanClass<?> type) {
        if (value.verify(value.convertAs(actual, type))) return true;
        return Verification.errorLog(value.errorMessage(property, actual.value()));
    }

    public Stream<?> fieldNames() {
        return actual.fieldNames().stream();
    }

    public Stream<Actual> subElements() {
        return actual.list().wraps().stream().map(data -> new Actual(property + "[" + data.index() + "]", data.value()));
    }

    public boolean verifyFormatter(Formatter<Object, Object> formatter) {
        return formatter.isValid(actual.value())
                || Verification.errorLog("Expected field `%s` to be formatter `%s`\nActual: %s", property,
                formatter.getFormatterName(), actual.dump());
    }

    boolean verifySize(Function<Actual, Stream<?>> actualStream, int expectSize) {
        return actualStream.apply(this).count() == expectSize
                || Verification.errorLog("Expected field `%s` to be size <%d>, but was size <%d>",
                property, expectSize, actualStream.apply(this).count());
    }

    boolean moreExpectSize(int size) {
        return Verification.errorLog("Collection Field `%s` size was only <%d>, expected too more",
                property, size);
    }

    public boolean lessExpectSize(int size) {
        return Verification.errorLog("Expected collection field `%s` to be size <%d>, but too many elements", property, size);
    }

    boolean verifyType(Type<Object> expect) {
        if (expect.verify(actual.value())) return true;
        return Verification.errorLog(expect.errorMessage(property, actual.value()));
    }

    boolean inInstanceOf(BeanClass<?> type) {
        return type.isInstance(actual.value()) ||
                Verification.errorLog(String.format("Expected field `%s` to be %s\nActual: %s", property,
                        type.getName(), actual.dump()));
    }

    public boolean equalsExpect(Object expect, DALRuntimeContext runtimeContext) {
        return Objects.equals(expect, actual.value()) ||
                Verification.errorLog(format("Expected field `%s` to be %s\nActual: %s", property,
                        runtimeContext.data(expect).dump(), actual.dump()));
    }

    public void verifySchema(Schema expect) {
        try {
            expect.verify(actual);
        } catch (Throwable throwable) {
            Verification.errorLog(throwable.getMessage());
        }
    }
}
