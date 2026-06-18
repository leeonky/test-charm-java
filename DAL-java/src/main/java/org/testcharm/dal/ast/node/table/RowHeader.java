package org.testcharm.dal.ast.node.table;

import org.testcharm.dal.ast.node.DALExpression;
import org.testcharm.dal.ast.node.DALNode;
import org.testcharm.dal.ast.node.InputNode;
import org.testcharm.dal.ast.opt.DALOperator;
import org.testcharm.dal.compiler.DALProcedure;
import org.testcharm.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;
import org.testcharm.interpreter.Clause;
import org.testcharm.interpreter.Operator;
import org.testcharm.interpreter.OperatorParser;

import java.util.Optional;

import static org.testcharm.dal.ast.node.DALExpression.expression;
import static org.testcharm.util.function.Extension.getFirstPresent;

public class RowHeader extends DALNode {
    public static final RowType DEFAULT_INDEX = new DefaultIndexRowType();
    public static final RowType SPECIFY_INDEX = new SpecifyIndexRowType();
    public static final RowType SPECIFY_PROPERTY = new SpecifyPropertyRowType();
    private final Optional<DALNode> indexOrProperty;
    private final Optional<Clause<DALNode>> clause;
    private final Optional<DALOperator> rowOperator;

    public RowHeader(Optional<DALNode> indexOrProperty, Optional<Clause<DALNode>> clause,
                     Optional<DALOperator> rowOperator) {
        this.rowOperator = rowOperator;
        this.indexOrProperty = indexOrProperty;
        this.clause = clause;
    }

    @Override
    public String inspect() {
        String indexAndSchema = (indexOrProperty.map(DALNode::inspect).orElse("") + " " + clause.map(clause ->
                clause.expression(null).inspect()).orElse("")).trim();
        return rowOperator.map(dalOperator -> dalOperator.inspect(indexAndSchema, "").trim()).orElse(indexAndSchema);
    }

    public DALNode makeExpressionWithOptionalIndexAndSchema(RowType rowType, DALNode input,
                                                            DALOperator defaultOperator, DALNode expectedRow,
                                                            DALRuntimeContext context) {
        DALNode rowAccessor = rowType.constructAccessingRowNode(input, indexOrProperty, context);
        return expression(clause.map(clause -> clause.expression(rowAccessor)).orElse(rowAccessor),
                rowOperator.orElse(defaultOperator), expectedRow);
    }

    public OperatorParser<DALRuntimeContext, DALNode, DALOperator, DALProcedure, DALExpression> operator() {
        return procedure -> rowOperator;
    }

    public RowType resolveRowType() {
        return indexOrProperty.map(DALNode::guessTableHeaderType).orElseGet(this::guessTableHeaderType);
    }

    public Optional<Integer> position() {
        return getFirstPresent(() -> indexOrProperty.map(DALNode::getPositionBegin),
                () -> clause.map(c -> ((DALExpression) c.expression(InputNode.Placeholder.INSTANCE)).operator().getPosition()),
                () -> rowOperator.map(Operator::getPosition));
    }
}