package com.github.leeonky.dal.runtime.checker;

import com.github.leeonky.dal.runtime.DalRuntimeException;
import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder;
import com.github.leeonky.util.ConvertException;

import java.math.BigDecimal;

public class MatchesChecker implements Checker {

    @Override
    public Data transformActual(Data actual, Data expected, RuntimeContextBuilder.DALRuntimeContext context) {
        try {
            return actual.convert(expected.instance().getClass());
        } catch (ConvertException e) {
            throw new DalRuntimeException(e.getMessage());
        }
    }

    @Override
    public String message(CheckingContext checkingContext) {
        return checkingContext.messageMatch();
    }

    @Override
    public boolean failed(CheckingContext checkingContext) {
        if (checkingContext.getExpected().instanceOf(BigDecimal.class)
                && checkingContext.getActual().instanceOf(BigDecimal.class))
            return ((BigDecimal) checkingContext.getExpected().instance())
                    .compareTo((BigDecimal) checkingContext.getActual().instance()) != 0;
        return Checker.super.failed(checkingContext);
    }
}
