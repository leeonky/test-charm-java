package com.github.leeonky.interpreter;

import java.util.ArrayList;
import java.util.List;

public class InterpreterException extends RuntimeException {
    private final List<Position> positions = new ArrayList<>();

    public InterpreterException(String message, int position) {
        this(message, position, Position.Type.CHAR);
    }

    public InterpreterException(String message, int position, Position.Type type) {
        super(message);
        positions.add(new Position(type, position));
    }

    @SuppressWarnings("unchecked")
    public <E extends InterpreterException> E multiPosition(int positionBegin, Position.Type type) {
        positions.add(new Position(type, positionBegin));
        return (E) this;
    }

    public String show(String code) {
        return show(code, 0);
    }

    public String show(String code, int offset) {
        StringWithPosition stringWithPosition = new StringWithPosition(code);
        positions.forEach(position -> position.mark(stringWithPosition));
        return stringWithPosition.result(offset);
    }

    public void setType(Position.Type type) {
        if (positions.size() > 0)
            positions.set(0, new Position(type, positions.get(0).position));
    }

    public void clearPosition() {
        positions.clear();
    }

    public static class Position {
        protected final Type type;
        private final int position;

        public Position(Type type, int position) {
            this.type = type;
            this.position = position;
        }

        private void mark(StringWithPosition stringWithPosition) {
            type.mark(stringWithPosition, position);
        }

        public enum Type {
            CHAR {
                @Override
                public void mark(StringWithPosition stringWithPosition, int position) {
                    stringWithPosition.position(position);
                }
            },
            ROW {
                @Override
                public void mark(StringWithPosition stringWithPosition, int position) {
                    stringWithPosition.row(position);
                }
            },
            COLUMN {
                @Override
                public void mark(StringWithPosition stringWithPosition, int position) {
                    stringWithPosition.column(position);
                }
            };

            public abstract void mark(StringWithPosition stringWithPosition, int position);
        }
    }
}
