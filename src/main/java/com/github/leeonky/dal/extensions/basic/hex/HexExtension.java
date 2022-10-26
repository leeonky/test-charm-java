package com.github.leeonky.dal.extensions.basic.hex;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.extensions.basic.hex.util.Hex;
import com.github.leeonky.dal.extensions.basic.hex.util.HexFormatter;
import com.github.leeonky.dal.extensions.basic.hex.util.HexInspector;
import com.github.leeonky.dal.runtime.*;
import com.github.leeonky.dal.util.TextUtil;
import com.github.leeonky.interpreter.StringWithPosition;

import java.io.ByteArrayOutputStream;

public class HexExtension implements Extension {
    private static final HexInspector HEX_INSPECTOR = new HexInspector();
    private static final HexFormatter HEX_FORMATTER = new HexFormatter();

    @Override
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder()
                .registerTextFormatter("HEX", HEX_FORMATTER)
                .registerEqualsChecker(Hex.class, new HexEqualsChecker())
                .registerInspector(Hex.class, data -> HEX_INSPECTOR);
    }

    private static String inspect(Hex hex) {
        return "Binary " + hex.toString();
    }

    private static class HexEqualsChecker implements Checker {

        @Override
        public boolean verify(ExpectActual expectActual, int position) {
            Data actual = expectActual.getActual();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            actual.getValueList().forEach(b -> stream.write((byte) b));
            Hex actualHex = new Hex(stream.toByteArray());


            if (!expectActual.getExpectInstance().equals(actualHex)) {

                String expectedString = expectActual.getExpected().inspect();
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
