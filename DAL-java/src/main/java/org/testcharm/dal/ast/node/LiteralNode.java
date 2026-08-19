package org.testcharm.dal.ast.node;

import org.testcharm.dal.ast.node.table.RowHeader;
import org.testcharm.dal.ast.node.table.RowType;
import org.testcharm.dal.runtime.RuntimeContextBuilder;
import org.testcharm.util.NumberFormat;
import org.testcharm.util.NumberWithFormat;

public class LiteralNode extends DALNode {

    private final Object value;
    private final NumberFormat numberFormat;

    public LiteralNode(Object value) {
        this.value = value;
        numberFormat = null;
    }

    public LiteralNode(NumberWithFormat numberWithFormat) {
        value = numberWithFormat.number;
        numberFormat = numberWithFormat.format;
    }

    public LiteralNode() {
        value = null;
        numberFormat = null;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public Object evaluate(RuntimeContextBuilder.DALRuntimeContext context) {
        return value;
    }

    @Override
    public String inspect() {
        if (value == null)
            return "null";
        if (value instanceof String)
            return String.format("'%s'", value);
        return value.toString();
    }

    @Override
    public RowType guessTableHeaderType() {
        return RowHeader.SPECIFY_INDEX;
    }

    @Override
    public boolean needPostBlankWarningCheck() {
        return true;
    }
}
