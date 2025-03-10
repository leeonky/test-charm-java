package com.github.leeonky.dal.ast.node;

import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.MetaData;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder;

import static com.github.leeonky.dal.runtime.DalException.locateError;

public class MetaSymbolNode extends SymbolNode {
    public MetaSymbolNode(String content) {
        super(content, Type.SYMBOL);
    }

    @Override
    public Data getValue(DALNode left, RuntimeContextBuilder.DALRuntimeContext context) {
        return context.wrap(() -> {
            try {
                return context.invokeMetaProperty(new MetaData(left, getRootSymbolName(), context));
            } catch (Throwable e) {
                throw locateError(e, getPositionBegin());
            }
        }, null).mapError(e -> locateError(e, getPositionBegin()));
    }
}
