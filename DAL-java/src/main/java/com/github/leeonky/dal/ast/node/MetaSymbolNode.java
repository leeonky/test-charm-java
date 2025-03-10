package com.github.leeonky.dal.ast.node;

import com.github.leeonky.dal.runtime.*;
import com.github.leeonky.interpreter.InterpreterException;

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
            } catch (InterpreterException | ExpressionException e) {
                throw e;
            } catch (AssertionError e) {
                throw new AssertionFailure(e.getMessage(), getPositionBegin());
            } catch (Exception e) {
                throw new DalException(getPositionBegin(), e);
            }
        }, null).mapError(e -> {
            if (e instanceof AssertionError) {
                throw new AssertionFailure(e.getMessage(), getPositionBegin());
            }
            if (e instanceof InterruptedException || e instanceof ExpressionException)
                return e;
            return locateError(e, getPositionBegin());
        });
    }
}
