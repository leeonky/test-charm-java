package com.github.leeonky.dal.ast.node;

import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder;

import java.util.Collections;
import java.util.List;

public abstract class InputNode extends DALNode {

    @Override
    public Data<?> evaluateData(RuntimeContextBuilder.DALRuntimeContext context) {
        return context.getThis();
    }

    @Override
    public String inspect() {
        return "";
    }

    @Override
    public List<Object> propertyChain() {
        return Collections.emptyList();
    }

    public static class Root extends InputNode {
        public static final InputNode.Root INSTANCE = new Root();
    }

    public static class Placeholder extends InputNode {
        public static final InputNode.Placeholder INSTANCE = new Placeholder();

        @Override
        public Data<?> evaluateData(RuntimeContextBuilder.DALRuntimeContext context) {
            throw new IllegalStateException("Should not evaluate placeholder node");
        }
    }

    public static class StackInput extends InputNode {
        private final RuntimeContextBuilder.DALRuntimeContext context;

        public StackInput(RuntimeContextBuilder.DALRuntimeContext context) {
            this.context = context;
        }

        @Override
        public int getPositionBegin() {
            return context.lastPosition();
        }
    }
}
