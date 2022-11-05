package com.github.leeonky.dal.extensions.basic.string.util;

import com.github.leeonky.dal.extensions.basic.Diff;
import com.github.leeonky.dal.runtime.Checker;
import com.github.leeonky.dal.runtime.CheckingContext;
import com.github.leeonky.dal.runtime.Data;

import java.util.Optional;

import static java.util.Optional.of;

public class CharSequenceMatcherChecker implements Checker {
    private static final Optional<Checker> MATCHES_CHECKER = of(new CharSequenceMatcherChecker());

    public static Optional<Checker> factory(Data d1, Data d2) {
        return MATCHES_CHECKER;
    }

    @Override
    public boolean failed(CheckingContext checkingContext) {
        return !convertToString(checkingContext.getOriginalExpected().getInstance())
                .equals(convertToString(checkingContext.getOriginalActual().convert(String.class).getInstance()));
    }

    protected String getPrefix() {
        return "Expected to match: ";
    }

    protected String convertToString(Object object) {
        return object == null ? null : object.toString();
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
}
