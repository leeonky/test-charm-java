package com.github.leeonky.dal.extensions.basic.string.util;

import com.github.leeonky.dal.extensions.basic.CheckerType;
import com.github.leeonky.dal.extensions.basic.CheckerWithDiff;
import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;
import com.github.leeonky.dal.runtime.checker.Checker;
import com.github.leeonky.dal.runtime.checker.CheckingContext;

import java.util.Optional;

import static java.util.Optional.of;

public abstract class CharSequenceChecker extends CheckerWithDiff {
    public static Optional<Checker> equals(Data d1, Data d2) {
        return Equals.INSTANCE;
    }

    public static Optional<Checker> matches(Data d1, Data d2) {
        return Matches.INSTANCE;
    }

    @Override
    public Data transformExpected(Data expected, DALRuntimeContext context) {
        return context.wrap(expected.getInstance().toString());
    }

    @Override
    protected String actualDetail(CheckingContext checkingContext) {
        return (String) checkingContext.getActual().getInstance();
    }

    @Override
    protected String expectedDetail(CheckingContext checkingContext) {
        return (String) checkingContext.getExpected().getInstance();
    }

    public static class Equals extends CharSequenceChecker implements CheckerType.Equals {
        private static final Optional<Checker> INSTANCE = of(new CharSequenceChecker.Equals());

        @Override
        public Data transformActual(Data actual, Data expected, DALRuntimeContext context) {
            return context.wrap(actual.getInstance().toString());
        }
    }

    public static class Matches extends CharSequenceChecker implements CheckerType.Matches {
        private static final Optional<Checker> INSTANCE = of(new CharSequenceChecker.Matches());

        @Override
        public Data transformActual(Data actual, Data expected, DALRuntimeContext context) {
            return actual.convert(String.class);
        }
    }
}
