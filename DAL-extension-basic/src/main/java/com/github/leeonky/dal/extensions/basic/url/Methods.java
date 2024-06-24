package com.github.leeonky.dal.extensions.basic.url;

import java.net.MalformedURLException;
import java.net.URL;

public class Methods {
    public static URL url(CharSequence str) throws MalformedURLException {
        return new URL(str.toString());
    }
}
