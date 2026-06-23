package org.testcharm.dal.ast.node.table;

import org.testcharm.dal.ast.node.DALNode;
import org.testcharm.dal.ast.node.ListScopeNode;
import org.testcharm.dal.ast.node.ObjectScopeNode;
import org.testcharm.dal.runtime.Data;
import org.testcharm.interpreter.Clause;

import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

class EmptyTableRowType extends RowType {

    @Override
    public RowType merge(RowType current) {
        return current;
    }

    @Override
    public DALNode constructVerificationNode(Data<?> actual, Stream<Clause<DALNode>> rowClauses,
                                             Comparator<Data<?>> comparator) {
        return actual.isList() ? new ListScopeNode(rowClauses.collect(toList()), comparator, ListScopeNode.Style.TABLE)
                : new ObjectScopeNode(Collections.emptyList());
    }
}
