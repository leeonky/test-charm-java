package com.github.leeonky.dal.runtime.checker;

import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder;

import java.math.BigDecimal;

public class MatchesChecker implements Checker {

    @Override
    public Data<?> transformActual(Data<?> actual, Data<?> expected, RuntimeContextBuilder.DALRuntimeContext context) {
        return actual.convert(expected.instance().getClass());
    }

    @Override
    public String message(CheckingContext checkingContext) {
        return checkingContext.messageMatch();
    }

    @Override
    public boolean failed(CheckingContext checkingContext) {
        return checkingContext.getExpected().cast(BigDecimal.class).flatMap(number1 ->
                        checkingContext.getActual().cast(BigDecimal.class).map(number2 ->
                                number1.compareTo(number2) != 0))
                .orElseGet(() -> Checker.super.failed(checkingContext));
    }
}
