package org.testcharm.jfactory.legacy2.specs;

import org.testcharm.jfactory.Spec;
import org.testcharm.jfactory.Trait;
import org.testcharm.jfactory.legacy2.entity.ProductStock;

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
