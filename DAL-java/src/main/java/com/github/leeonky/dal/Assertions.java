package com.github.leeonky.dal;

import com.github.leeonky.dal.ast.node.ConstValueNode;
import com.github.leeonky.dal.ast.node.InputNode;
import com.github.leeonky.dal.ast.opt.Factory;
import com.github.leeonky.dal.runtime.IllegalTypeException;
import com.github.leeonky.dal.runtime.InputException;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder;
import com.github.leeonky.dal.runtime.schema.Expect;
import com.github.leeonky.dal.runtime.schema.Verification;
import com.github.leeonky.dal.type.InputCode;
import com.github.leeonky.dal.type.InputValue;
import com.github.leeonky.interpreter.InterpreterException;

import java.lang.reflect.Array;
import java.util.function.Supplier;

import static com.github.leeonky.dal.ast.node.DALExpression.expression;
import static com.github.leeonky.dal.runtime.schema.Actual.actual;
import static com.github.leeonky.util.BeanClass.create;

public class Assertions {
    private final InputCode<Object> inputCode;
    public static boolean dumpInput = true;
    private DAL dal;
    private static Supplier<DAL> dalFactory = () -> DAL.getInstance("AssertD");
    private Class<?> schema;

    public static void setDalFactory(Supplier<DAL> dalFactory) {
        Assertions.dalFactory = dalFactory;
    }

    public static void dumpInput(boolean enable) {
        dumpInput = enable;
    }

    private Assertions(InputCode<Object> input) {
        inputCode = input;
    }

    public Assertions use(DAL dal) {
        this.dal = dal;
        return this;
    }

    public static Assertions expect(Object input) {
        return new Assertions((InputValue<Object>) () -> input);
    }

    public static Assertions expectRun(InputCode<Object> supplier) {
        return new Assertions(supplier);
    }

    public Assertions should(String dalExpression) {
        return should("", dalExpression);
    }

    public Assertions should(String prefix, String verification) {
        String fullCode = prefix + verification;
        try {
            return execute(() -> getDal().evaluate(inputCode, fullCode, schema));
        } catch (InterpreterException e) {
            String detailMessage = "\n" + e.show(fullCode, prefix.length()) + "\n\n" + e.getMessage();
            if (dumpInput)
                detailMessage += "\n\nThe root value was: " + getDal().getRuntimeContextBuilder().build(null).wrap(inputCode).dumpAll();
            throw new AssertionError(detailMessage);
        }
    }

    private DAL getDal() {
        if (dal == null)
            dal = dalFactory.get();
        return dal;
    }

    private Assertions execute(Runnable runnable) {
        try {
            runnable.run();
        } catch (InputException e) {
            String detailMessage = "\n" + e.getMessage();
            detailMessage += "\n\nInput code got exception: " + getDal().getRuntimeContextBuilder().build(null).wrap(e.getInputClause()).dumpAll();
            throw new AssertionError(detailMessage);
        }
        return this;
    }

    public void exact(String verification) {
        should("=", verification);
    }

    public void match(String verification) {
        should(":", verification);
    }

    public Assertions is(Class<?> schema) {
        RuntimeContextBuilder.DALRuntimeContext context = getDal().getRuntimeContextBuilder().build(inputCode, schema);
        try {
            this.schema = schema;
            return execute(() -> Verification.expect(new Expect(create((Class) schema), null))
                    .verify(context, actual(context.getThis())));
        } catch (IllegalTypeException e) {
            String detailMessage = "\n" + e.getMessage();
            if (dumpInput)
                detailMessage += "\n\nThe root value was: " + getDal().getRuntimeContextBuilder().build(null).wrap(inputCode).dumpAll();
            throw new AssertionError(detailMessage);
        }
    }

    public Assertions is(String schema) {
        if (schema.startsWith("[") && schema.endsWith("]"))
            return is(Array.newInstance(getDal().getRuntimeContextBuilder().schemaType(
                    schema.replace('[', ' ').replace(']', ' ').trim()).getType(), 0).getClass());
        return is(getDal().getRuntimeContextBuilder().schemaType(schema).getType());
    }

    public Assertions isEqualTo(Object expect) {
        return execute(() -> expression(InputNode.INPUT_NODE, Factory.equal(), new ConstValueNode(expect))
                .evaluate(getDal().getRuntimeContextBuilder().build(inputCode)));
    }
}
