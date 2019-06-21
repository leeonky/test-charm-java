package com.github.leeonky.map;

import com.github.leeonky.map.beans.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

class MapperTest {
    private Bean bean = new Bean().setKey("key").setValue("value");
    private Mapper mapper = new Mapper(getClass().getPackage().getName());
    private Order order = new Order();
    private OrderWrapper orderWrapper = new OrderWrapper();
    private ProductLine productLine = new ProductLine();
    private ServiceLine serviceLine = new ServiceLine();
    private OrderWrapperMap orderWrapperMap = new OrderWrapperMap();

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

        orderWrapper.lines = asList(productLine, serviceLine).stream()
                .map(l -> {
                    LineWrapper lineWrapper = new LineWrapper();
                    lineWrapper.line = l;
                    return lineWrapper;
                }).collect(Collectors.toList());

        orderWrapperMap.lines = new HashMap<>();
        LineWrapper lineWrapper1 = new LineWrapper();
        lineWrapper1.line = productLine;
        orderWrapperMap.lines.put(lineWrapper1.line.id, lineWrapper1);
        LineWrapper lineWrapper2 = new LineWrapper();
        lineWrapper2.line = serviceLine;
        orderWrapperMap.lines.put(lineWrapper2.line.id, lineWrapper2);
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
        OrderInfoList vo = mapper.map(order, OrderInfoList.class);
        assertThat(vo.lines).containsOnly("1", "2");
    }

    @Test
    void support_map_from_nested_list_property_to_map() {
        OrderInfoMap vo = mapper.map(order, OrderInfoMap.class);

        assertThat(vo.lines).hasSize(2)
                .containsEntry("1", "1")
                .containsEntry("2", "2");
    }

    @Test
    void support_both_specify_from_property_and_map_view_list_to_list() {
        OrderWrapperVO vo = mapper.map(orderWrapper, OrderWrapperVO.class);

        assertThat(vo.lines).hasSize(2);
        assertThat(vo.lines.get(0))
                .isInstanceOf(ProductLineVO.class)
                .hasFieldOrPropertyWithValue("product", "p1");
        assertThat(vo.lines.get(1))
                .isInstanceOf(ServiceLineVO.class)
                .hasFieldOrPropertyWithValue("service", "s2");
    }

    @Test
    void support_both_specify_from_property_and_map_view_list_to_array() {
        OrderWrapperArrayVO vo = mapper.map(orderWrapper, OrderWrapperArrayVO.class);

        assertThat(vo.lines).hasSize(2);
        assertThat(vo.lines[0])
                .isInstanceOf(ProductLineVO.class)
                .hasFieldOrPropertyWithValue("product", "p1");
        assertThat(vo.lines[1])
                .isInstanceOf(ServiceLineVO.class)
                .hasFieldOrPropertyWithValue("service", "s2");
    }

    @Test
    void support_both_specify_from_property_and_map_view_map_to_list() {
        OrderWrapperMapVO vo = mapper.map(orderWrapperMap, OrderWrapperMapVO.class);

        assertThat(vo.lines).hasSize(2);
        assertThat(vo.lines.get(0))
                .isInstanceOf(ProductLineVO.class)
                .hasFieldOrPropertyWithValue("product", "p1");
        assertThat(vo.lines.get(1))
                .isInstanceOf(ServiceLineVO.class)
                .hasFieldOrPropertyWithValue("service", "s2");
    }

    @Test
    void support_both_specify_from_property_and_map_view_map_to_map() {
        OrderWrapperMapToMapVO vo = mapper.map(orderWrapperMap, OrderWrapperMapToMapVO.class);

        assertThat(vo.lines).hasSize(2);
        assertThat(vo.lines.get("1"))
                .isInstanceOf(ProductLineVO.class)
                .hasFieldOrPropertyWithValue("product", "p1");
        assertThat(vo.lines.get("2"))
                .isInstanceOf(ServiceLineVO.class)
                .hasFieldOrPropertyWithValue("service", "s2");
    }

    @Test
    void support_both_specify_from_property_and_map_view_list_to_map() {
        OrderWrapperListToMapVO vo = mapper.map(orderWrapper, OrderWrapperListToMapVO.class);

        assertThat(vo.lines).hasSize(2);
        assertThat(vo.lines.get("1"))
                .isInstanceOf(ProductLineVO.class)
                .hasFieldOrPropertyWithValue("product", "p1");
        assertThat(vo.lines.get("2"))
                .isInstanceOf(ServiceLineVO.class)
                .hasFieldOrPropertyWithValue("service", "s2");
    }

    @Test
    void should_return_null_when_source_class_not_register() {
        assertThat((Object) mapper.map("", Simple.class)).isNull();
    }

    @Test
    void should_return_null_when_view_not_register() {
        assertThat((Object) mapper.map(bean, String.class)).isNull();
    }

    @Test
    void should_return_all_candidate_class_for_super_class_and_view() {
        assertThat(mapper.findSubDestClasses(ProductLineVO.class, Simple.class))
                .containsOnly(ProductLineVO.class);

        assertThat(mapper.findSubDestClasses(DetailProductLineVO.class, Detail.class))
                .containsOnly(DetailProductLineVO.class, DetailOnlineProductLineVO.class);

        mapper.setScope(Customer.class);

        assertThat(mapper.findSubDestClasses(DetailProductLineVO.class, Detail.class))
                .containsOnly(DetailProductLineVO.class, DetailOnlineProductLineVO.class, ScopedDetailOnlineProductLineVO.class);
    }

    @MappingFrom(OnlineProductLine.class)
    @MappingScope(Customer.class)
    public static class ScopedDetailOnlineProductLineVO extends DetailProductLineVO {
    }

    @MappingFrom(OnlineProductLine.class)
    public static class DetailOnlineProductLineVO extends DetailProductLineVO {
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
    public static class OrderInfoList {

        @FromProperty(value = "lines{id}", toElement = true)
        public List<String> lines;
    }

    @MappingFrom(Order.class)
    public static class OrderInfoMap {

        @FromProperty(value = "lines{id}", key = "lines{id}", toMapEntry = true)
        public Map<String, String> lines;
    }

    public static class OrderWrapper {
        public List<LineWrapper> lines;
    }

    public static class LineWrapper {
        public Line line;
    }

    @MappingFrom(OrderWrapper.class)
    public static class OrderWrapperVO {

        @FromProperty(value = "lines{line}", toElement = true)
        @MappingView(Simple.class)
        public List<LineVO> lines;
    }

    @MappingFrom(OrderWrapper.class)
    public static class OrderWrapperArrayVO {

        @FromProperty(value = "lines{line}", toElement = true)
        @MappingView(Simple.class)
        public LineVO[] lines;
    }

    public static class OrderWrapperMap {
        public Map<String, LineWrapper> lines;
    }

    @MappingFrom(OrderWrapperMap.class)
    public static class OrderWrapperMapVO {

        @FromProperty(value = "lines{value.line}", toElement = true)
        @MappingView(Simple.class)
        public List<LineVO> lines;
    }

    @MappingFrom(OrderWrapperMap.class)
    public static class OrderWrapperMapToMapVO {

        @FromProperty(value = "lines{value.line}", key = "lines{key}", toMapEntry = true)
        @MappingView(Simple.class)
        public Map<String, LineVO> lines;
    }

    @MappingFrom(OrderWrapper.class)
    public static class OrderWrapperListToMapVO {

        @FromProperty(value = "lines{line}", key = "lines{line.id}", toMapEntry = true)
        @MappingView(Simple.class)
        public Map<String, LineVO> lines;
    }

    public class OnlineProductLine extends ProductLine {

    }
}