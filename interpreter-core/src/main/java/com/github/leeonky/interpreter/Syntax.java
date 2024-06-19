package com.github.leeonky.interpreter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class Syntax<N extends Node<?, N>, P extends Procedure<?, N, ?, ?>, PA extends Parser<P, PA, MA, T>,
        MA extends Parser.Mandatory<P, PA, MA, T>, T, R, A> {
    protected final BiFunction<P, Syntax<N, P, PA, MA, ?, ?, A>, A> parser;

    private Token token;

    protected Syntax(BiFunction<P, Syntax<N, P, PA, MA, ?, ?, A>, A> parser) {
        this.parser = parser;
    }

    public static <N extends Node<?, N>, P extends Procedure<?, N, ?, ?>, PA extends Parser<P, PA, MA, T>,
            MA extends Parser.Mandatory<P, PA, MA, T>, T> Syntax<N, P, PA, MA, T, NodeParser<N, P>, T> single(PA parser) {
        return new DefaultSyntax<N, P, PA, MA, T, NodeParser<N, P>, T>((procedure, syntax) -> {
            Optional<T> optional = parser.parse(procedure);
            if (optional.isPresent()) {
                syntax.isClose(procedure);
                syntax.close(procedure);
            }
            return optional.orElse(null);
        }) {
            @Override
            protected NodeParser<N, P> parse(Syntax<N, P, PA, MA, T, NodeParser<N, P>,
                    T> syntax, Function<T, N> factory) {
                return (P procedure) -> Optional.ofNullable(parser.apply(procedure, syntax)).map(factory);
            }
        };
    }

    public static <N extends Node<?, N>, P extends Procedure<?, N, ?, ?>, PA extends Parser<P, PA, MA, T>,
            MA extends Parser.Mandatory<P, PA, MA, T>, T> Syntax<N, P, PA, MA, T,
            NodeParser.Mandatory<N, P>, T> single(MA parser) {
        return new DefaultSyntax<N, P, PA, MA, T, NodeParser.Mandatory<N, P>, T>((procedure, syntax) -> {
            T t = parser.parse(procedure);
            syntax.isClose(procedure);
            syntax.close(procedure);
            return t;
        }) {
            @Override
            protected NodeParser.Mandatory<N, P> parse(Syntax<N, P, PA, MA, T,
                    NodeParser.Mandatory<N, P>, T> syntax, Function<T, N> factory) {
                return (P procedure) -> factory.apply(parser.apply(procedure, syntax));
            }
        };
    }

    public static <N extends Node<?, N>, P extends Procedure<?, N, ?, ?>, PA extends Parser<P, PA, MA, T>,
            MA extends Parser.Mandatory<P, PA, MA, T>, T> Syntax<N, P, PA, MA, T,
            NodeParser.Mandatory<N, P>, List<T>> many(MA mandatory) {
        return new DefaultSyntax<>((procedure, syntax) -> procedure.withColumn(() -> new ArrayList<T>() {{
            while (!syntax.isClose(procedure)) {
                add(mandatory.parse(procedure));
                procedure.incrementColumn();
                if (!syntax.isSplitter(procedure)) {
                    syntax.isClose(procedure);
                    break;
                }
            }
            syntax.close(procedure);
        }}));
    }

    public static <N extends Node<?, N>, P extends Procedure<?, N, ?, ?>, PA extends Parser<P, PA, MA, T>,
            MA extends Parser.Mandatory<P, PA, MA, T>, T> Syntax<N, P, PA, MA, T,
            NodeParser.Mandatory<N, P>, List<T>> many(PA parser) {
        return new DefaultSyntax<>((procedure, syntax) -> procedure.withColumn(() -> new ArrayList<T>() {{
            while (!syntax.isClose(procedure)) {
                Optional<T> optional = parser.parse(procedure);
                if (!optional.isPresent())
                    break;
                add(optional.get());
                procedure.incrementColumn();
                if (!syntax.isSplitter(procedure)) {
                    syntax.isClose(procedure);
                    break;
                }
            }
            syntax.close(procedure);
        }}));
    }

    protected abstract boolean isClose(P procedure);

    protected abstract void close(P procedure);

    protected abstract boolean isSplitter(P procedure);

    @SuppressWarnings("unchecked")
    protected R parse(Syntax<N, P, PA, MA, T, R, A> syntax, Function<A, N> factory) {
        return (R) (NodeParser.Mandatory<N, P>) procedure -> factory.apply(parser.apply(procedure, syntax));
    }

    public <NR, NA> Syntax<N, P, PA, MA, T, NR, NA> and(Function<Syntax<N, P, PA, MA, T, R, A>,
            Syntax<N, P, PA, MA, T, NR, NA>> rule) {
        return rule.apply(this);
    }

    public R as(Function<A, N> factory) {
        return parse(this, factory);
    }

    @SuppressWarnings("unchecked")
    public R as() {
        return parse(this, a -> (N) a);
    }

    protected Token getToken() {
        return token;
    }

    protected void setToken(Token token) {
        this.token = token;
    }

    public static class DefaultSyntax<N extends Node<?, N>, P extends Procedure<?, N, ?, ?>,
            PA extends Parser<P, PA, MA, T>, MA extends Parser.Mandatory<P, PA, MA, T>, T, R, A>
            extends Syntax<N, P, PA, MA, T, R, A> {

        public DefaultSyntax(BiFunction<P, Syntax<N, P, PA, MA, ?, ?, A>, A> parser) {
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

    public static class CompositeSyntax<N extends Node<?, N>, P extends Procedure<?, N, ?, ?>,
            PA extends Parser<P, PA, MA, T>, MA extends Parser.Mandatory<P, PA, MA, T>, T, R, A>
            extends Syntax<N, P, PA, MA, T, R, A> {

        private final Syntax<N, P, PA, MA, T, R, A> syntax;

        public CompositeSyntax(Syntax<N, P, PA, MA, T, R, A> syntax) {
            super(syntax.parser);
            this.syntax = syntax;
        }

        protected String quote(String label) {
            return label.contains("`") ? "'" + label + "'" : "`" + label + "`";
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
        protected R parse(Syntax<N, P, PA, MA, T, R, A> syntax, Function<A, N> factory) {
            return this.syntax.parse(syntax, factory);
        }

        @Override
        protected Token getToken() {
            return syntax.getToken();
        }

        @Override
        protected void setToken(Token token) {
            syntax.setToken(token);
        }
    }
}
