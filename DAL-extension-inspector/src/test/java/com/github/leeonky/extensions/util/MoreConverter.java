package com.github.leeonky.extensions.util;

import com.github.leeonky.dal.extensions.inspector.cucumber.pagebk.Panel;
import com.github.leeonky.util.Converter;
import com.github.leeonky.util.ConverterExtension;

public class MoreConverter implements ConverterExtension {
    @Override
    public void extend(Converter converter) {
        converter.addTypeConverter(Panel.class, String.class, Panel::text);
    }
}
