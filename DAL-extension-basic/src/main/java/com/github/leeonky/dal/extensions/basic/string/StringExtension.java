package com.github.leeonky.dal.extensions.basic.string;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.extensions.basic.string.util.CharSequenceChecker;
import com.github.leeonky.dal.runtime.Extension;

import static com.github.leeonky.dal.extensions.basic.binary.BinaryExtension.readAll;
import static com.github.leeonky.dal.extensions.basic.file.util.FileGroup.register;
import static com.github.leeonky.dal.extensions.basic.string.Methods.string;

@SuppressWarnings("unused")
public class StringExtension implements Extension {

    @Override
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder().registerStaticMethodExtension(Methods.class);
        dal.getRuntimeContextBuilder().checkerSetForMatching()
                .register(CharSequence.class, CharSequenceChecker::matches);
        dal.getRuntimeContextBuilder().checkerSetForEqualing()
                .register(CharSequence.class, CharSequence.class, CharSequenceChecker::equals);

        register("txt", inputStream -> string(readAll(inputStream)));
        register("TXT", inputStream -> string(readAll(inputStream)));
    }
}
