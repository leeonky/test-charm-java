package org.testcharm.dal;

import org.testcharm.dal.ast.node.InputNode;
import org.testcharm.dal.ast.node.LiteralNode;
import org.testcharm.dal.ast.opt.Factory;
import org.testcharm.dal.runtime.Data;
import org.testcharm.dal.runtime.IllegalTypeException;
import org.testcharm.dal.runtime.RuntimeContextBuilder;
import org.testcharm.dal.runtime.schema.Expect;
import org.testcharm.dal.runtime.schema.Verification;
import org.testcharm.dal.type.InputCode;
import org.testcharm.interpreter.InterpreterException;
import org.testcharm.util.ThrowingSupplier;

import java.lang.reflect.Array;
import java.util.function.Supplier;

import static org.testcharm.dal.ast.node.DALExpression.expression;
import static org.testcharm.dal.runtime.schema.Actual.actual;
import static org.testcharm.util.BeanClass.create;

public class Assertions {
    private final InputCode<Object> inputCode;
    public static boolean dumpInput = true;
    private DAL dal;
    private static Supplier<DAL> dalFactory;
    private Class<?> schema;
    private Object constants;

    static {
        setDALFactory(DAL::dal);
    }

    public static void setDALFactory(Supplier<DAL> dalFactory) {
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
        return new Assertions(() -> input);
    }

    public static Assertions expectRun(ThrowingSupplier<Object> supplier) {
        return new Assertions(supplier::get);
    }

    public Assertions should(String dalExpression) {
        return should("", dalExpression);
    }

    public Assertions should(String prefix, String verification) {
        String fullCode = prefix + verification;
        try {
            getDAL().evaluate(inputCode, fullCode, schema, constants);
            return this;
        } catch (InterpreterException e) {
            String detailMessage = e.show(fullCode, prefix.length()) + "\n\n" + e.getMessage();
            if (dumpInput)
                detailMessage += "\n\nThe root value was: " + getDAL().getRuntimeContextBuilder().build(inputCode).getThis().dump() + "\n";
            throw new AssertionError(detailMessage);
        }
    }

    private DAL getDAL() {
        if (dal == null)
            dal = dalFactory.get();
        return dal;
    }

    public void exact(String verification) {
        should("=", verification);
    }

    public void match(String verification) {
        should(":", verification);
    }

    @SuppressWarnings("unchecked")
    public Assertions is(Class<?> schema) {
        RuntimeContextBuilder.DALRuntimeContext context = getDAL().getRuntimeContextBuilder().build(inputCode, schema);
        Data<?> input = context.getThis();
        try {
            this.schema = schema;
            Verification.expect(new Expect(create((Class) schema), null))
                    .verify(context, actual(context.getThis()));
            return this;
        } catch (IllegalTypeException e) {
            String detailMessage = "\n" + e.getMessage();
            if (dumpInput)
                detailMessage += "\n\nThe root value was: " + input.dump();
            throw new AssertionError(detailMessage);
        }
    }

    public Assertions is(String schema) {
        if (schema.startsWith("["))
            if (schema.endsWith("]"))
                return is(Array.newInstance(getDAL().getRuntimeContextBuilder().schemaType(
                        schema.replace('[', ' ').replace(']', ' ').trim()).getType(), 0).getClass());
            else
                throw new IllegalArgumentException("Invalid schema: " + schema);
        return is(getDAL().getRuntimeContextBuilder().schemaType(schema).getType());
    }

    public Assertions isEqualTo(Object expect) {
        try {
            expression(InputNode.Root.INSTANCE, Factory.equal(), new LiteralNode(expect))
                    .evaluate(getDAL().getRuntimeContextBuilder().build(inputCode));
            return this;
        } catch (InterpreterException e) {
            throw new AssertionError(e.getMessage());
        }
    }

    public Assertions constants(Object constants) {
        this.constants = constants;
        return this;
    }
}
