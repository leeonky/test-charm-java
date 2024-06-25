package com.github.leeonky.jfactory.helper;

import com.github.leeonky.dal.runtime.checker.Checker;
import com.github.leeonky.dal.runtime.checker.CheckingContext;

import java.util.function.BiConsumer;

class OverrideVerificationOptChecker<A, E> implements Checker {
    private final BiConsumer<A, E> runnable;

    public OverrideVerificationOptChecker(BiConsumer<A, E> runnable) {
        this.runnable = runnable;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean failed(CheckingContext checkingContext) {
        runnable.accept((A) checkingContext.getOriginalActual().instance(),
                (E) checkingContext.getOriginalExpected().instance());
        return false;
    }

    @Override
    public String message(CheckingContext checkingContext) {
        return null;
    }
}
