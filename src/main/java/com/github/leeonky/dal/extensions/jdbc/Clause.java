package com.github.leeonky.dal.extensions.jdbc;

import java.util.ArrayList;
import java.util.List;

public class Clause {
    private final String clause;
    private final List<String> parameters = new ArrayList<>();

    public Clause(String clause) {
        StringBuilder clauseBuilder = new StringBuilder();
        StringBuilder parameterBuilder = new StringBuilder();
        boolean isParameter = false;
        for (char c : clause.toCharArray()) {
            if (isParameter) {
                if (validParameter(c)) {
                    parameterBuilder.append(c);
                    continue;
                } else {
                    parameters.add(parameterBuilder.toString());
                    parameterBuilder = new StringBuilder();
                    isParameter = false;
                }
            }
            if (c == ':') {
                clauseBuilder.append('?');
                isParameter = true;
            } else
                clauseBuilder.append(c);
        }
        this.clause = clauseBuilder.toString();
        if (parameterBuilder.length() > 0)
            parameters.add(parameterBuilder.toString());
    }

    private boolean validParameter(char c) {
        return Character.isAlphabetic(c) || Character.isDigit(c) || c == '_';
    }

    public String getClause() {
        return clause;
    }

    public List<String> getParameters() {
        return parameters;
    }
}
