package com.github.leeonky.dal.runtime.checker;

import com.github.leeonky.dal.runtime.Calculator;
import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.util.TextUtil;
import com.github.leeonky.interpreter.StringWithPosition;

import static java.lang.String.format;

public class CheckingContext {
    private final Data originalExpected, originalActual, expected, actual;

    public Data getOriginalActual() {
        return originalActual;
    }

    public Data getOriginalExpected() {
        return originalExpected;
    }

    public CheckingContext(Data originalExpected, Data originalActual, Data expected, Data actual) {
        this.originalExpected = originalExpected;
        this.originalActual = originalActual;
        this.expected = expected;
        this.actual = actual;
    }

    public boolean objectNotEquals() {
        return !Calculator.equals(actual.resolved(), expected.resolved());
    }

    public String messageEqualTo() {
        return verificationMessage("Expected to be equal to: ", "");
    }

    public String messageMatch() {
        return verificationMessage("Expected to match: ", actual.instance() == originalActual.instance() ? ""
                : " converted from: " + originalActual.dump());
    }

    public String verificationMessage(String prefix, String actualPostfix) {
        String actual = this.actual.dump() + actualPostfix;
        String expected = this.expected.dump();
        int position = TextUtil.differentPosition(expected, actual);
        return new StringWithPosition(actual).position(position)
                .result(new StringWithPosition(expected).position(position).result(prefix) + "\nActual: ");
    }

    public String cannotCompare() {
        return format("Cannot compare between %s\nand %s", actual.dump(), expected.dump());
    }

    public Data getExpected() {
        return expected;
    }

    public Data getActual() {
        return actual;
    }
}
