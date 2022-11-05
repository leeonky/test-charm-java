package com.github.leeonky.dal.extensions.basic;

public interface CheckerType {
    String getPrefix();

    interface Equals extends CheckerType {

        @Override
        default String getPrefix() {
            return "Expected to be equal to: ";
        }
    }

    interface Matches extends CheckerType {

        @Override
        default String getPrefix() {
            return "Expected to match: ";
        }
    }
}
