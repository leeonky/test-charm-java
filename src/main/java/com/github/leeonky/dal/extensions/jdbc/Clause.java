package com.github.leeonky.dal.extensions.jdbc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Clause {
    private final String select;
    private final String clause;
    private final String defaultJoinColumn, defaultValueColumn, defaultLink;
    final Map<String, Object> parameters = new HashMap<>();

    public Clause(String select, String clause, String defaultJoinColumn, String defaultValueColumn, String defaultLink, Map<String, Object> parameters) {
        this.select = select;
        this.clause = clause;
        this.defaultJoinColumn = defaultJoinColumn;
        this.defaultValueColumn = defaultValueColumn;
        this.defaultLink = defaultLink;
        this.parameters.putAll(parameters);
    }

    public Clause(String select) {
        this(select, null, null, null, null, new HashMap<>());
    }

    public String buildSql(String table) {
        StringBuilder sql = new StringBuilder().append("select ").append(select).append(" from ").append(table);
        List<String> clauses = new ArrayList<>();
        if (defaultLink != null) {
            clauses.add(defaultLink);
        } else {
            if (defaultJoinColumn != null && defaultValueColumn != null)
                clauses.add(defaultJoinColumn + " = :" + defaultValueColumn);
        }
        if (clause != null)
            clauses.add(clause);
        if (!clauses.isEmpty()) sql.append(" where ").append(String.join(" and ", clauses));
        return sql.toString();
    }

    public Clause select(String select) {
        return new Clause(select, clause, defaultJoinColumn, defaultValueColumn, defaultLink, parameters);
    }

    public Clause where(String clause) {
        return new Clause(select, clause, defaultJoinColumn, defaultValueColumn, defaultLink, parameters);
    }

    public Clause parameters(Map<String, Object> parameters) {
        return new Clause(select, clause, defaultJoinColumn, defaultValueColumn, defaultLink, parameters);
    }

    public Clause defaultJoinColumn(String defaultJoinColumn) {
        return new Clause(select, clause, defaultJoinColumn, defaultValueColumn, defaultLink, parameters);
    }

    public Clause defaultValueColumn(String defaultValueColumn) {
        return new Clause(select, clause, defaultJoinColumn, defaultValueColumn, defaultLink, parameters);
    }

    public Clause on(String condition) {
        if (ClauseParser.onlyColumn(condition))
            return new Clause(select, clause, condition, defaultValueColumn, defaultLink, parameters);
        else if (ClauseParser.onlyParameter(condition))
            return new Clause(select, clause, defaultJoinColumn, condition.substring(1), defaultLink, parameters);
        return new Clause(select, clause, defaultJoinColumn, defaultValueColumn, condition, parameters);
    }
}