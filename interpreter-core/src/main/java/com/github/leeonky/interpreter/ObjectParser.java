package com.github.leeonky.interpreter;

public interface ObjectParser<P extends Procedure<?, ?, ?, ?>, T> extends Parser<P, ObjectParser<P, T>,
        ObjectParser.Mandatory<P, T>, T> {

    @Override
    default ObjectParser<P, T> castParser(Parser<P, ObjectParser<P, T>, Mandatory<P, T>, T> parser) {
        return parser::parse;
    }

    @Override
    default Mandatory<P, T> castMandatory(Parser.Mandatory<P, ObjectParser<P, T>, Mandatory<P, T>, T> mandatory) {
        return mandatory::parse;
    }

    interface Mandatory<P extends Procedure<?, ?, ?, ?>, T> extends Parser.Mandatory<P, ObjectParser<P, T>,
            ObjectParser.Mandatory<P, T>, T> {

        @Override
        default ObjectParser<P, T> castParser(Parser<P, ObjectParser<P, T>, Mandatory<P, T>, T> parser) {
            return parser::parse;
        }

        @Override
        default Mandatory<P, T> castMandatory(Parser.Mandatory<P, ObjectParser<P, T>, Mandatory<P, T>, T> mandatory) {
            return mandatory::parse;
        }
    }
}
