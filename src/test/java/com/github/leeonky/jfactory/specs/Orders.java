package com.github.leeonky.jfactory.specs;

import com.github.leeonky.jfactory.Spec;
import com.github.leeonky.jfactory.entity.Order;

public class Orders {
    public static class 订单 extends Spec<Order> {
    }

    public static class OrderFactory extends Spec<Order> {
        @Override
        protected String getName() {
            return "Order";
        }
    }
}
