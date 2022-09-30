package com.github.leeonky.interpreter;

public interface OperatorParser<N extends Node<?, N>,
        O extends Operator<?, N, O>, P extends Procedure<?, N, ?, O>>
        extends Parser<P, OperatorParser<N, O, P>, OperatorParser.Mandatory<N, O, P>, O> {

    @Override
    default Mandatory<N, O, P> castMandatory(Parser.Mandatory<P, OperatorParser<N, O, P>,
            Mandatory<N, O, P>, O> mandatory) {
        return mandatory::parse;
    }

    @Override
    default OperatorParser<N, O, P> castParser(Parser<P, OperatorParser<N, O, P>,
            Mandatory<N, O, P>, O> parser) {
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

    interface Mandatory<N extends Node<?, N>, O extends Operator<?, N, O>, P extends Procedure<?, N, ?, O>>
            extends Parser.Mandatory<P, OperatorParser<N, O, P>, Mandatory<N, O, P>, O> {

        @Override
        default OperatorParser<N, O, P> castParser(Parser<P, OperatorParser<N, O, P>,
                Mandatory<N, O, P>, O> parser) {
            return parser::parse;
        }

        @Override
        default Mandatory<N, O, P> castMandatory(Parser.Mandatory<P, OperatorParser<N, O, P>,
                Mandatory<N, O, P>, O> mandatory) {
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
