package org.testcharm.extensions.dal;

import org.testcharm.dal.DAL;
import org.testcharm.dal.runtime.*;
import org.testcharm.pf.By;
import org.testcharm.pf.Element;
import org.testcharm.pf.Region;
import org.testcharm.pf.WebElement;
import org.testcharm.util.BeanClass;
import org.testcharm.util.Sneaky;

import java.lang.reflect.Method;
import java.util.function.Function;

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
                .registerMetaProperty(Element.class, "watch", (RuntimeDataHandler<MetaData<Element>>)
                        elementMetaData -> watch(elementMetaData, dal, Element::screenshot))
                .registerMetaProperty(Region.class, "watch", (RuntimeDataHandler<MetaData<Region>>)
                        elementMetaData -> watch(elementMetaData, dal, r -> r.element().screenshot()))
        ;
    }

    private static <T> Data<T> watch(MetaData<T> metaData, DAL dal, Function<T, Object> mapper) {
        return Sneaky.get(() -> {
            Class<?> inspectorClass = Class.forName("org.testcharm.dal.extensions.inspector.Inspector");
            Method method = inspectorClass.getMethod("watch", DAL.class, String.class, Data.class);
            Data<T> data = metaData.data();
            method.invoke(null, dal, metaData.inputNode().inspect(), data.map(mapper));
            return data;
        });
    }
}
