package com.github.leeonky.map;

import com.github.leeonky.map.beans.*;
import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

class MapperTest {
    private Bean bean = new Bean().setKey("key").setValue("value");
    private Mapper mapper = new Mapper(getClass().getPackage().getName());

    @Test
    void map_object_via_view() {
        assertThat((Object) mapper.map(bean, Simple.class))
                .isInstanceOf(SimpleBeanVO.class)
                .hasFieldOrPropertyWithValue("key", "key");

        assertThat((Object) mapper.map(bean, Detail.class))
                .isInstanceOf(DetailBeanVO.class)
                .hasFieldOrPropertyWithValue("key", "key")
                .hasFieldOrPropertyWithValue("value", "value");
    }

    @Test
    void map_object_from_sub_class() {
        assertThat((Object) mapper.map(bean, SubSimpleBeanVO.class))
                .isInstanceOf(SubSimpleBeanVO.class)
                .hasFieldOrPropertyWithValue("key", "key");
    }

    @Test
    void map_object_via_view_and_scope() {
        mapper.setScope(Customer.class);

        assertThat((Object) mapper.map(bean, Simple.class))
                .isInstanceOf(CustomerSimpleBeanVO.class)
                .hasFieldOrPropertyWithValue("key", "key");

        assertThat((Object) mapper.map(bean, Detail.class))
                .isInstanceOf(DetailBeanVO.class)
                .hasFieldOrPropertyWithValue("key", "key")
                .hasFieldOrPropertyWithValue("value", "value");
    }

    @Test
    void support_nested_polymorphism_mapping() {
        Order order = new Order();
        ProductLine productLine = new ProductLine();
        productLine.id = "1";
        productLine.product = "p1";
        productLine.productDetail = "book";
        ServiceLine serviceLine = new ServiceLine();
        serviceLine.id = "2";
        serviceLine.service = "s2";
        serviceLine.serviceDetail = "subscribe";
        order.lines = asList(productLine, serviceLine);
        order.number = "100";

        OrderVO orderVO = mapper.map(order, Simple.class);
        assertThat(orderVO).hasFieldOrPropertyWithValue("number", "100");
        assertThat(orderVO.lines.get(0))
                .isInstanceOf(ProductLineVO.class)
                .hasFieldOrPropertyWithValue("id", "1")
                .hasFieldOrPropertyWithValue("product", "p1");
        assertThat(orderVO.lines.get(1))
                .isInstanceOf(ServiceLineVO.class)
                .hasFieldOrPropertyWithValue("id", "2")
                .hasFieldOrPropertyWithValue("service", "s2");

        DetailOrderVO detailOrderVO = mapper.map(order, Detail.class);
        assertThat(detailOrderVO).hasFieldOrPropertyWithValue("number", "100");
        assertThat(detailOrderVO.lines.get(0))
                .isInstanceOf(DetailProductLineVO.class)
                .hasFieldOrPropertyWithValue("id", "1")
                .hasFieldOrPropertyWithValue("productDetail", "book")
                .hasFieldOrPropertyWithValue("product", "p1");
        assertThat(detailOrderVO.lines.get(1))
                .isInstanceOf(DetailServiceLineVO.class)
                .hasFieldOrPropertyWithValue("id", "2")
                .hasFieldOrPropertyWithValue("serviceDetail", "subscribe")
                .hasFieldOrPropertyWithValue("service", "s2");
    }

    @MappingView(SubSimpleBeanVO.class)
    public static class SubSimpleBeanVO extends SimpleBeanVO {
    }
}