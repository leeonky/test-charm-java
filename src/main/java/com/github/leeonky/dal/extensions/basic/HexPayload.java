package com.github.leeonky.dal.extensions.basic;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.extensions.basic.formatters.Hex;
import com.github.leeonky.dal.runtime.*;
import com.github.leeonky.dal.util.TextUtil;
import com.github.leeonky.interpreter.StringWithPosition;

import java.io.ByteArrayOutputStream;

import static com.github.leeonky.dal.extensions.basic.formatters.Hex.hex;

public class HexPayload implements Extension {

    @Override
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder()
                .registerTextFormatter("HEX", new TextFormatter() {
                    @Override
                    public String description() {
                        return "use hex numbers as binary data, like 'FF EF 08...'";
                    }

                    @Override
                    public Object format(Object content, TextAttribute attribute) {
                        return hex((String) content);
                    }
                })
                .registerEqualsChecker(Hex.class, new HexEqualsChecker())
                .registerInspectorBk(Hex.class, data -> (path, inspectorCache) -> inspect((Hex) data.getInstance()));
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
