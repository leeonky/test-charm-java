package com.github.leeonky.dal.extensions.basic.binary.util;

import com.github.leeonky.dal.extensions.basic.CheckerType;
import com.github.leeonky.dal.extensions.basic.Diff;
import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;
import com.github.leeonky.dal.runtime.checker.Checker;
import com.github.leeonky.dal.runtime.checker.CheckingContext;
import com.github.leeonky.util.CannotToStreamException;
import com.github.leeonky.util.ConvertException;

import java.io.InputStream;
import java.util.Optional;

import static java.util.Optional.of;

public abstract class HexChecker implements Checker, CheckerType {

    public static Optional<Checker> equals(Data d1, Data d2) {
        return Equals.INSTANCE;
    }

    public static Optional<Checker> matches(Data d1, Data d2) {
        return Matches.INSTANCE;
    }

    @Override
    public String message(CheckingContext context) {
        return new Diff(getType(), context.getExpected().dumpAll(), context.getActual().dumpAll()).detail();
    }

    @Override
    public Data transformExpected(Data expected, DALRuntimeContext context) {
        return context.wrap(HexDumper.getBytes(expected));
    }

    public static class Equals extends HexChecker implements CheckerType.Equals {
        private static final Optional<Checker> INSTANCE = of(new HexChecker.Equals());

        @Override
        public Data transformActual(Data actual, Data expected, DALRuntimeContext context) {
            return context.wrap(HexDumper.getBytes(actual));
        }
    }

    public static class Matches extends HexChecker implements CheckerType.Matches {
        private static final Optional<Checker> INSTANCE = of(new HexChecker.Matches());

        @Override
        public Data transformActual(Data actual, Data expected, DALRuntimeContext context) {
            return context.wrap(HexDumper.getBytes(convert(actual)));
        }

        private Data convert(Data actual) {
            try {
                return actual.convert(byte[].class);
            } catch (ConvertException | CannotToStreamException _ignore) {
                try {
                    return actual.convert(Byte[].class);
                } catch (ConvertException | CannotToStreamException _ignore2) {
                    return actual.convert(InputStream.class);
                }
            }
        }
    }
}
