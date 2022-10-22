package com.github.leeonky.dal.extensions.basic;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.runtime.Extension;

import java.net.MalformedURLException;
import java.net.URL;

public class UrlExtension implements Extension {

    @Override
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder().registerStaticMethodExtension(StaticMethods.class);
    }

    public static class StaticMethods {
        public static URL url(CharSequence str) throws MalformedURLException {
            return new URL(str.toString());
        }
    }
}
