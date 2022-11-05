package com.github.leeonky.dal.extensions.basic;

import com.github.leeonky.dal.runtime.checker.Checker;
import com.github.leeonky.dal.runtime.checker.CheckingContext;

public abstract class CheckerWithDiff implements Checker, CheckerType {
    @Override
    public String message(CheckingContext checkingContext) {
        StringBuilder result = new StringBuilder(checkingContext.verificationMessage(getType(), ""));
        String detail = new Diff("Detail:\nExpect:", expectedDetail(checkingContext), actualDetail(checkingContext)).detail();
        if (!detail.isEmpty())
            result.append("\n\n").append(detail);
        return result.toString();
    }

    protected abstract String actualDetail(CheckingContext checkingContext);

    protected abstract String expectedDetail(CheckingContext checkingContext);
}
