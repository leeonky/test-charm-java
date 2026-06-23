package org.testcharm.dal.ast.node.table;

import org.testcharm.dal.ast.node.*;
import org.testcharm.dal.ast.opt.Factory;
import org.testcharm.dal.runtime.Data;
import org.testcharm.dal.runtime.RuntimeContextBuilder;
import org.testcharm.interpreter.Clause;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.testcharm.dal.ast.node.DALExpression.expression;
import static org.testcharm.dal.ast.node.SymbolNode.Type.BRACKET;
import static org.testcharm.dal.compiler.Notations.EMPTY;
import static org.testcharm.util.function.When.when;

public class SpecifyIndexRowType extends RowType {
    @Override
    public RowType merge(RowType current) {
        return current.mergeBy(this);
    }

    @Override
    protected RowType mergeBy(SpecifyIndexRowType last) {
        return this;
    }

    @Override
    protected RowType mergeBy(SpecifyPropertyRowType last) {
        return last;
    }

    @Override
    public DALNode constructVerificationNode(Data<?> actual, Stream<Clause<DALNode>> rowClauses,
                                             Comparator<Data<?>> comparator) {
        List<DALNode> rowNodes = rowClauses.map(rowClause -> rowClause.expression(null))
                .collect(toList());
        if (actual.isList())
            return new ListScopeNode(rowNodes, ListScopeNode.Type.FIRST_N_ITEMS, comparator, ListScopeNode.Style.TABLE);
        return new ObjectScopeNode(rowNodes);
    }

    @Override
    public DALNode constructAccessingRowNode(DALNode input, Optional<DALNode> indexOrKey, RuntimeContextBuilder.DALRuntimeContext context) {
        return indexOrKey.flatMap(node -> indexToExpression(node, context)).orElseThrow(IllegalStateException::new);
    }

    static Optional<DALNode> indexToExpression(DALNode node, RuntimeContextBuilder.DALRuntimeContext context) {
        return when(node instanceof LiteralNode).optional(() -> expression(new InputNode.StackInput(context), Factory.executable(EMPTY),
                new SymbolNode(((LiteralNode) node).getValue(), BRACKET).setPositionBegin(node.getPositionBegin())));
    }
}
