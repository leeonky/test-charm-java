package com.github.leeonky.map;

import com.github.leeonky.map.beans.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

class MapperTest {
    private Bean bean = new Bean().setKey("key").setValue("value");
    private Mapper mapper = new Mapper(getClass().getPackage().getName());
    private Order order = new Order();
    private ProductLine productLine = new ProductLine();
    private ServiceLine serviceLine = new ServiceLine();

    @BeforeEach
    void buildOrder() {
        productLine.id = "1";
        productLine.product = "p1";
        productLine.productDetail = "book";
        serviceLine.id = "2";
        serviceLine.service = "s2";
        serviceLine.serviceDetail = "subscribe";
        order.lines = asList(productLine, serviceLine);
        order.number = "100";
    }

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

    @Test
    void support_map_from_nested_property() {
        BeanWrapper beanWrapper = new BeanWrapper();
        beanWrapper.bean.setKey("hello");

        NestedSimpleBeanVO vo = mapper.map(beanWrapper, NestedSimpleBeanVO.class);
        assertThat(vo.beanKey).isEqualTo("hello");
    }

    @Test
    void support_map_from_nested_list_property() {
        OrderInfo orderInfo = mapper.map(order, OrderInfo.class);
        assertThat(orderInfo.lines).containsOnly("1", "2");
    }

    @Test
    void support_map_from_nested_list_property_to_map() {
        OrderInfoMap orderInfoMap = mapper.map(order, OrderInfoMap.class);

        assertThat(orderInfoMap.lines)
                .hasSize(2)
                .containsEntry("1", "1")
                .containsEntry("2", "2");
    }

    @MappingView(SubSimpleBeanVO.class)
    public static class SubSimpleBeanVO extends SimpleBeanVO {
    }

    public static class BeanWrapper {
        public Bean bean = new Bean();
    }

    @MappingFrom(BeanWrapper.class)
    public static class NestedSimpleBeanVO {

        @FromProperty("bean.key")
        public String beanKey;
    }

    @MappingFrom(Order.class)
    public static class OrderInfo {

        @FromProperty(value = "lines{id}", toElement = true)
        public List<String> lines;
    }

    @MappingFrom(Order.class)
    public static class OrderInfoMap {

        @FromProperty(value = "lines{id}", key = "lines{id}", toMapEntry = true)
        public Map<String, Object> lines;
    }
}