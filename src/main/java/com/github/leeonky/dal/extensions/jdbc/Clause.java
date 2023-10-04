package com.github.leeonky.dal.extensions.jdbc;

import java.util.HashMap;
import java.util.Map;

public class Clause {
    final String select;
    final String clause;
    final Map<String, Object> parameters = new HashMap<>();

    public Clause(String select, String clause) {
        this.select = select;
        this.clause = clause;
    }

    public String buildSql(String table) {
        StringBuilder sql = new StringBuilder().append("select ").append(select).append(" from ").append(table);
        if (clause != null) {
            sql.append(" where ");
            sql.append(clause);
        }
        return sql.toString();
    }

    public Clause select(String select) {
        Clause newClause = new Clause(select, clause);
        newClause.parameters.putAll(parameters);
        return newClause;
    }

    public Clause where(String clause) {
        Clause newClause = new Clause(select, clause);
        newClause.parameters.putAll(parameters);
        return newClause;
    }

    public Clause parameters(Map<String, Object> parameters) {
        Clause newClause = new Clause(select, clause);
        newClause.parameters.putAll(parameters);
        return newClause;
    }
}