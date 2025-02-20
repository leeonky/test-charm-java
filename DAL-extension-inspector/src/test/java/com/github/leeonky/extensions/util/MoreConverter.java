package com.github.leeonky.extensions.util;

import com.github.leeonky.dal.extensions.inspector.cucumber.page.SeleniumWebElement;
import com.github.leeonky.util.Converter;
import com.github.leeonky.util.ConverterExtension;

public class MoreConverter implements ConverterExtension {
    @Override
    public void extend(Converter converter) {
        converter.addTypeConverter(SeleniumWebElement.class, String.class, SeleniumWebElement::getText);
    }
}
