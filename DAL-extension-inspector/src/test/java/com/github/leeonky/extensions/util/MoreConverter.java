package com.github.leeonky.extensions.util;

import com.github.leeonky.util.Converter;
import com.github.leeonky.util.ConverterExtension;
import org.openqa.selenium.WebElement;

public class MoreConverter implements ConverterExtension {
    @Override
    public void extend(Converter converter) {
        converter.addTypeConverter(WebElement.class, String.class, WebElement::getText);
    }
}
