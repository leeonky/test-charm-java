package com.github.leeonky.interpreter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class Syntax<C extends RuntimeContext<C>, N extends Node<C, N>, E extends Expression<C, N, E, O>,
        O extends Operator<C, N, O>, P extends Procedure<C, N, E, O, P>, PA extends Parser<C, N, E, O, P, PA, MA, T>,
        MA extends Parser.Mandatory<C, N, E, O, P, PA, MA, T>, T, R, A> {
    protected final BiFunction<P, Syntax<C, N, E, O, P, PA, MA, ?, ?, A>, A> parser;

    protected Syntax(BiFunction<P, Syntax<C, N, E, O, P, PA, MA, ?, ?, A>, A> parser) {
        this.parser = parser;
    }

    public static <C extends RuntimeContext<C>, N extends Node<C, N>, E extends Expression<C, N, E, O>,
            O extends Operator<C, N, O>, P extends Procedure<C, N, E, O, P>, PA extends Parser<C, N, E, O, P, PA, MA, T>,
            MA extends Parser.Mandatory<C, N, E, O, P, PA, MA, T>, T> Syntax<C, N, E, O, P, PA, MA, T,
            NodeParser<C, N, E, O, P>, T> single(PA parser) {
        return new DefaultSyntax<C, N, E, O, P, PA, MA, T, NodeParser<C, N, E, O, P>, T>((procedure, syntax) -> {
            Optional<T> optional = parser.parse(procedure);
            if (optional.isPresent()) {
                syntax.isClose(procedure);
                syntax.close(procedure);
            }
            return optional.orElse(null);
        }) {
            @Override
            protected NodeParser<C, N, E, O, P> parse(Syntax<C, N, E, O, P, PA, MA, T, NodeParser<C, N, E, O, P>,
                    T> syntax, Function<T, N> factory) {
                return (P procedure) -> Optional.ofNullable(parser.apply(procedure, syntax)).map(factory);
            }
        };
    }

    public static <C extends RuntimeContext<C>, N extends Node<C, N>, E extends Expression<C, N, E, O>,
            O extends Operator<C, N, O>, P extends Procedure<C, N, E, O, P>, PA extends Parser<C, N, E, O, P, PA, MA, T>,
            MA extends Parser.Mandatory<C, N, E, O, P, PA, MA, T>, T> Syntax<C, N, E, O, P, PA, MA, T,
            NodeParser.Mandatory<C, N, E, O, P>, T> single(MA parser) {
        return new DefaultSyntax<C, N, E, O, P, PA, MA, T, NodeParser.Mandatory<C, N, E, O, P>, T>((procedure, syntax) -> {
            T t = parser.parse(procedure);
            syntax.isClose(procedure);
            syntax.close(procedure);
            return t;
        }) {
            @Override
            protected NodeParser.Mandatory<C, N, E, O, P> parse(Syntax<C, N, E, O, P, PA, MA, T,
                    NodeParser.Mandatory<C, N, E, O, P>, T> syntax, Function<T, N> factory) {
                return (P procedure) -> factory.apply(parser.apply(procedure, syntax));
            }
        };
    }

    public static <C extends RuntimeContext<C>, N extends Node<C, N>, E extends Expression<C, N, E, O>,
            O extends Operator<C, N, O>, P extends Procedure<C, N, E, O, P>, PA extends Parser<C, N, E, O, P, PA, MA, T>,
            MA extends Parser.Mandatory<C, N, E, O, P, PA, MA, T>, T> Syntax<C, N, E, O, P, PA, MA, T,
            NodeParser.Mandatory<C, N, E, O, P>, List<T>> many(MA mandatory) {
        return new DefaultSyntax<>((procedure, syntax) -> procedure.withColumn(() -> new ArrayList<T>() {{
            while (!syntax.isClose(procedure)) {
                add(mandatory.parse(procedure));
                procedure.incrementColumn();
                if (!syntax.isSplitter(procedure))
                    break;
            }
            syntax.close(procedure);
        }}));
    }

    public static <C extends RuntimeContext<C>, N extends Node<C, N>, E extends Expression<C, N, E, O>,
            O extends Operator<C, N, O>, P extends Procedure<C, N, E, O, P>, PA extends Parser<C, N, E, O, P, PA, MA, T>,
            MA extends Parser.Mandatory<C, N, E, O, P, PA, MA, T>, T> Syntax<C, N, E, O, P, PA, MA, T,
            NodeParser.Mandatory<C, N, E, O, P>, List<T>> many(PA parser) {
        return new DefaultSyntax<>((procedure, syntax) -> procedure.withColumn(() -> new ArrayList<T>() {{
            while (!syntax.isClose(procedure)) {
                Optional<T> optional = parser.parse(procedure);
                if (!optional.isPresent())
                    break;
                add(optional.get());
                procedure.incrementColumn();
                if (!syntax.isSplitter(procedure))
                    break;
            }
            syntax.close(procedure);
        }}));
    }

    protected abstract boolean isClose(P procedure);

    protected abstract void close(P procedure);

    protected abstract boolean isSplitter(P procedure);

    @SuppressWarnings("unchecked")
    protected R parse(Syntax<C, N, E, O, P, PA, MA, T, R, A> syntax, Function<A, N> factory) {
        return (R) (NodeParser.Mandatory<C, N, E, O, P>) procedure -> factory.apply(parser.apply(procedure, syntax));
    }

    public <NR, NA> Syntax<C, N, E, O, P, PA, MA, T, NR, NA> and(Function<Syntax<C, N, E, O, P, PA, MA, T, R, A>,
            Syntax<C, N, E, O, P, PA, MA, T, NR, NA>> rule) {
        return rule.apply(this);
    }

    public R as(Function<A, N> factory) {
        return parse(this, factory);
    }

    @SuppressWarnings("unchecked")
    public R as() {
        return parse(this, a -> (N) a);
    }

    public static class DefaultSyntax<C extends RuntimeContext<C>, N extends Node<C, N>, E extends Expression<C, N, E, O>,
            O extends Operator<C, N, O>, P extends Procedure<C, N, E, O, P>, PA extends Parser<C, N, E, O, P, PA, MA, T>,
            MA extends Parser.Mandatory<C, N, E, O, P, PA, MA, T>, T, R, A> extends Syntax<C, N, E, O, P, PA, MA, T, R, A> {

        public DefaultSyntax(BiFunction<P, Syntax<C, N, E, O, P, PA, MA, ?, ?, A>, A> parser) {
            super(parser);
        }

        @Override
        protected boolean isClose(P procedure) {
            return false;
        }

        @Override
        protected void close(P procedure) {
        }

        @Override
        protected boolean isSplitter(P procedure) {
            return true;
        }
    }

    public static class CompositeSyntax<C extends RuntimeContext<C>, N extends Node<C, N>, E extends Expression<C, N, E, O>,
            O extends Operator<C, N, O>, P extends Procedure<C, N, E, O, P>, PA extends Parser<C, N, E, O, P, PA, MA, T>,
            MA extends Parser.Mandatory<C, N, E, O, P, PA, MA, T>, T, R, A> extends Syntax<C, N, E, O, P, PA, MA, T, R, A> {

        private final Syntax<C, N, E, O, P, PA, MA, T, R, A> syntax;

        public CompositeSyntax(Syntax<C, N, E, O, P, PA, MA, T, R, A> syntax) {
            super(syntax.parser);
            this.syntax = syntax;
        }

        @Override
        protected boolean isClose(P procedure) {
            return syntax.isClose(procedure);
        }

        @Override
        protected void close(P procedure) {
            syntax.close(procedure);
        }

        @Override
        protected boolean isSplitter(P procedure) {
            return syntax.isSplitter(procedure);
        }

        @Override
        protected R parse(Syntax<C, N, E, O, P, PA, MA, T, R, A> syntax, Function<A, N> factory) {
            return this.syntax.parse(syntax, factory);
        }
    }
}
