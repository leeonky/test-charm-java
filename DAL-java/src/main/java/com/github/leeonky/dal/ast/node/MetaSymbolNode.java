package com.github.leeonky.dal.ast.node;

import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder;
import com.github.leeonky.dal.runtime.SchemaType;

import static com.github.leeonky.dal.runtime.DalException.locateError;

public class MetaSymbolNode extends SymbolNode {
    public MetaSymbolNode(String content) {
        super(content, Type.SYMBOL);
    }

    @Override
    public Data<?> getValue(DALNode left, RuntimeContextBuilder.DALRuntimeContext context) {
        Data<?> inputData = context.lazy(() -> left.evaluateData(context).instance(), SchemaType.create(null));
        try {
            return context.invokeMetaProperty(left, inputData, getRootSymbolName());
        } catch (Throwable e) {
            throw locateError(e, getPositionBegin());
        }
    }
}
