package org.testcharm.dal.runtime;

import org.testcharm.dal.ast.opt.DALOperator;

public interface ExpectationFactory {
    Expectation create(DALOperator operator, Data<?> actual);

    interface Expectation {
        Data<?> matches();

        Data<?> equalTo();

//        Type type();
    }

//    enum Type {
//        OBJECT, REGEX, VALUE, LIST
//    }
}
