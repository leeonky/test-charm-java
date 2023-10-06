package com.github.leeonky.dal.extensions.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Stream.of;

public class Query {
    private final String select;
    private final List<String> clauses = new ArrayList<>();
    private final String defaultLinkColumn, defaultParameterColumn, defaultLink, linkColumn;
    private final Map<String, Object> parameters = new HashMap<>();
    private final String name;

    public Query(String name, String select, List<String> clauses, String defaultLinkColumn,
                 String defaultParameterColumn, String defaultLink, Map<String, Object> parameters, String linkColumn) {
        this.select = select;
        this.clauses.addAll(clauses);
        this.defaultLinkColumn = defaultLinkColumn;
        this.defaultParameterColumn = defaultParameterColumn;
        this.defaultLink = defaultLink;
        this.parameters.putAll(parameters);
        this.linkColumn = linkColumn;
        this.name = name;
    }

    public Query(String name, String select) {
        this(name, select, Collections.emptyList(), null, null, null, new HashMap<>(), null);
    }

    public String buildSql() {
        return String.format("select %s from %s%s", select, name, buildWhere());
    }

    private String buildWhere() {
        String clause = Stream.concat(clauses.stream(), of(resolveLinkClause()))
                .filter(Objects::nonNull).collect(Collectors.joining(" and "));
        return clause.isEmpty() ? "" : " where " + clause;
    }

    private String resolveLinkClause() {
        return defaultLink != null ? defaultLink : defaultLinkClause();
    }

    private String defaultLinkClause() {
        String joinColumn = linkColumn == null ? defaultLinkColumn : linkColumn;
        if (joinColumn != null && defaultParameterColumn != null)
            return joinColumn + " = :" + defaultParameterColumn;
        return null;
    }

    public Query select(String select) {
        return new Query(name, select, clauses, defaultLinkColumn, defaultParameterColumn, defaultLink, parameters, linkColumn);
    }

    public Query where(String clause) {
        Query newQuery = new Query(name, select, clauses, defaultLinkColumn, defaultParameterColumn, defaultLink, parameters, linkColumn);
        newQuery.clauses.add(clause);
        return newQuery;
    }

    public Query parameters(Map<String, Object> parameters) {
        return new Query(name, select, clauses, defaultLinkColumn, defaultParameterColumn, defaultLink, parameters, linkColumn);
    }

    public Map<String, Object> parameters() {
        return parameters;
    }

    public Query defaultLinkColumn(String defaultLinkColumn) {
        return new Query(name, select, clauses, defaultLinkColumn, defaultParameterColumn, defaultLink, parameters, linkColumn);
    }

    public Query defaultParameterColumn(String defaultParameterColumn) {
        return new Query(name, select, clauses, defaultLinkColumn, defaultParameterColumn, defaultLink, parameters, linkColumn);
    }

    public Query on(String condition) {
        if (condition == null)
            return new Query(name, select, clauses, null, null, null, parameters, linkColumn);
        if (ClauseParser.onlyColumn(condition))
            return new Query(name, select, clauses, defaultLinkColumn, defaultParameterColumn, defaultLink, parameters, condition);
        else if (ClauseParser.onlyParameter(condition))
            return new Query(name, select, clauses, defaultLinkColumn, condition.substring(1), defaultLink, parameters, linkColumn);
        return new Query(name, select, clauses, defaultLinkColumn, defaultParameterColumn, condition, parameters, linkColumn);
    }

    public String linkColumn() {
        return linkColumn;
    }

    ResultSet execute(Connection connection1) throws SQLException {
        ClauseParser parser = new ClauseParser(buildSql());
        PreparedStatement preparedStatement = connection1.prepareStatement(parser.getClause());
        int parameterIndex = 1;
        for (String parameter : parser.getParameters())
            preparedStatement.setObject(parameterIndex++, parameters().get(parameter));
        ResultSet resultSet = preparedStatement.executeQuery();
        return resultSet;
    }
}