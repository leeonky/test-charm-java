package com.github.leeonky.dal.ast.node;

import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder;

import static com.github.leeonky.dal.runtime.ExpressionException.opt2;

public class ListMappingNodeMeta extends ListMappingNode {
    public ListMappingNodeMeta(DALNode symbolNode) {
        super(symbolNode);
    }

    @Override
    public Data<?> getValue(DALNode left, RuntimeContextBuilder.DALRuntimeContext context) {
        return context.data(opt2(left.evaluateData(context)::list).autoMapping(item ->
                context.invokeMetaProperty(left, item, getRootSymbolName())));
    }
}
