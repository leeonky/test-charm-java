package com.github.leeonky.dal.extensions.jdbc;

import java.util.*;

public class Clause {
    private final String select;
    private final List<String> clauses = new ArrayList<>();
    private final String defaultJoinColumn, defaultValueColumn, defaultLink, joinColumn;
    private final Map<String, Object> parameters = new HashMap<>();

    public Clause(String select, List<String> clauses, String defaultJoinColumn,
                  String defaultValueColumn, String defaultLink, Map<String, Object> parameters, String joinColumn) {
        this.select = select;
        this.clauses.addAll(clauses);
        this.defaultJoinColumn = defaultJoinColumn;
        this.defaultValueColumn = defaultValueColumn;
        this.defaultLink = defaultLink;
        this.parameters.putAll(parameters);
        this.joinColumn = joinColumn;
    }

    public Clause(String select) {
        this(select, Collections.emptyList(), null, null, null, new HashMap<>(), null);
    }

    public String buildSql(String table) {
        StringBuilder sql = new StringBuilder().append("select ").append(select).append(" from ").append(table);
        List<String> clauses = new ArrayList<>(this.clauses);
        if (defaultLink != null) {
            clauses.add(defaultLink);
        } else {
            String joinColumn = this.joinColumn == null ? defaultJoinColumn : this.joinColumn;
            if (joinColumn != null && defaultValueColumn != null)
                clauses.add(joinColumn + " = :" + defaultValueColumn);
        }
        if (!clauses.isEmpty()) sql.append(" where ").append(String.join(" and ", clauses));
        return sql.toString();
    }

    public Clause select(String select) {
        return new Clause(select, clauses, defaultJoinColumn, defaultValueColumn, defaultLink, parameters, joinColumn);
    }

    public Clause where(String clause) {
        Clause newClause = new Clause(select, clauses, defaultJoinColumn, defaultValueColumn, defaultLink, parameters, joinColumn);
        newClause.clauses.add(clause);
        return newClause;
    }

    public Clause parameters(Map<String, Object> parameters) {
        return new Clause(select, clauses, defaultJoinColumn, defaultValueColumn, defaultLink, parameters, joinColumn);
    }

    public Map<String, Object> parameters() {
        return parameters;
    }

    public Clause defaultJoinColumn(String defaultJoinColumn) {
        return new Clause(select, clauses, defaultJoinColumn, defaultValueColumn, defaultLink, parameters, joinColumn);
    }

    public Clause defaultValueColumn(String defaultValueColumn) {
        return new Clause(select, clauses, defaultJoinColumn, defaultValueColumn, defaultLink, parameters, joinColumn);
    }

    public Clause on(String condition) {
        if (condition == null)
            return new Clause(select, clauses, null, null, null, parameters, joinColumn);
        if (ClauseParser.onlyColumn(condition))
            return new Clause(select, clauses, defaultJoinColumn, defaultValueColumn, defaultLink, parameters, condition);
        else if (ClauseParser.onlyParameter(condition))
            return new Clause(select, clauses, defaultJoinColumn, condition.substring(1), defaultLink, parameters, joinColumn);
        return new Clause(select, clauses, defaultJoinColumn, defaultValueColumn, condition, parameters, joinColumn);
    }

    public String joinColumn() {
        return joinColumn;
    }
}