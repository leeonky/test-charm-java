package com.github.leeonky.dal.extensions.basic.string;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.extensions.basic.Diff;
import com.github.leeonky.dal.runtime.*;

import static com.github.leeonky.dal.extensions.basic.binary.BinaryExtension.readAll;
import static com.github.leeonky.dal.extensions.basic.file.util.FileGroup.register;
import static com.github.leeonky.dal.extensions.basic.string.Methods.string;
import static com.github.leeonky.dal.runtime.ConditionalChecker.matchTypeChecker;
import static java.util.Optional.of;

@SuppressWarnings("unused")
public class StringExtension implements Extension {

    @Override
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder()
                .registerStaticMethodExtension(Methods.class)
//                TODO refactor
                .registerEqualsChecker(CharSequence.class, CharSequence.class, (a1, a2) -> of(new CharSequenceChecker()))
                .registerMatchesChecker(CharSequence.class, matchTypeChecker(Number.class, String.class)
                        .and(matchTypeChecker(Boolean.class, String.class))
                        .and(new CharSequenceMatcherChecker()))
        ;

        register("txt", inputStream -> string(readAll(inputStream)));
        register("TXT", inputStream -> string(readAll(inputStream)));
    }

    private static class CharSequenceChecker implements ConditionalChecker {

        @Override
        public Data transformActual(Data actual, RuntimeContextBuilder.DALRuntimeContext context) {
            return context.wrap(actual.getInstance().toString());
        }

        @Override
        public Data transformExpected(Data expected, RuntimeContextBuilder.DALRuntimeContext context) {
            return context.wrap(expected.getInstance().toString());
        }

        @Override
        public String message(CheckingContext checkingContext) {
            String message = checkingContext.verificationMessage(getPrefix());
            if (checkingContext.getActual().isNull())
                return message;
            String detail = new Diff(convertToString(checkingContext.getExpected().getInstance()),
                    convertToString(checkingContext.getActual().getInstance())).detail();
            return detail.isEmpty() ? message : message + "\n\n" + detail;
        }

        protected String getPrefix() {
            return "Expected to be equal to: ";
        }

        protected String convertToString(Object object) {
            return ((CharSequence) object).toString();
        }
    }

    private static class CharSequenceMatcherChecker implements ConditionalChecker {

        @Override
        public boolean failed(CheckingContext checkingContext) {
            return !convertToString(checkingContext.getExpected().getInstance())
                    .equals(convertToString(checkingContext.getActual().convert(String.class).getInstance()));
        }

        protected String getPrefix() {
            return "Expected to match: ";
        }

        protected String convertToString(Object object) {
            return object == null ? null : object.toString();
        }

        @Override
        public String message(CheckingContext checkingContext) {
            String message = checkingContext.verificationMessage(getPrefix());
            if (checkingContext.getActual().isNull())
                return message;
            String detail = new Diff(convertToString(checkingContext.getExpected().getInstance()),
                    convertToString(checkingContext.getActual().getInstance())).detail();
            return detail.isEmpty() ? message : message + "\n\n" + detail;
        }
    }
}
