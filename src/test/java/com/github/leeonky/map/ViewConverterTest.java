package com.github.leeonky.map;

import com.github.leeonky.map.beans.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

class ViewConverterTest {
    private Mapper mapper = new Mapper(getClass().getPackage().getName());
    private Order order = new Order();
    private LineMap lineMap = new LineMap();

    @BeforeEach
    void buildOrder() {
        ProductLine productLine = new ProductLine();
        productLine.id = "1";
        ServiceLine serviceLine = new ServiceLine();
        serviceLine.id = "2";
        order.lines = asList(productLine, serviceLine);
        lineMap.lineMap = new HashMap<>();
        lineMap.lineMap.put("product", productLine);
        lineMap.lineMap.put("service", serviceLine);
    }

    @Test
    void support_auto_map_to_set_class() {
        LinkedSetLineOrderVO vo = mapper.map(order, LinkedSetLineOrderVO.class);

        assertThat(vo.lines).isInstanceOf(LinkedHashSet.class);
        assertThat(vo.lines.size()).isEqualTo(2);

        assertThat(vo.lines.stream().filter(ProductLineVO.class::isInstance).findFirst().get())
                .hasFieldOrPropertyWithValue("id", "1");
        assertThat(vo.lines.stream().filter(ServiceLineVO.class::isInstance).findFirst().get())
                .hasFieldOrPropertyWithValue("id", "2");
    }

    @Test
    void support_auto_map_array() {
        ArrayLineOrderVO vo = mapper.map(order, ArrayLineOrderVO.class);
        assertThat(vo.lines).isInstanceOf(LineVO[].class);
        assertThat(vo.lines.length).isEqualTo(2);
        assertThat(vo.lines[0])
                .hasFieldOrPropertyWithValue("id", "1");
        assertThat(vo.lines[1])
                .hasFieldOrPropertyWithValue("id", "2");
    }

    @Test
    void support_auto_map_map() {
        LineMapVO vo = mapper.map(lineMap, LineMapVO.class);
        assertThat(vo.lineMap).isInstanceOf(Map.class);
        assertThat(vo.lineMap.size()).isEqualTo(2);
        assertThat(vo.lineMap.get("product"))
                .hasFieldOrPropertyWithValue("id", "1");
        assertThat(vo.lineMap.get("service"))
                .hasFieldOrPropertyWithValue("id", "2");
    }

    @Test
    void support_auto_map_map_class() {
        LineLinkedMapVO vo = mapper.map(lineMap, LineLinkedMapVO.class);
        assertThat(vo.lineMap).isInstanceOf(LinkedHashMap.class);
        assertThat(vo.lineMap.size()).isEqualTo(2);
        assertThat(vo.lineMap.get("product"))
                .hasFieldOrPropertyWithValue("id", "1");
        assertThat(vo.lineMap.get("service"))
                .hasFieldOrPropertyWithValue("id", "2");
    }

    @MappingFrom(Order.class)
    public static class ListLineOrderVO {

        @MappingView(Simple.class)
        public List<LineVO> lines;
    }

    @MappingFrom(Order.class)
    public static class LinkedListLineOrderVO {

        @MappingView(Simple.class)
        public LinkedList<LineVO> lines;
    }

    @MappingFrom(Order.class)
    public static class SetLineOrderVO {

        @MappingView(Simple.class)
        public Set<LineVO> lines;
    }

    @MappingFrom(Order.class)
    public static class LinkedSetLineOrderVO {

        @MappingView(Simple.class)
        public LinkedHashSet<LineVO> lines;
    }

    @MappingFrom(Order.class)
    public static class ArrayLineOrderVO {

        @MappingView(Simple.class)
        public LineVO[] lines;
    }

    public static class LineMap {
        public Map<String, Line> lineMap;
    }

    @MappingFrom(LineMap.class)
    public static class LineMapVO {

        @MappingView(Simple.class)
        public Map<String, LineVO> lineMap;
    }

    @MappingFrom(LineMap.class)
    public static class LineLinkedMapVO {

        @MappingView(Simple.class)
        public LinkedHashMap<String, LineVO> lineMap;
    }
}