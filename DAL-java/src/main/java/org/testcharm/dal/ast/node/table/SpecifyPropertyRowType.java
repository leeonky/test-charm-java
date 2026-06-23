package org.testcharm.dal.ast.node.table;

import org.testcharm.dal.ast.node.DALNode;
import org.testcharm.dal.ast.node.ObjectScopeNode;
import org.testcharm.dal.runtime.Data;
import org.testcharm.dal.runtime.RuntimeContextBuilder;
import org.testcharm.interpreter.Clause;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.testcharm.dal.ast.node.table.SpecifyIndexRowType.indexToExpression;

public class SpecifyPropertyRowType extends RowType {

    @Override
    public RowType merge(RowType current) {
        return current.mergeBy(this);
    }

    @Override
    protected RowType mergeBy(SpecifyPropertyRowType last) {
        return this;
    }

    @Override
    protected RowType mergeBy(SpecifyIndexRowType last) {
        return this;
    }

    @Override
    public DALNode constructVerificationNode(Data<?> actual, Stream<Clause<DALNode>> rowClauses,
                                             Comparator<Data<?>> comparator) {
        return new ObjectScopeNode(rowClauses.map(rowNode -> rowNode.expression(null)).collect(toList()));
    }

    @Override
    public DALNode constructAccessingRowNode(DALNode input, Optional<DALNode> indexOrKey, RuntimeContextBuilder.DALRuntimeContext context) {
        return indexOrKey.map(node -> indexToExpression(node, context).orElse(node)).orElseThrow(IllegalStateException::new);
    }
}
