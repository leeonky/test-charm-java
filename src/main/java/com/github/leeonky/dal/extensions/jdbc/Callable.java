package com.github.leeonky.dal.extensions.jdbc;

import java.util.function.Function;

interface Callable<T> extends Function<String, T> {
}
