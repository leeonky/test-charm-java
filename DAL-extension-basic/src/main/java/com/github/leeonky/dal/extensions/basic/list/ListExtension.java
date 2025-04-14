package com.github.leeonky.dal.extensions.basic.list;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.ast.opt.DALOperator;
import com.github.leeonky.dal.runtime.*;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;

import static com.github.leeonky.dal.runtime.ExpressionException.opt1;
import static com.github.leeonky.dal.runtime.ExpressionException.opt2;
import static java.lang.Integer.parseInt;
import static java.lang.String.format;

public class ListExtension implements Extension {

    @Override
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder()
                .registerMetaProperty("top", metaData -> (Callable<Integer, DALCollection<Object>>)
                        opt2(metaData.data()::list)::limit)
                .registerMetaProperty("filter", metaData -> new Filterable(metaData.data()))
                .registerOperator(Operators.MATCH, new VerificationInFilter())
                .registerOperator(Operators.EQUAL, new VerificationInFilter())
                .registerExclamation(Filterable.class, runtimeData -> runtimeData.data().map(
                        data -> ((Filterable) data).requireNotEmpty()))
                .registerDataRemark(Filterable.class, remarkData -> remarkData.data().map(
                        data -> ((Filterable) data).require(parseInt(remarkData.remark()))));
    }

    public static class Filterable {
        private final Data<?> data;

        public Filterable(Data<?> data) {
            this.data = data;
        }

        public DALCollection<Object> filter(DALOperator operator, Data v2, DALRuntimeContext context) {
            return filterList(operator, v2, context);
        }

        protected DALCollection<Object> filterList(DALOperator operator, Data<?> v2, DALRuntimeContext context) {
            return opt1(data::list).wraps().filter(element -> {
                try {
                    context.calculate(element, operator, v2);
                    return true;
                } catch (Throwable ig) {
                    return false;
                }
            }).map((i, d) -> d.instance());
        }

        public Filterable requireNotEmpty() {
            return new Filterable(data) {
                @Override
                protected DALCollection<Object> filterList(DALOperator operator, Data<?> v2, DALRuntimeContext context) {
                    DALCollection<Object> list = super.filterList(operator, v2, context);
                    if (!list.iterator().hasNext())
                        throw ExpressionException.exception(expression -> new NotReadyException(
                                "Filtered result is empty, try again", expression.left().getOperandPosition()));
                    return list;
                }
            };
        }

        public Filterable require(int size) {
            return new Filterable(data) {

                @Override
                protected DALCollection<Object> filterList(DALOperator operator, Data<?> v2, DALRuntimeContext context) {
                    DALCollection<Object> list = super.filterList(operator, v2, context).limit(size);
                    if (list.size() >= size)
                        return list;
                    throw ExpressionException.exception(expression -> new NotReadyException(
                            format("There are only %d elements, try again", list.size()), expression.left().getOperandPosition()));
                }
            };
        }
    }

    private static class VerificationInFilter implements Operation {

        @Override
        public boolean match(Data<?> v1, DALOperator operator, Data<?> v2, DALRuntimeContext context) {
            return v1.instanceOf(Filterable.class);
        }

        @Override
        public Object operate(Data<?> v1, DALOperator operator, Data<?> v2, DALRuntimeContext context) {
            return ((Filterable) v1.instance()).filter(operator, v2, context);
        }
    }
}
