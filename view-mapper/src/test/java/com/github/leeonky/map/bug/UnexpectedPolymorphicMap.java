package com.github.leeonky.map.bug;

import com.github.leeonky.map.*;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class UnexpectedPolymorphicMap {

    @Test
    void root_cause() {
        MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();

        assertThat(mapperFactory.getMapperFacade().map(new OrderLine(), SimpleOrderLine.class).getClass()).isEqualTo(SimpleOrderLine.class);

        mapperFactory.classMap(OrderLine.class, SimpleOrderLine.class).byDefault().register(); //should also register or supper class
        mapperFactory.classMap(OrderLine.class, DetailOrderLine.class).byDefault().register();

        assertThat(mapperFactory.getMapperFacade().map(new OrderLine(), SimpleOrderLine.class).getClass()).isEqualTo(SimpleOrderLine.class);
    }

    @Test
    void fix() {
        Mapper mapper = new Mapper("com.github.leeonky.map.bug");
        Action action = new Action();
        OrderLine orderLine = new OrderLine();
        action.action = "a";
        action.orderLine = orderLine;
        orderLine.id = 1;
        orderLine.actions = asList(action);
        orderLine.action = action;

        DetailAction detailAction = mapper.mapTo(action, DetailAction.class);
        assertThat(detailAction.orderLine.action.getClass()).isEqualTo(SimpleAction.class);
        assertThat(detailAction.orderLine.actions.get(0).getClass()).isEqualTo(SimpleAction.class);
    }

    public static class Action {
        public String action;
        public OrderLine orderLine;
    }

    public static class OrderLine {
        public long id;
        public List<Action> actions;
        public Action action;
    }

    @Mapping(from = Action.class, view = View.Summary.class)
    public static class SimpleAction {
        public String action;
    }

    @MappingView(View.Detail.class)
    public static class DetailAction extends SimpleAction {
        @FromProperty("orderLine")
        public DetailOrderLine orderLine;
    }

    @Mapping(from = OrderLine.class, view = View.Summary.class)
    public static class SimpleOrderLine {
        public long id;
    }

    @MappingView(View.Detail.class)
    public static class DetailOrderLine extends SimpleOrderLine {
        public List<SimpleAction> actions;
        public SimpleAction action;
    }
}
