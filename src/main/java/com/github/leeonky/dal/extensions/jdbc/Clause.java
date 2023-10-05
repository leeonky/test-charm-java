package com.github.leeonky.dal.extensions.jdbc;

import java.util.*;

public class Clause {
    private final String select;
    private final List<String> clauses = new ArrayList<>();
    private final String defaultLinkColumn, defaultParameterColumn, defaultLink, linkColumn;
    private final Map<String, Object> parameters = new HashMap<>();

    public Clause(String select, List<String> clauses, String defaultLinkColumn,
                  String defaultParameterColumn, String defaultLink, Map<String, Object> parameters, String linkColumn) {
        this.select = select;
        this.clauses.addAll(clauses);
        this.defaultLinkColumn = defaultLinkColumn;
        this.defaultParameterColumn = defaultParameterColumn;
        this.defaultLink = defaultLink;
        this.parameters.putAll(parameters);
        this.linkColumn = linkColumn;
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
            String joinColumn = linkColumn == null ? defaultLinkColumn : linkColumn;
            if (joinColumn != null && defaultParameterColumn != null)
                clauses.add(joinColumn + " = :" + defaultParameterColumn);
        }
        if (!clauses.isEmpty()) sql.append(" where ").append(String.join(" and ", clauses));
        return sql.toString();
    }

    public Clause select(String select) {
        return new Clause(select, clauses, defaultLinkColumn, defaultParameterColumn, defaultLink, parameters, linkColumn);
    }

    public Clause where(String clause) {
        Clause newClause = new Clause(select, clauses, defaultLinkColumn, defaultParameterColumn, defaultLink, parameters, linkColumn);
        newClause.clauses.add(clause);
        return newClause;
    }

    public Clause parameters(Map<String, Object> parameters) {
        return new Clause(select, clauses, defaultLinkColumn, defaultParameterColumn, defaultLink, parameters, linkColumn);
    }

    public Map<String, Object> parameters() {
        return parameters;
    }

    public Clause defaultLinkColumn(String defaultLinkColumn) {
        return new Clause(select, clauses, defaultLinkColumn, defaultParameterColumn, defaultLink, parameters, linkColumn);
    }

    public Clause defaultParameterColumn(String defaultParameterColumn) {
        return new Clause(select, clauses, defaultLinkColumn, defaultParameterColumn, defaultLink, parameters, linkColumn);
    }

    public Clause on(String condition) {
        if (condition == null)
            return new Clause(select, clauses, null, null, null, parameters, linkColumn);
        if (ClauseParser.onlyColumn(condition))
            return new Clause(select, clauses, defaultLinkColumn, defaultParameterColumn, defaultLink, parameters, condition);
        else if (ClauseParser.onlyParameter(condition))
            return new Clause(select, clauses, defaultLinkColumn, condition.substring(1), defaultLink, parameters, linkColumn);
        return new Clause(select, clauses, defaultLinkColumn, defaultParameterColumn, condition, parameters, linkColumn);
    }

    public String linkColumn() {
        return linkColumn;
    }
}