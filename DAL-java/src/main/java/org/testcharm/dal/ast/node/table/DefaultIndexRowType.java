package org.testcharm.dal.ast.node.table;

import org.testcharm.dal.ast.node.DALNode;
import org.testcharm.dal.ast.node.ListScopeNode;
import org.testcharm.dal.runtime.Data;
import org.testcharm.interpreter.Clause;

import java.util.Comparator;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class DefaultIndexRowType extends RowType {
    @Override
    public RowType merge(RowType current) {
        return current.mergeBy(this);
    }

    @Override
    protected RowType mergeBy(DefaultIndexRowType last) {
        return this;
    }

    @Override
    public DALNode constructVerificationNode(Data<?> actual, Stream<Clause<DALNode>> rowClauses,
                                             Comparator<Data<?>> comparator) {
        return new ListScopeNode(rowClauses.collect(toList()), comparator, ListScopeNode.Style.TABLE);
    }
}
