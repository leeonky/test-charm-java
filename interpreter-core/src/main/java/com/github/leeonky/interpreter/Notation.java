package com.github.leeonky.interpreter;

import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toSet;

public class Notation<C extends RuntimeContext, N extends Node<C, N>, O extends Operator<C, N, O, E>,
        P extends Procedure<C, N, E, O>, E extends Expression<C, N, E, O>>
        implements ObjectParser<P, String> {
    private final String label;

    private Notation(String label) {
        this.label = label;
    }

    public static <C extends RuntimeContext, N extends Node<C, N>, O extends Operator<C, N, O, E>,
            P extends Procedure<C, N, E, O>, E extends Expression<C, N, E, O>>
    Notation<C, N, O, P, E> notation(String label) {
        return new Notation<>(label);
    }

    public String getLabel() {
        return label;
    }

    public int length() {
        return label.length();
    }

    private Optional<Token> getToken(P procedure, Predicate<P> predicate) {
        return procedure.getSourceCode().popWord(this, () -> predicate.test(procedure));
    }

    private Optional<Token> getToken(P procedure) {
        return getToken(procedure, p -> true);
    }

    public NodeParser<N, P> node(Function<String, N> factory) {
        return procedure -> getToken(procedure).map(token ->
                factory.apply(token.getContent()).setPositionBegin(token.getPosition()));
    }

    public NodeParser<N, P> wordNode(Function<String, N> factory, Set<String> delimiter) {
        return procedure -> procedure.getSourceCode().tryFetch(() -> getToken(procedure).map(token ->
                notAWord(delimiter, procedure) ? null :
                        factory.apply(token.getContent()).setPositionBegin(token.getPosition())));
    }

    private boolean notAWord(Set<String> delimiter, P procedure) {
        return procedure.getSourceCode().hasCode()
               && delimiter.stream().noneMatch(s -> procedure.getSourceCode().startsWith(s));
    }

    public OperatorParser<C, N, O, P, E> operator(Supplier<O> factory, Predicate<P> predicate) {
        return procedure -> getToken(procedure, predicate).map(token -> factory.get().setPosition(token.getPosition()));
    }

    public OperatorParser<C, N, O, P, E> operator(Supplier<O> factory) {
        return operator(factory, procedure -> true);
    }

    public OperatorParser<C, N, O, P, E> keywordOperator(Supplier<O> factory, Set<String> Delimiter) {
        return procedure -> procedure.getSourceCode().tryFetch(() -> operator(factory, p -> true)
                .parse(procedure).map(operator -> notAWord(Delimiter, procedure) ? null : operator));
    }

    public NodeParser<N, P> with(NodeParser.Mandatory<N, P> mandatory) {
        return procedure -> getToken(procedure).map(t -> mandatory.parse(procedure).setPositionBegin(t.getPosition()));
    }

    public <PA extends Parser<P, PA, MA, T>, MA extends Parser.Mandatory<P, PA, MA, T>, T> PA before(PA parser) {
        return parser.castParser(procedure -> procedure.getSourceCode().tryFetch(() -> getToken(procedure)
                .flatMap(t -> parser.parse(procedure))));
    }

    public <PA extends Parser<P, PA, MA, T>, MA extends Parser.Mandatory<P, PA, MA, T>, T> PA before(MA ma) {
        return ma.castParser(procedure -> getToken(procedure).map(t -> ma.parse(procedure)));
    }

    public ClauseParser<N, P> clause(BiFunction<Token, N, N> nodeFactory) {
        return procedure -> getToken(procedure).map(token -> input ->
                nodeFactory.apply(token, input).setPositionBegin(token.getPosition()));
    }

    @Override
    public Optional<String> parse(P procedure) {
        return procedure.getSourceCode().popString(getLabel());
    }

    public Set<Notation<C, N, O, P, E>> postfix(Set<?> postfixes) {
        return postfixes.stream().map(c -> getLabel() + c).map(label -> Notation.<C, N, O, P, E>notation(label)).collect(toSet());
    }
}
