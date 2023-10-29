package com.github.leeonky.jfactory.specs;

import com.github.leeonky.jfactory.Spec;
import com.github.leeonky.jfactory.entity.Cart;

public class Carts {
    public static class 购物车 extends Spec<Cart> {
    }

    public static class CartProduct extends Spec<Cart> {
        @Override
        protected String getName() {
            return "Cart";
        }
    }
}
