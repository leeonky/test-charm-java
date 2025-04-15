package com.github.leeonky.dal.runtime;

import com.github.leeonky.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;

public class RuntimeData<T> {
    protected final DALRuntimeContext runtimeContext;
    protected final Data<T> data;

    public RuntimeData(Data<T> data, DALRuntimeContext runtimeContext) {
        this.runtimeContext = runtimeContext;
        this.data = data;
    }

    public DALRuntimeContext runtimeContext() {
        return runtimeContext;
    }

    public Data<T> data() {
        return data;
    }
}
