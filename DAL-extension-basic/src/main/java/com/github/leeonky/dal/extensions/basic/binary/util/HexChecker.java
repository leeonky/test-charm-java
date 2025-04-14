package com.github.leeonky.dal.extensions.basic.binary.util;

import com.github.leeonky.dal.extensions.basic.CheckerType;
import com.github.leeonky.dal.extensions.basic.Diff;
import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;
import com.github.leeonky.dal.runtime.checker.Checker;
import com.github.leeonky.dal.runtime.checker.CheckingContext;

import java.io.InputStream;
import java.util.Optional;

import static java.util.Optional.of;

public abstract class HexChecker implements Checker, CheckerType {

    public static Optional<Checker> equals(Data<?> d1, Data<?> d2) {
        return Equals.INSTANCE;
    }

    public static Optional<Checker> matches(Data<?> d1, Data<?> d2) {
        return Matches.INSTANCE;
    }

    @Override
    public String message(CheckingContext context) {
        return new Diff(getType(), context.getExpected().dump(), context.getActual().dump()).detail();
    }

    @Override
    public Data<?> transformExpected(Data<?> expected, DALRuntimeContext context) {
        return expected.map(HexDumper::extractBytes);
    }

    public static class Equals extends HexChecker implements CheckerType.Equals {
        private static final Optional<Checker> INSTANCE = of(new HexChecker.Equals());

        @Override
        public Data<?> transformActual(Data<?> actual, Data<?> expected, DALRuntimeContext context) {
            return actual.map(HexDumper::extractBytes);
        }
    }

    public static class Matches extends HexChecker implements CheckerType.Matches {
        private static final Optional<Checker> INSTANCE = of(new HexChecker.Matches());

        @Override
        public Data<?> transformActual(Data<?> actual, Data<?> expected, DALRuntimeContext context) {
            return actual.tryConvert(byte[].class, InputStream.class, Byte[].class).map(HexDumper::extractBytes);
        }
    }
}
