package com.github.leeonky.dal;

import com.github.leeonky.dal.ast.node.DALNode;
import com.github.leeonky.dal.compiler.Compiler;
import com.github.leeonky.dal.compiler.Notations;
import com.github.leeonky.dal.runtime.Extension;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;
import com.github.leeonky.dal.type.InputCode;
import com.github.leeonky.dal.util.TextUtil;
import com.github.leeonky.interpreter.SourceCode;
import com.github.leeonky.interpreter.SyntaxException;
import com.github.leeonky.util.Classes;

import java.util.*;
import java.util.stream.Collectors;

import static com.github.leeonky.util.Classes.subTypesOf;
import static com.github.leeonky.util.function.Extension.not;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Stream.concat;

public class DAL {
    private final Compiler compiler = new Compiler();
    private final RuntimeContextBuilder runtimeContextBuilder = new RuntimeContextBuilder();
    private static final ThreadLocal<DAL> instance = new ThreadLocal<>();
    private static final ThreadLocal<Map<String, DAL>> instances = new ThreadLocal<>();
    private final String name;

    @Deprecated
    public DAL() {
        name = String.valueOf(hashCode());
    }

    public static synchronized DAL getInstance() {
        if (instance.get() == null)
            instance.set(create("Default"));
        return instance.get();
    }

    @Deprecated
    public static DAL create(Class<?>... exceptExtensions) {
        Iterator<DALFactory> iterator = ServiceLoader.load(DALFactory.class).iterator();
        if (iterator.hasNext())
            return iterator.next().newInstance();
        return new DAL().extend(exceptExtensions);
    }

    public DAL(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static synchronized DAL getInstance(String name) {
        Map<String, DAL> dalMaps = instances.get();
        if (dalMaps == null) {
            dalMaps = new HashMap<>();
            instances.set(dalMaps);
        }
        return dalMaps.computeIfAbsent(name, DAL::create);
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

    @SuppressWarnings("unchecked")
    public <T> List<T> evaluateAll(InputCode<Object> input, String expressions) {
        DALRuntimeContext runtimeContext = runtimeContextBuilder.build(input);
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

    @SuppressWarnings("unchecked")
    public <T> T evaluate(InputCode<Object> input, String expression, Class<?> rootSchema) {
        DALRuntimeContext runtimeContext = runtimeContextBuilder.build(input, rootSchema);
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
        concat(subTypesOf(Extension.class, "com.github.leeonky.dal.extensions").stream(),
                subTypesOf(Extension.class, "com.github.leeonky.extensions.dal").stream())
                .filter(not(exceptExtensions::contains))
                .map(Classes::newInstance)
                .sorted(Comparator.comparing(Extension::order))
                .forEach(e -> e.extend(this));
        return this;
    }
}
