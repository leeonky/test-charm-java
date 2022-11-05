package com.github.leeonky.dal.extensions.basic.binary.util;

import com.github.leeonky.dal.extensions.basic.CheckerType;
import com.github.leeonky.dal.extensions.basic.Diff;
import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder;
import com.github.leeonky.dal.runtime.checker.Checker;
import com.github.leeonky.dal.runtime.checker.CheckingContext;

import java.io.ByteArrayOutputStream;

public class HexEqualsChecker implements Checker, CheckerType, CheckerType.Equals {

    @Override
    public Data transformActual(Data actual, Data expected, RuntimeContextBuilder.DALRuntimeContext context) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        actual.getValueList().forEach(b -> stream.write((byte) b));
        return context.wrap(new Hex(stream.toByteArray()));
    }

    @Override
    public String message(CheckingContext context) {
        return new Diff(getType(), context.getExpected().dumpAll(), context.getActual().dumpAll()).detail();
    }
}
