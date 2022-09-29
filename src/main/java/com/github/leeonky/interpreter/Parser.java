package com.github.leeonky.interpreter;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Optional.empty;

public interface Parser<P extends Procedure<?, ?, ?, ?, P>, PA extends Parser<P, PA, MA, T>,
        MA extends Parser.Mandatory<P, PA, MA, T>, T> {

    @SuppressWarnings("unchecked")
    static <P extends Procedure<?, ?, ?, ?, P>, PA extends Parser<P, PA, MA, T>,
            MA extends Parser.Mandatory<P, PA, MA, T>, T> PA oneOf(PA... parsers) {
        return parsers[0].castParser(procedure -> Stream.of(parsers).map(parser -> parser.parse(procedure)).
                filter(Optional::isPresent).findFirst().orElse(empty()));
    }

    static <E extends Expression<C, N, E, O>, N extends Node<C, N>, C extends RuntimeContext<C>,
            O extends Operator<C, N, O>, P extends Procedure<C, N, E, O, P>> ClauseParser<C, N, E, O, P> lazyClause(
            Supplier<ClauseParser<C, N, E, O, P>> parser) {
        return procedure -> parser.get().parse(procedure);
    }

    static <E extends Expression<C, N, E, O>, N extends Node<C, N>, C extends RuntimeContext<C>,
            O extends Operator<C, N, O>, P extends Procedure<C, N, E, O, P>> NodeParser<C, N, E, O, P> lazyNode(
            Supplier<NodeParser<C, N, E, O, P>> parser) {
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
        return castMandatory(procedure -> parse(procedure).orElseThrow(() -> procedure.getSourceCode().syntaxError(message, 0)));
    }

    interface Mandatory<P extends Procedure<?, ?, ?, ?, P>, PA extends Parser<P, PA, MA, T>,
            MA extends Mandatory<P, PA, MA, T>, T> {

        default PA castParser(Parser<P, PA, MA, T> parser) {
            throw new IllegalStateException();
        }

        T parse(P procedure);
    }
}
