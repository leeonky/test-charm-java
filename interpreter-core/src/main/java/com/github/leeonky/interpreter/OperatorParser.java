package com.github.leeonky.interpreter;

public interface OperatorParser<C extends RuntimeContext, N extends Node<C, N>,
        O extends Operator<C, N, O, E>, P extends Procedure<C, N, E, O>, E extends Expression<C, N, E, O>>
        extends Parser<P, OperatorParser<C, N, O, P, E>, OperatorParser.Mandatory<C, N, O, P, E>, O> {

    @Override
    default Mandatory<C, N, O, P, E> castMandatory(Parser.Mandatory<P, OperatorParser<C, N, O, P, E>,
            Mandatory<C, N, O, P, E>, O> mandatory) {
        return mandatory::parse;
    }

    @Override
    default OperatorParser<C, N, O, P, E> castParser(Parser<P, OperatorParser<C, N, O, P, E>,
            Mandatory<C, N, O, P, E>, O> parser) {
        return parser::parse;
    }

    default ClauseParser<N, P> clause(NodeParser.Mandatory<N, P> nodeFactory) {
        return procedure -> parse(procedure).map(operator -> procedure.underOperator(operator, () -> {
            N right = nodeFactory.parse(procedure);
            return left -> procedure.createExpression(left, operator, right);
        }));
    }

    default ClauseParser<N, P> clause(NodeParser<N, P> nodeParser) {
        return procedure -> procedure.getSourceCode().tryFetch(() -> parse(procedure).map(operator ->
                procedure.underOperator(operator, () -> nodeParser.parse(procedure).<Clause<N>>map(n ->
                        left -> procedure.createExpression(left, operator, n)).orElse(null))));
    }

    default NodeParser<N, P> unary(NodeParser.Mandatory<N, P> nodeFactory) {
        return procedure -> parse(procedure).map(operator -> procedure.underOperator(operator, () ->
                procedure.createExpression(null, operator, nodeFactory.parse(procedure))));
    }

    interface Mandatory<C extends RuntimeContext, N extends Node<C, N>, O extends Operator<C, N, O, E>,
            P extends Procedure<C, N, E, O>, E extends Expression<C, N, E, O>>
            extends Parser.Mandatory<P, OperatorParser<C, N, O, P, E>, Mandatory<C, N, O, P, E>, O> {

        @Override
        default OperatorParser<C, N, O, P, E> castParser(Parser<P, OperatorParser<C, N, O, P, E>,
                Mandatory<C, N, O, P, E>, O> parser) {
            return parser::parse;
        }

        @Override
        default Mandatory<C, N, O, P, E> castMandatory(Parser.Mandatory<P, OperatorParser<C, N, O, P, E>,
                Mandatory<C, N, O, P, E>, O> mandatory) {
            return mandatory::parse;
        }

        default ClauseParser.Mandatory<N, P> clause(NodeParser.Mandatory<N, P> nodeFactory) {
            return procedure -> {
                O operator = parse(procedure);
                return procedure.underOperator(operator, () -> {
                    N right = nodeFactory.parse(procedure);
                    return left -> procedure.createExpression(left, operator, right);
                });
            };
        }
    }
}
