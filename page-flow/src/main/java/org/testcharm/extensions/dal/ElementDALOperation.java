package org.testcharm.extensions.dal;

import org.testcharm.dal.DAL;
import org.testcharm.dal.ast.opt.DALOperator;
import org.testcharm.dal.runtime.*;
import org.testcharm.dal.runtime.RuntimeContextBuilder.DALRuntimeContext;
import org.testcharm.pf.*;
import org.testcharm.util.BeanClass;
import org.testcharm.util.Sneaky;

import java.lang.reflect.Method;
import java.util.function.Function;

public class ElementDALOperation implements Extension {

    @Override
    public void extend(DAL dal) {
        dal.getRuntimeContextBuilder()
                .registerPropertyAccessor(WebElement.class,
                        new JavaClassPropertyAccessor<WebElement<?, ?, ?>>() {
                            @Override
                            public Object getValue(WebElement<?, ?, ?> webElement, Object property) {
                                if (BeanClass.cast(property, String.class).map(s -> s.startsWith("@")).orElse(false))
                                    return webElement.attribute(((String) property).substring(1));
                                return super.getValue(webElement, property);
                            }
                        })
                .registerMetaProperty(Elements.class, "filter", metaData -> new ElementsFilterable(metaData.data().value()))
                .registerOperator(Operators.MATCH, new VerificationInFilter())
                .registerOperator(Operators.EQUAL, new VerificationInFilter())
                .registerDumper(By.class, byData -> (data, dumpingBuffer) -> dumpingBuffer.append(data.value().toString()))
                .registerMetaProperty(Element.class, "watch", (RuntimeDataHandler<MetaData<Element>>)
                        elementMetaData -> watch(elementMetaData, dal, Element::screenshot))
                .registerMetaProperty(Panel.class, "watch", (RuntimeDataHandler<MetaData<Panel>>)
                        elementMetaData -> watch(elementMetaData, dal, r -> r.element().screenshot()))
                .registerCustomSorter(Element.class, Element::text)
                .registerExclamation(Elements.class, (RuntimeDataHandler<RuntimeData<Elements>>)
                        elementsRuntimeData -> elementsRuntimeData.data().map(Elements::visible))

                .registerOperator(Operators.PLUS, new AbstractOperation<Elements, Elements>() {
                    @Override
                    public boolean match(Data<?> v1, DALOperator operator, Data<?> v2, DALRuntimeContext context) {
                        return v1.instanceOf(Elements.class) && v2.instanceOf(Elements.class);
                    }

                    @Override
                    public Object operateObject(Data<Elements> v1, DALOperator operator, Data<Elements> v2, DALRuntimeContext context) {
                        return Elements.concat(v1.value(), v2.value());
                    }
                })
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

    private class ElementsFilterable {
        private final Elements<?> elements;

        public ElementsFilterable(Elements<?> elements) {
            this.elements = elements;
        }

        public Elements<?> filter(DALOperator operator, Data<Object> v2, DALRuntimeContext context) {
            return filterList(operator, v2, context);
        }

        protected Elements<?> filterList(DALOperator operator, Data<?> v2, DALRuntimeContext context) {
            return elements.filter(element -> {
                try {
                    context.calculate(context.data(element), operator, v2);
                    return true;
                } catch (Throwable ig) {
                    return false;
                }
            });
        }
    }

    private static class VerificationInFilter extends AbstractOperation<ElementsFilterable, Object> {
        @Override
        public Object operateObject(Data<ElementDALOperation.ElementsFilterable> v1, DALOperator operator, Data<Object> v2, DALRuntimeContext context) {
            return v1.value().filter(operator, v2, context);
        }
    }
}
