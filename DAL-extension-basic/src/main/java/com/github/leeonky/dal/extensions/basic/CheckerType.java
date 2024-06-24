package com.github.leeonky.dal.extensions.basic;

public interface CheckerType {
    String getType();

    interface Equals extends CheckerType {

        @Override
        default String getType() {
            return "Expected to be equal to: ";
        }
    }

    interface Matches extends CheckerType {

        @Override
        default String getType() {
            return "Expected to match: ";
        }
    }
}
