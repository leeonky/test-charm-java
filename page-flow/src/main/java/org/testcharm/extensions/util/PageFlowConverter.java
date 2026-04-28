package org.testcharm.extensions.util;

import org.testcharm.pf.Element;
import org.testcharm.pf.Panel;
import org.testcharm.util.Converter;
import org.testcharm.util.ConverterExtension;

public class PageFlowConverter implements ConverterExtension {
    @Override
    public void extend(Converter converter) {
        converter.addTypeConverter(Element.class, String.class, Element::text);
        converter.addTypeConverter(Element.class, Integer.class, element -> Integer.valueOf(element.text()));
        converter.addTypeConverter(Element.class, Double.class, element -> Double.valueOf(element.text()));
        converter.addTypeConverter(Element.class, Long.class, element -> Long.valueOf(element.text()));
        converter.addTypeConverter(Panel.class, String.class, Panel::text);
    }
}
