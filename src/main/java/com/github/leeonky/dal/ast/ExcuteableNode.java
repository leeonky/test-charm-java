package com.github.leeonky.dal.ast;

import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder;

//TODO incorrect word
public interface ExcuteableNode {
    Data getPropertyValue(Data data, RuntimeContextBuilder.DALRuntimeContext context);
}
