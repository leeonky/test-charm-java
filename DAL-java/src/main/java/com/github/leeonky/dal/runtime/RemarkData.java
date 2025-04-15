package com.github.leeonky.dal.runtime;

import com.github.leeonky.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;

public class RemarkData<T> extends RuntimeData<T> {
    private final String remark;

    public RemarkData(Data<T> data, DALRuntimeContext runtimeContext, String remark) {
        super(data, runtimeContext);
        this.remark = remark;
    }

    public String remark() {
        return remark;
    }
}
