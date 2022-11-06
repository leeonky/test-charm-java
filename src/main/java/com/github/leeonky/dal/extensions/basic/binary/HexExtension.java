package com.github.leeonky.dal.extensions.basic.binary;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.extensions.basic.binary.util.Hex;
import com.github.leeonky.dal.extensions.basic.binary.util.HexChecker;
import com.github.leeonky.dal.extensions.basic.binary.util.HexDumper;
import com.github.leeonky.dal.extensions.basic.binary.util.HexFormatter;
import com.github.leeonky.dal.runtime.Extension;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder;
import com.github.leeonky.dal.runtime.inspector.Dumper;

import java.io.InputStream;

@SuppressWarnings("unused")
public class HexExtension implements Extension {
    private static final HexFormatter HEX_FORMATTER = new HexFormatter();
    private static final Dumper HEX_DUMPER = new HexDumper();

    @Override
    public void extend(DAL dal) {
        RuntimeContextBuilder contextBuilder = dal.getRuntimeContextBuilder();
        contextBuilder
                .registerTextFormatter("HEX", HEX_FORMATTER)
                .registerDumper(Hex.class, data -> HEX_DUMPER)
                .registerTextFormatter("HEX", HEX_FORMATTER);

        contextBuilder.checkerSetForEqualing()
                .register(Hex.class, byte[].class, HexChecker::equals)
                .register(Hex.class, Byte[].class, HexChecker::equals)
                .register(Hex.class, InputStream.class, HexChecker::equals);

        contextBuilder.checkerSetForMatching()
                .register(Hex.class, HexChecker::matches);
    }
}
