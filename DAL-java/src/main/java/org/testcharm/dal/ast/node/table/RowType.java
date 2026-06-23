package org.testcharm.dal.ast.node.table;

import org.testcharm.dal.ast.node.DALNode;
import org.testcharm.dal.runtime.Data;
import org.testcharm.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;
import org.testcharm.interpreter.Clause;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

public abstract class RowType {

    public abstract RowType merge(RowType current);

    protected RowType mergeBy(SpecifyIndexRowType last) {
        throw new IllegalArgumentException();
    }

    protected RowType mergeBy(DefaultIndexRowType last) {
        throw new IllegalArgumentException();
    }

    protected RowType mergeBy(SpecifyPropertyRowType last) {
        throw new IllegalArgumentException();
    }

    public abstract DALNode constructVerificationNode(Data<?> actual, Stream<Clause<DALNode>> rowClauses,
                                                      Comparator<Data<?>> comparator);

    public DALNode constructAccessingRowNode(DALNode input, Optional<DALNode> indexOrKey, DALRuntimeContext context) {
        return input;
    }
}