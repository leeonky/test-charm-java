package com.github.leeonky.dal.extensions.basic.binary;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.extensions.basic.binary.util.Hex;
import com.github.leeonky.dal.extensions.basic.binary.util.HexDumper;
import com.github.leeonky.dal.extensions.basic.binary.util.HexEqualsChecker;
import com.github.leeonky.dal.extensions.basic.binary.util.HexFormatter;
import com.github.leeonky.dal.runtime.Extension;
import com.github.leeonky.dal.runtime.inspector.Dumper;

import java.util.Optional;

import static java.util.Optional.of;

@SuppressWarnings("unused")
public class HexExtension implements Extension {
    private static final HexFormatter HEX_FORMATTER = new HexFormatter();
    private static final Dumper HEX_DUMPER = new HexDumper();
    private static final HexEqualsChecker CHECKER_FOR_HEX_EQUALS = new HexEqualsChecker();

    @Override
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder()
                .registerTextFormatter("HEX", HEX_FORMATTER)
                .registerDumper(Hex.class, data -> HEX_DUMPER)
                .checkerSetForEqualing().register(Hex.class, (d1, actual) ->
                        actual.isList() ? of(CHECKER_FOR_HEX_EQUALS) : Optional.empty());
    }
}
