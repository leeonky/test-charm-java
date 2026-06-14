package org.testcharm.dal.runtime.schema;

import org.testcharm.dal.runtime.IllegalTypeException;
import org.testcharm.dal.runtime.MissingFieldValueGenericTypeException;
import org.testcharm.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;
import org.testcharm.util.BeanClass;
import org.testcharm.util.Zipped;
import org.testcharm.util.function.IfFactory;
import org.testcharm.util.function.TriConsumer;

import static java.lang.String.format;
import static java.util.stream.Collectors.toSet;
import static org.testcharm.util.Zipped.zip;
import static org.testcharm.util.function.When.when;

public class Verification {
    private static final IfFactory.Factory<Expect, TriConsumer<Verification, DALRuntimeContext, Actual>> VERIFICATIONS =
            when(Expect::isSchema).<TriConsumer<Verification, DALRuntimeContext, Actual>>then(Verification::schema)
                    .when(Expect::isFormatter).then(Verification::formatter)
                    .when(Expect::isSchemaValue).then(combine(Verification::valueStructure, Verification::valueContent))
                    .when(Expect::isMap).then(combine(Verification::mapStructure, Verification::mapContent))
                    .when(Expect::isCollection).then(combine(Verification::collectionStructure, Verification::collectionContent))
                    .when(Expect::isSchemaType).then(combine(Verification::typeStructure, Verification::typeContent))
                    .orElse(combine(Verification::structure, Verification::content));
    private final Expect expect;

    private Verification(Expect expect) {
        this.expect = expect;
    }

    public static Verification expect(Expect expect) {
        return new Verification(expect);
    }

    public static IllegalTypeException buildError(String format, Object... params) {
        return new IllegalTypeException(format(format, params));
    }

    private static TriConsumer<Verification, DALRuntimeContext, Actual> combine(
            TriConsumer<Verification, DALRuntimeContext, Actual> structure,
            TriConsumer<Verification, DALRuntimeContext, Actual> content) {
        return ((verification, context, actual) -> {
            if (verification.expect.structure())
                structure.accept(verification, context, actual);
            else
                content.accept(verification, context, actual);
        });
    }

    public boolean verify(DALRuntimeContext runtimeContext, Actual actual) {
        VERIFICATIONS.get(expect).accept(this, runtimeContext, actual);
        return true;
    }

    private void valueStructure(DALRuntimeContext runtimeContext, Actual actual) {
        actual.convertAble(expect.getGenericType(0).orElseThrow(actual::invalidGenericType),
                expect.inspectExpectType());
    }

    private void valueContent(DALRuntimeContext runtimeContext, Actual actual) {
        try {
            expect.verifyValue(actual::verifyValue);
        } catch (MissingFieldValueGenericTypeException ignore) {
            throw actual.invalidGenericType();
        }
    }

    @SuppressWarnings("unchecked")
    private void mapStructure(DALRuntimeContext context, Actual actual) {
        BeanClass<Object> type = (BeanClass<Object>) expect.getGenericType(1).orElseThrow(actual::invalidGenericType);
        actual.fieldNames().forEach(key -> expect(expect.sub(type, key)).verify(context, actual.sub(key)));
    }

    private void mapContent(DALRuntimeContext context, Actual actual) {
        actual.verifySize(Actual::fieldNames, expect.mapKeysSize());
        mapStructure(context, actual);
    }

    private void collectionStructure(DALRuntimeContext context, Actual actual) {
        zip(actual.subElements(), expect.subElements()).stream()
                .forEach(e -> expect(e.right()).verify(context, e.left()));
    }

    private void collectionContent(DALRuntimeContext context, Actual actual) {
        Zipped<Actual, Expect> zipped = zip(actual.subElements(), expect.subElements());
        zipped.stream().forEach(e -> expect(e.right()).verify(context, e.left()));
        if (zipped.hasLeft())
            throw actual.lessExpectSizeError(zipped.index());
        if (zipped.hasRight())
            throw actual.moreExpectSizeError(zipped.index());
    }

    private void formatter(DALRuntimeContext runtimeContext, Actual actual) {
        actual.verifyFormatter(expect.extractFormatter());
    }

    private void typeContent(DALRuntimeContext runtimeContext, Actual actual) {
        actual.verifyType(expect.extractType());
    }

    private void typeStructure(DALRuntimeContext r, Actual actual) {
        expect.isInstanceType(actual);
    }

    private void structure(DALRuntimeContext runtimeContext, Actual actual) {
        expect.isInstanceOf(actual);
    }

    private void content(DALRuntimeContext runtimeContext, Actual actual) {
        expect.equals(actual, runtimeContext);
    }

    private void schema(DALRuntimeContext runtimeContext, Actual actual) {
        expect.asSchema(actual).verify(runtimeContext, actual,
                actual.fieldNames().filter(String.class::isInstance).map(Object::toString).collect(toSet()));
    }
}
