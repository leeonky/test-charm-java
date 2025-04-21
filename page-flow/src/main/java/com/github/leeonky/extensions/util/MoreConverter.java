package com.github.leeonky.extensions.util;

import com.github.leeonky.pf.Element;
import com.github.leeonky.util.Converter;
import com.github.leeonky.util.ConverterExtension;

public class MoreConverter implements ConverterExtension {
    @Override
    public void extend(Converter converter) {
        converter.addTypeConverter(Element.class, String.class, Element::text);
    }
}
