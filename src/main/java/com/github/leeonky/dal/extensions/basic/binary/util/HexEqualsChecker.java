package com.github.leeonky.dal.extensions.basic.binary.util;

import com.github.leeonky.dal.runtime.Checker;
import com.github.leeonky.dal.runtime.CheckingContext;
import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder;
import com.github.leeonky.dal.util.TextUtil;
import com.github.leeonky.interpreter.StringWithPosition;

import java.io.ByteArrayOutputStream;

public class HexEqualsChecker implements Checker {

    @Override
    public Data transformActual(Data actual, Data expected, RuntimeContextBuilder.DALRuntimeContext context) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        actual.getValueList().forEach(b -> stream.write((byte) b));
        return context.wrap(new Hex(stream.toByteArray()));
    }

    @Override
    public String message(CheckingContext checkingContext) {
        String expected = checkingContext.getExpected().dumpAll();
        String actual = checkingContext.getActual().dumpAll();
        int diffPosition = TextUtil.differentPosition(expected, actual);
        return new StringWithPosition(actual).position(diffPosition).result(new StringWithPosition(expected)
                .position(diffPosition).result("Expected to be equal to: ") + "\nActual: ");
    }
}
