package com.github.leeonky.dal.extensions.jdbc;

import java.util.List;
import java.util.Map;

public interface Query {
    Query clause(String clause);

    List<Map<String, Object>> query();

    Query through(String clause);
}
