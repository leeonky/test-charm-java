package com.github.leeonky.dal.ast.node;

import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.MetaData;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder;
import com.github.leeonky.dal.runtime.RuntimeException;
import com.github.leeonky.interpreter.InterpreterException;

public class MetaSymbolNode extends SymbolNode {
    public MetaSymbolNode(String content) {
        super(content, Type.SYMBOL);
    }

    @Override
    public Data getValue(DALNode left, RuntimeContextBuilder.DALRuntimeContext context) {
        try {
            MetaData metaData = new MetaData(left, this, context);
            return context.wrap(context.fetchMetaFunction(metaData).apply(metaData));
        } catch (InterpreterException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), getPositionBegin());
        }
    }
}
