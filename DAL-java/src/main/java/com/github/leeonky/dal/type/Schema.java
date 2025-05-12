package com.github.leeonky.dal.type;

import com.github.leeonky.dal.runtime.Data;

public interface Schema {
    default void verify(Data<?> data) {
    }
}
