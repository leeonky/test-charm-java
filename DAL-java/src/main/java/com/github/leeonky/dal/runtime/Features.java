package com.github.leeonky.dal.runtime;

public class Features {
    private Level level = Level.ERROR;

    public void ambiguousMissedComma(Level level) {
        this.level = level;
    }

    public Level ambiguousMissedComma() {
        return level;
    }

    public enum Level {
        ERROR, WARNING, NONE
    }
}
