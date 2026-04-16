package org.testcharm.dal;

import org.testcharm.dal.ast.node.DALNode;
import org.testcharm.dal.compiler.Compiler;
import org.testcharm.dal.compiler.Notations;
import org.testcharm.dal.runtime.Data;
import org.testcharm.dal.runtime.Extension;
import org.testcharm.dal.runtime.RuntimeContextBuilder;
import org.testcharm.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;
import org.testcharm.dal.type.InputCode;
import org.testcharm.dal.util.TextUtil;
import org.testcharm.interpreter.SourceCode;
import org.testcharm.interpreter.SyntaxException;
import org.testcharm.util.Classes;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Stream.concat;
import static org.testcharm.util.Classes.subTypesOf;
import static org.testcharm.util.function.Extension.not;

public class DAL {
    private final Compiler compiler = new Compiler();
    private final RuntimeContextBuilder runtimeContextBuilder = new RuntimeContextBuilder();
    private static final ThreadLocal<DAL> instance = ThreadLocal.withInitial(() -> dal("Default"));
    private static final ThreadLocal<Map<String, DAL>> instances = ThreadLocal.withInitial(HashMap::new);
    private final String name;

    @Deprecated
    public DAL() {
        name = String.valueOf(hashCode());
    }

    @Deprecated
    public static synchronized DAL getInstance() {
        return dal();
    }

    @Deprecated
    public static DAL create(Class<?>... exceptExtensions) {
        Iterator<DALFactory> iterator = ServiceLoader.load(DALFactory.class).iterator();
        if (iterator.hasNext())
            return iterator.next().newInstance();
        return new DAL().extend(exceptExtensions);
    }

    public static DAL dal() {
        return instance.get();
    }

    public DAL(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static synchronized DAL dal(String name) {
        return instances.get().computeIfAbsent(name, DAL::create);
    }

    public static DAL create(String name, Class<?>... exceptExtensions) {
        Iterator<DALFactory> iterator = ServiceLoader.load(DALFactory.class).iterator();
        if (iterator.hasNext())
            return iterator.next().newInstance();
        return new DAL(name).extend(exceptExtensions);
    }

    public RuntimeContextBuilder getRuntimeContextBuilder() {
        return runtimeContextBuilder;
    }

    public <T> List<T> evaluateAll(Object input, String expressions) {
        return evaluateAll(() -> input, expressions);
    }

    public <T> List<T> evaluateAll(InputCode<Object> input, String expressions) {
        return evaluateAll(input, expressions, null);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> evaluateAll(InputCode<Object> input, String expressions, Object constants) {
        DALRuntimeContext runtimeContext = runtimeContextBuilder.build(input, null, constants);
        try {
            return compile(expressions, runtimeContext).stream()
                    .map(node -> (T) node.evaluate(runtimeContext))
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            if (!runtimeContext.hookError(expressions, e))
                throw e;
            return emptyList();
        }
    }

    public <T> T evaluate(Object input, String expression) {
        return evaluate(() -> input, expression);
    }

    public <T> T evaluate(InputCode<Object> input, String expression) {
        return evaluate(input, expression, null);
    }

    public <T> T evaluate(InputCode<Object> input, String expression, Class<?> rootSchema) {
        return evaluate(input, expression, rootSchema, null);
    }

    @SuppressWarnings("unchecked")
    public <T> T evaluate(InputCode<Object> input, String expression, Class<?> rootSchema, Object constants) {
        DALRuntimeContext runtimeContext = runtimeContextBuilder.build(input, rootSchema, constants);
        try {
            return (T) compileSingle(expression, runtimeContext).evaluate(runtimeContext);
        } catch (Throwable e) {
            if (!runtimeContext.hookError(expression, e))
                throw e;
            return null;
        }
    }

    public DALNode compileSingle(String expression, DALRuntimeContext runtimeContext) {
        List<DALNode> nodes = compile(expression, runtimeContext);
        if (nodes.size() > 1)
            throw new SyntaxException("more than one expression", getOperandPosition(nodes.get(1)));
        return nodes.get(0);
    }

    public List<DALNode> compile(String expression, DALRuntimeContext runtimeContext) {
        return compiler.compile(new SourceCode(format(expression), Notations.LINE_COMMENTS),
                runtimeContext);
    }

    private int getOperandPosition(DALNode node) {
        return node.getPositionBegin() == 0 ? node.getOperandPosition() : node.getPositionBegin();
    }

    private String format(String expression) {
        return String.join("\n", TextUtil.lines(expression));
    }

    public DAL extend(Class<?>... excepts) {
        Set<Class<?>> exceptExtensions = new HashSet<>(asList(excepts));
        concat(subTypesOf(Extension.class, "org.testcharm.dal.extensions").stream(),
                subTypesOf(Extension.class, "org.testcharm.extensions.dal").stream())
                .filter(not(exceptExtensions::contains))
                .map(Classes::newInstance)
                .sorted(Comparator.comparing(Extension::order))
                .forEach(e -> e.extend(this));
        return this;
    }

    public Data<?> wrap(Object object) {
        return getRuntimeContextBuilder().build(object).getThis();
    }
}
