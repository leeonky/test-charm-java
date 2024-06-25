package com.github.leeonky.jfactory.specs;

import com.github.leeonky.jfactory.Spec;
import com.github.leeonky.jfactory.Trait;
import com.github.leeonky.jfactory.entity.ProductStock;

public class ProductStocks {
    public static class 库存 extends Spec<ProductStock> {

        @Trait
        public void 无货() {
            property("count").value(0);
        }

        @Trait
        public void 满货() {
            property("count").value(100);
        }
    }

    public static class Inventory extends Spec<ProductStock> {
    }
}
