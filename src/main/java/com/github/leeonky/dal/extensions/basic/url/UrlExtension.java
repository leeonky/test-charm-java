package com.github.leeonky.dal.extensions.basic.url;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.Extension;

@SuppressWarnings("unused")
public class UrlExtension implements Extension {

    @Override
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder().registerStaticMethodExtension(Methods.class);
    }
}
