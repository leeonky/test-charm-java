package com.github.leeonky.dal.extensions.jdbc;

import com.github.leeonky.util.Suppressor;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DataBase {
    private final Connection connection;
    private final Function<Statement, Collection<String>> tableQuerier;
    private final Function<Statement, Collection<String>> viewQuerier;

    DataBase(Connection connection, Function<Statement, Collection<String>> tableQuerier,
             Function<Statement, Collection<String>> viewQuerier) {
        this.connection = connection;
        this.tableQuerier = tableQuerier;
        this.viewQuerier = viewQuerier;
    }

    public Map<String, Table> tables() {
        return Suppressor.get(() -> tableQuerier.apply(connection.createStatement())).stream()
                .collect(Collectors.toMap(Function.identity(), Table::new));
    }

    public Map<String, Table> views() {
        return Suppressor.get(() -> viewQuerier.apply(connection.createStatement())).stream()
                .collect(Collectors.toMap(Function.identity(), Table::new));
    }

    public class Table {
        private final String name;

        public Table(String name) {
            this.name = name;
        }
    }
}
