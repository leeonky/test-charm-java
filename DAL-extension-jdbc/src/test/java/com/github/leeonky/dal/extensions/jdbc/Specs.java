package com.github.leeonky.dal.extensions.jdbc;

import com.github.leeonky.jfactory.Global;
import com.github.leeonky.jfactory.Spec;

public class Specs {
    @Global
    public static class Order extends Spec<com.github.leeonky.dal.extensions.jdbc.entity.Order> {
    }

    @Global
    public static class Product extends Spec<com.github.leeonky.dal.extensions.jdbc.entity.Product> {
    }

    @Global
    public static class OrderLine extends Spec<com.github.leeonky.dal.extensions.jdbc.entity.OrderLine> {
        @Override
        public void main() {
            property("product").is(Product.class);
        }
    }

    @Global
    public static class Sku extends Spec<com.github.leeonky.dal.extensions.jdbc.entity.Sku> {
    }
}
