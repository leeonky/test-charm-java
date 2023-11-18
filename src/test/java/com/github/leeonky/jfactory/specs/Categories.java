package com.github.leeonky.jfactory.specs;

import com.github.leeonky.jfactory.Spec;
import com.github.leeonky.jfactory.entity.Category;

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
