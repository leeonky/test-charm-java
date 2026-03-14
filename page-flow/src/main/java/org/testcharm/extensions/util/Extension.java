package org.testcharm.extensions.util;

import org.testcharm.pf.Element;
import org.testcharm.pf.Region;
import org.testcharm.util.Converter;
import org.testcharm.util.ConverterExtension;

public class Extension implements ConverterExtension {
    @Override
    public void extend(Converter converter) {
        converter.addTypeConverter(Element.class, String.class, Element::text)
                .addTypeConverter(Region.class, String.class, r -> r.element().text());
    }
}
