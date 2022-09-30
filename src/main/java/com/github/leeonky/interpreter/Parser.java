package com.github.leeonky.interpreter;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static java.util.Optional.empty;

public interface Parser<P extends Procedure<?, ?, ?, ?>, PA extends Parser<P, PA, MA, T>,
        MA extends Parser.Mandatory<P, PA, MA, T>, T> extends MapAble<PA, T> {

    @Override
    default PA map(UnaryOperator<T> mapper) {
        return castParser(procedure -> parse(procedure).map(mapper));
    }

    @SuppressWarnings("unchecked")
    static <P extends Procedure<?, ?, ?, ?>, PA extends Parser<P, PA, MA, T>,
            MA extends Parser.Mandatory<P, PA, MA, T>, T> PA oneOf(PA... parsers) {
        return parsers[0].castParser(procedure -> Stream.of(parsers).map(parser -> parser.parse(procedure)).
                filter(Optional::isPresent).findFirst().orElse(empty()));
    }

    static <N extends Node<?, N>, P extends Procedure<?, N, ?, ?>> ClauseParser<N, P> lazyClause(
            Supplier<ClauseParser<N, P>> parser) {
        return procedure -> parser.get().parse(procedure);
    }

    static <N extends Node<?, N>, P extends Procedure<?, N, ?, ?>> NodeParser<N, P> lazyNode(
            Supplier<NodeParser<N, P>> parser) {
        return procedure -> parser.get().parse(procedure);
    }

    Optional<T> parse(P procedure);

    default PA castParser(Parser<P, PA, MA, T> parser) {
        throw new IllegalStateException();
    }

    default MA castMandatory(Parser.Mandatory<P, PA, MA, T> mandatory) {
        throw new IllegalStateException();
    }

    default MA or(MA mandatory) {
        return castMandatory(procedure -> parse(procedure).orElseGet(() -> mandatory.parse(procedure)));
    }

    default MA mandatory(String message) {
        return castMandatory(procedure -> parse(procedure)
                .orElseThrow(() -> procedure.getSourceCode().syntaxError(message, 0)));
    }

    interface Mandatory<P extends Procedure<?, ?, ?, ?>, PA extends Parser<P, PA, MA, T>,
            MA extends Mandatory<P, PA, MA, T>, T> extends MapAble<MA, T> {

        default PA castParser(Parser<P, PA, MA, T> parser) {
            throw new IllegalStateException();
        }

        default MA castMandatory(Parser.Mandatory<P, PA, MA, T> mandatory) {
            throw new IllegalStateException();
        }

        T parse(P procedure);

        @Override
        default MA map(UnaryOperator<T> mapper) {
            return castMandatory(procedure -> mapper.apply(parse(procedure)));
        }
    }
}
