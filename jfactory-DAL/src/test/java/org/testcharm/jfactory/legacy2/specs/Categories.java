package org.testcharm.jfactory.legacy2.specs;

import org.testcharm.jfactory.Spec;
import org.testcharm.jfactory.legacy2.entity.Category;

public class Categories {
    public static class 品类 extends Spec<Category> {
    }

    public static class 电器 extends Spec<Category> {
        @Override
        public void main() {
            property("name").value("电器");
        }
    }
}
