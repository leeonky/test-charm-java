package com.github.leeonky.dal.extensions.basic.number;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.Extension;

@SuppressWarnings("unused")
public class NumberExtension implements Extension {

    @Override
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder().registerStaticMethodExtension(Methods.class);
    }
}
