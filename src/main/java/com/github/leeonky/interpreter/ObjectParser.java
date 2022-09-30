package com.github.leeonky.interpreter;

public interface ObjectParser<P extends Procedure<?, ?, ?, ?>, T> extends Parser<P, ObjectParser<P, T>,
        ObjectParser.Mandatory<P, T>, T> {

    interface Mandatory<P extends Procedure<?, ?, ?, ?>, T> extends Parser.Mandatory<P, ObjectParser<P, T>,
            ObjectParser.Mandatory<P, T>, T> {
    }
}
