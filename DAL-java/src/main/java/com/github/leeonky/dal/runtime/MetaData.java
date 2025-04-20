package com.github.leeonky.dal.runtime;

import com.github.leeonky.dal.ast.node.DALNode;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static com.github.leeonky.dal.runtime.ExpressionException.illegalOp2;
import static java.lang.String.format;

public class MetaData<T> extends RuntimeData<T> {
    private DALNode inputNode;
    private final Object name;

    public MetaData(DALNode inputNode, Data<T> inputData, Object symbolName, DALRuntimeContext runtimeContext) {
        super(inputData, runtimeContext);
        this.inputNode = inputNode;
        name = symbolName;
    }

    private MetaData(DALRuntimeContext runtimeContext, Data<T> data, String name) {
        super(data, runtimeContext);
        this.name = name;
    }

    private final List<Class<?>> callTypes = new ArrayList<>();

    public Object callSuper() {
        return runtimeContext().fetchSuperMetaFunction(this).orElseThrow(() -> illegalOp2(format(
                        "Local meta property `%s` has no super in type %s", name, callTypes.get(callTypes.size() - 1).getName())))
                .handle(this);
    }

//    public Object callSuper(Supplier<Object> supplier) {
//        setData(() -> {
//            Object newData = supplier.get();
//            checkType(newData);
//            return runtimeContext.wrap(newData);
//        });
//        return callSuper();
//    }

    public Object callGlobal() {
        return runtimeContext().fetchGlobalMetaFunction(this).handle(this);
    }

//    TODO
//    public Object callGlobal(Supplier<Object> supplier) {
//        setData(() -> runtimeContext.wrap(supplier.get()));
//        return callGlobal();
//    }

    private MetaData<T> rename(String name) {
        return new MetaData<>(runtimeContext, data, name);
    }

    public Object callMeta(String another) {
        MetaData<T> metaData = rename(another);
        return runtimeContext().fetchGlobalMetaFunction(metaData).handle(metaData);
    }

    public Data<?> delegate(Function<Data<T>, Data<?>> sub) {
        return runtimeContext.invokeMetaProperty(inputNode, sub.apply(data), name);
    }

//    TODO
//    public Object callMeta(String another, Supplier<Object> supplier) {
//        MetaData metaData = newMeta(another);
//        metaData.setData(() -> runtimeContext.wrap(supplier.get()));
//        return runtimeContext().fetchGlobalMetaFunction(metaData).apply(metaData);
//    }

    private void checkType(Object data) {
        Class<?> expect = this.data.value().getClass();
        Class<?> actual = Objects.requireNonNull(data).getClass();
        if (actual.isAnonymousClass())
            actual = actual.getSuperclass();
        if (!actual.equals(expect))
            throw illegalOp2(format("Do not allow change data type in callSuper, expect %s but %s",
                    expect.getName(), actual.getName()));
    }

    public void addCallType(Class<?> callType) {
        callTypes.add(callType);
    }

    public boolean calledBy(Class<?> type) {
        return callTypes.contains(type);
    }

    public boolean isInstance(Class<?> type) {
        return data.instanceOf(type);
    }

    public Object name() {
        return name;
    }

    public DALNode inputNode() {
        return inputNode;
    }
}
