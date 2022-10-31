package com.github.leeonky.dal.extensions.basic.string;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.extensions.basic.Diff;
import com.github.leeonky.dal.runtime.ConditionalChecker;
import com.github.leeonky.dal.runtime.ExpectActual;
import com.github.leeonky.dal.runtime.Extension;

import static com.github.leeonky.dal.extensions.basic.binary.BinaryExtension.readAll;
import static com.github.leeonky.dal.extensions.basic.file.util.FileGroup.register;
import static com.github.leeonky.dal.extensions.basic.string.Methods.string;
import static com.github.leeonky.dal.runtime.ConditionalChecker.matchTypeChecker;

public class StringExtension implements Extension {

    @Override
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder()
                .registerStaticMethodExtension(Methods.class)
                .registerEqualsChecker(CharSequence.class, new CharSequenceChecker())
                .registerMatchesChecker(CharSequence.class, matchTypeChecker(Number.class, String.class)
                        .and(matchTypeChecker(Boolean.class, String.class))
                        .and(new CharSequenceMatcherChecker()))
        ;

        register("txt", inputStream -> string(readAll(inputStream)));
        register("TXT", inputStream -> string(readAll(inputStream)));
    }

    private static class CharSequenceChecker implements ConditionalChecker {

        @Override
        public boolean failed(ExpectActual expectActual) {
            return !convertToString(expectActual.getExpected().getInstance())
                    .equals(convertToString(expectActual.getActual().getInstance()));
        }

        @Override
        public String message(ExpectActual expectActual) {
            String message = expectActual.verificationMessage(getPrefix());
            if (expectActual.getActual().isNull())
                return message;
            String detail = new Diff(convertToString(expectActual.getExpected().getInstance()),
                    convertToString(expectActual.getActual().getInstance())).detail();
            return detail.isEmpty() ? message : message + "\n\n" + detail;
        }

        protected String getPrefix() {
            return "Expected to be equal to: ";
        }

        protected String convertToString(Object object) {
            return object == null ? null : ((CharSequence) object).toString();
        }
    }

    private static class CharSequenceMatcherChecker extends CharSequenceChecker {

        @Override
        public boolean failed(ExpectActual expectActual) {
            return !convertToString(expectActual.getExpected().getInstance())
                    .equals(convertToString(expectActual.getActual().convert(String.class).getInstance()));
        }

        @Override
        protected String getPrefix() {
            return "Expected to match: ";
        }

        @Override
        protected String convertToString(Object object) {
            return object == null ? null : object.toString();
        }
    }
}
