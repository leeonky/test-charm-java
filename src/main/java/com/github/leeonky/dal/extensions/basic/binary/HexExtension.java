package com.github.leeonky.dal.extensions.basic.binary;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.extensions.basic.binary.util.Hex;
import com.github.leeonky.dal.extensions.basic.binary.util.HexDumper;
import com.github.leeonky.dal.extensions.basic.binary.util.HexFormatter;
import com.github.leeonky.dal.runtime.*;
import com.github.leeonky.dal.runtime.inspector.Dumper;
import com.github.leeonky.dal.util.TextUtil;
import com.github.leeonky.interpreter.StringWithPosition;

import java.io.ByteArrayOutputStream;

@SuppressWarnings("unused")
public class HexExtension implements Extension {
    private static final HexFormatter HEX_FORMATTER = new HexFormatter();
    private static final Dumper HEX_DUMPER = new HexDumper();

    @Override
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder()
                .registerTextFormatter("HEX", HEX_FORMATTER)
                .registerEqualsChecker(Hex.class, new HexEqualsChecker())
                .registerDumper(Hex.class, data -> HEX_DUMPER);
    }

    private static String inspect(Hex hex) {
        return hex.toString();
    }

    private static class HexEqualsChecker implements Checker {

        @Override
        public boolean verify(ExpectActual expectActual, int position) {
            Data actual = expectActual.getActual();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            actual.getValueList().forEach(b -> stream.write((byte) b));
            Hex actualHex = new Hex(stream.toByteArray());


            if (!expectActual.getExpectInstance().equals(actualHex)) {

                String expectedString = expectActual.getExpected().dumpDetail();
                String actualString = inspect(actualHex);
                int diffPosition = TextUtil.differentPosition(expectedString, actualString);
                String firstPart = new StringWithPosition(expectedString).position(diffPosition).result("Expected to be equal to: ");

                String message = new StringWithPosition(actualString).position(diffPosition).result(firstPart + "\nActual: ");

                throw new AssertionFailure(message, position);
            }
            return true;
        }
    }
}
