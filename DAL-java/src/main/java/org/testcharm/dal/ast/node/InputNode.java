package org.testcharm.dal.ast.node;

import org.testcharm.dal.runtime.Data;
import org.testcharm.dal.runtime.RuntimeContextBuilder;

public abstract class InputNode extends DALNode {

    @Override
    public Data<?> evaluateData(RuntimeContextBuilder.DALRuntimeContext context) {
        return context.getThis();
    }

    @Override
    public String inspect() {
        return "";
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
