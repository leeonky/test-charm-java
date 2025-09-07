package com.github.leeonky.dal.ast.node.table;

import com.github.leeonky.dal.ast.node.InputNode;
import com.github.leeonky.dal.ast.node.SortGroupNode;
import com.github.leeonky.dal.ast.node.SymbolNode;
import com.github.leeonky.dal.ast.opt.Factory;
import com.github.leeonky.dal.compiler.Notations;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder;

import static com.github.leeonky.dal.ast.node.DALExpression.expression;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;

public class DefaultIndexColumnHeaderRow extends ColumnHeaderRow {

    public DefaultIndexColumnHeaderRow() {
        super(emptyList());
    }

    @Override
    public ColumnHeader getHeader(int index, RuntimeContextBuilder.DALRuntimeContext context) {
        return new ColumnHeader(SortGroupNode.NO_SEQUENCE, expression(new InputNode.StackInput(context),
                Factory.executable(Notations.EMPTY), new SymbolNode(index, SymbolNode.Type.NUMBER)), empty());
    }

    @Override
    public void checkDataCellSize(Row rowNode) {
    }

    @Override
    public String inspect() {
        return "^";
    }
}
