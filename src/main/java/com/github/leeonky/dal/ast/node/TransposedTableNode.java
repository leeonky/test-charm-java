package com.github.leeonky.dal.ast.node;

import com.github.leeonky.dal.ast.node.table.TransposedBody;
import com.github.leeonky.dal.ast.node.table.TransposedRowHeaderRow;
import com.github.leeonky.dal.ast.opt.DALOperator;
import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.Expectation;
import com.github.leeonky.dal.runtime.RowAssertionFailure;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;

public class TransposedTableNode extends DALNode {
    private final TransposedRowHeaderRow tableHead;
    private final TransposedBody tableBody;

    public TransposedTableNode(DALNode transposeTableHead, DALNode transposedTableBody) {
        tableHead = (TransposedRowHeaderRow) transposeTableHead;
        tableBody = ((TransposedBody) transposedTableBody).checkFormat(tableHead);
    }

    @Override
    public Data evaluateData(DALRuntimeContext context) {
        return context.wrap(new Expectation() {
            @Override
            public Data equalTo(DALOperator operator, Data actual) {
                try {
                    return ((Expectation) transpose().convertToVerificationNode(actual, operator, context)
                            .evaluateData(context).instance()).equalTo(operator, actual);
                } catch (RowAssertionFailure rowAssertionFailure) {
                    throw rowAssertionFailure.columnPositionException(TransposedTableNode.this);
                }
            }

            @Override
            public Data matches(DALOperator operator, Data actual) {
                try {
                    return ((Expectation) transpose().convertToVerificationNode(actual, operator, context)
                            .evaluateData(context).instance()).matches(operator, actual);
                } catch (RowAssertionFailure rowAssertionFailure) {
                    throw rowAssertionFailure.columnPositionException(TransposedTableNode.this);
                }
            }
        });
    }
   
    public TableNode transpose() {
        return (TableNode) new TableNode(tableBody.transposeHead(), tableBody.transpose(tableHead))
                .setPositionBegin(getPositionBegin());
    }

    @Override
    public String inspect() {
        return tableHead.inspect() + tableBody.inspect();
    }
}
