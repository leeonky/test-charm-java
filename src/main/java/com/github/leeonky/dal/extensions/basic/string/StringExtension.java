package com.github.leeonky.dal.extensions.basic.string;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.extensions.basic.string.util.CharSequenceEqualsChecker;
import com.github.leeonky.dal.extensions.basic.string.util.CharSequenceMatcherChecker;
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
                .register(CharSequence.class, CharSequenceMatcherChecker::factory);
        dal.getRuntimeContextBuilder().checkerSetForEqualing()
                .register(CharSequence.class, CharSequence.class, CharSequenceEqualsChecker::factory);

        register("txt", inputStream -> string(readAll(inputStream)));
        register("TXT", inputStream -> string(readAll(inputStream)));
    }
}
