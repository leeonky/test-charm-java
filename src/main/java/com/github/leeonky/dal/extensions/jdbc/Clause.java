package com.github.leeonky.dal.extensions.jdbc;

import java.util.*;

public class Clause {
    private final String select;
    private final List<String> clauses = new ArrayList<>();
    private final String defaultJoinColumn, defaultValueColumn, defaultLink;
    private final Map<String, Object> parameters = new HashMap<>();

    public Clause(String select, List<String> clauses, String defaultJoinColumn,
                  String defaultValueColumn, String defaultLink, Map<String, Object> parameters) {
        this.select = select;
        this.clauses.addAll(clauses);
        this.defaultJoinColumn = defaultJoinColumn;
        this.defaultValueColumn = defaultValueColumn;
        this.defaultLink = defaultLink;
        this.parameters.putAll(parameters);
    }

    public Clause(String select) {
        this(select, Collections.emptyList(), null, null, null, new HashMap<>());
    }

    public String buildSql(String table) {
        StringBuilder sql = new StringBuilder().append("select ").append(select).append(" from ").append(table);
        List<String> clauses = new ArrayList<>(this.clauses);
        if (defaultLink != null) {
            clauses.add(defaultLink);
        } else {
            if (defaultJoinColumn != null && defaultValueColumn != null)
                clauses.add(defaultJoinColumn + " = :" + defaultValueColumn);
        }
        if (!clauses.isEmpty()) sql.append(" where ").append(String.join(" and ", clauses));
        return sql.toString();
    }

    public Clause select(String select) {
        return new Clause(select, clauses, defaultJoinColumn, defaultValueColumn, defaultLink, parameters);
    }

    public Clause where(String clause) {
        Clause newClause = new Clause(select, clauses, defaultJoinColumn, defaultValueColumn, defaultLink, parameters);
        newClause.clauses.add(clause);
        return newClause;
    }

    public Clause parameters(Map<String, Object> parameters) {
        return new Clause(select, clauses, defaultJoinColumn, defaultValueColumn, defaultLink, parameters);
    }

    public Map<String, Object> parameters() {
        return parameters;
    }

    public Clause defaultJoinColumn(String defaultJoinColumn) {
        return new Clause(select, clauses, defaultJoinColumn, defaultValueColumn, defaultLink, parameters);
    }

    public Clause defaultValueColumn(String defaultValueColumn) {
        return new Clause(select, clauses, defaultJoinColumn, defaultValueColumn, defaultLink, parameters);
    }

    public Clause on(String condition) {
        if (condition == null)
            return new Clause(select, clauses, null, null, null, parameters);
        if (ClauseParser.onlyColumn(condition))
            return new Clause(select, clauses, condition, defaultValueColumn, defaultLink, parameters);
        else if (ClauseParser.onlyParameter(condition))
            return new Clause(select, clauses, defaultJoinColumn, condition.substring(1), defaultLink, parameters);
        return new Clause(select, clauses, defaultJoinColumn, defaultValueColumn, condition, parameters);
    }

    public String defaultJoinColumn() {
        return defaultJoinColumn;
    }
}