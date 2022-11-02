package com.github.leeonky.dal.extensions.basic.binary;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.extensions.basic.binary.util.Hex;
import com.github.leeonky.dal.extensions.basic.binary.util.HexDumper;
import com.github.leeonky.dal.extensions.basic.binary.util.HexFormatter;
import com.github.leeonky.dal.runtime.CheckingContext;
import com.github.leeonky.dal.runtime.ConditionalChecker;
import com.github.leeonky.dal.runtime.Data;
import com.github.leeonky.dal.runtime.Extension;
import com.github.leeonky.dal.runtime.inspector.Dumper;
import com.github.leeonky.dal.util.TextUtil;
import com.github.leeonky.interpreter.StringWithPosition;

import java.io.ByteArrayOutputStream;

import static java.util.Optional.of;

@SuppressWarnings("unused")
public class HexExtension implements Extension {
    private static final HexFormatter HEX_FORMATTER = new HexFormatter();
    private static final Dumper HEX_DUMPER = new HexDumper();

    //    TODO refactor
    @Override
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder()
                .registerTextFormatter("HEX", HEX_FORMATTER)
//                TODO actual type should be byte[] List<byte> inputStream
                .registerEqualsChecker(Hex.class, Object.class, (a1, a2) -> of(new HexEqualsChecker()))
                .registerDumper(Hex.class, data -> HEX_DUMPER);
    }

    private static String inspect(Hex hex) {
        return hex.toString();
    }

    private static class HexEqualsChecker implements ConditionalChecker {

        @Override
        public boolean failed(CheckingContext checkingContext) {
            Hex actualHex = getActual(checkingContext);
            return !checkingContext.getExpectInstance().equals(actualHex);
        }

        @Override
        public String message(CheckingContext checkingContext) {
            Hex actualHex = getActual(checkingContext);
            String expectedString = checkingContext.getExpected().dumpDetail();
            String actualString = inspect(actualHex);
            int diffPosition = TextUtil.differentPosition(expectedString, actualString);
            String firstPart = new StringWithPosition(expectedString).position(diffPosition).result("Expected to be equal to: ");
            return new StringWithPosition(actualString).position(diffPosition).result(firstPart + "\nActual: ");
        }

        private Hex getActual(CheckingContext checkingContext) {
            Data actual = checkingContext.getActual();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            actual.getValueList().forEach(b -> stream.write((byte) b));
            return new Hex(stream.toByteArray());
        }
    }
}
