package com.github.leeonky.dal.runtime.inspector;

import com.github.leeonky.dal.runtime.Data.Resolved;

import java.util.function.Function;

public interface DumperFactory extends Function<Resolved, Dumper> {
}
