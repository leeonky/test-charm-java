package org.testcharm.extensions.dal;

import org.testcharm.dal.DAL;
import org.testcharm.dal.runtime.Extension;
import org.testcharm.dal.runtime.JavaClassPropertyAccessor;
import org.testcharm.pf.By;
import org.testcharm.pf.Element;
import org.testcharm.pf.Region;
import org.testcharm.pf.WebElement;
import org.testcharm.util.BeanClass;

public class ElementExtension implements Extension {

    @Override
    @SuppressWarnings("unchecked")
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder()
                .registerPropertyAccessor(WebElement.class,
                        new JavaClassPropertyAccessor<WebElement<?, ?>>((BeanClass) BeanClass.create(WebElement.class)) {
                            @Override
                            public Object getValue(WebElement<?, ?> webElement, Object property) {
                                if (property instanceof String && ((String) property).startsWith("@"))
                                    return webElement.attribute(((String) property).substring(1));
                                return super.getValue(webElement, property);
                            }
                        })
                .registerDumper(By.class, byData -> (data, dumpingBuffer) -> dumpingBuffer.append(data.value().toString()))
                .registerMetaProperty(Element.class, "watch", elementMetaData -> elementMetaData.data().value().screenshot())
                .registerMetaProperty(Region.class, "watch", elementMetaData -> elementMetaData.data().value().element().screenshot())
        ;
    }
}
