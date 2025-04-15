package com.github.leeonky.dal.runtime;

import java.util.function.Function;

public interface RuntimeHandler<R extends RuntimeData<?>> extends Function<R, Object> {
}
