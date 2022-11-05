package com.github.leeonky.dal.extensions.basic.string.util;

import com.github.leeonky.dal.extensions.basic.Diff;
import com.github.leeonky.dal.runtime.Checker;
import com.github.leeonky.dal.runtime.CheckingContext;
import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;

import java.util.Optional;

import static java.util.Optional.of;

public class CharSequenceEqualsChecker implements Checker {
    private static final Optional<Checker> CHECKER = of(new CharSequenceEqualsChecker());

    public static Optional<Checker> factory(Data d1, Data d2) {
        return CHECKER;
    }

    @Override
    public Data transformActual(Data actual, Data expected, DALRuntimeContext context) {
        return context.wrap(actual.getInstance().toString());
    }

    @Override
    public Data transformExpected(Data expected, DALRuntimeContext context) {
        return context.wrap(expected.getInstance().toString());
    }

    @Override
    public String message(CheckingContext checkingContext) {
        String message = checkingContext.verificationMessage(getPrefix(), "");
        if (checkingContext.getOriginalActual().isNull())
            return message;
        String detail = new Diff(convertToString(checkingContext.getOriginalExpected().getInstance()),
                convertToString(checkingContext.getOriginalActual().getInstance())).detail();
        return detail.isEmpty() ? message : message + "\n\n" + detail;
    }

    protected String getPrefix() {
        return "Expected to be equal to: ";
    }

    protected String convertToString(Object object) {
        return ((CharSequence) object).toString();
    }
}
