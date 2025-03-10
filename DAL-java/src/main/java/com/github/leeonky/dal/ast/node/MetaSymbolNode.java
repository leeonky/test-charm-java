package com.github.leeonky.dal.ast.node;

import com.github.leeonky.dal.runtime.*;
import com.github.leeonky.interpreter.InterpreterException;

public class MetaSymbolNode extends SymbolNode {
    public MetaSymbolNode(String content) {
        super(content, Type.SYMBOL);
    }

    @Override
    public Data getValue(DALNode left, RuntimeContextBuilder.DALRuntimeContext context) {
        try {
            return context.wrap(context.invokeMetaProperty(new MetaData(left, getRootSymbolName(), context)));
        } catch (InterpreterException | ExpressionException e) {
            throw e;
        } catch (Exception e) {
            throw new DalException(getPositionBegin(), e);
        }
    }
}
