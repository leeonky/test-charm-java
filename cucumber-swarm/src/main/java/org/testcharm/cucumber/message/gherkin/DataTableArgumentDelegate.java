package org.testcharm.cucumber.message.gherkin;

import io.cucumber.core.gherkin.DataTableArgument;

import java.util.List;

public class DataTableArgumentDelegate implements DataTableArgument {
    private List<List<String>> cells;
    private int line;

    public List<List<String>> getCells() {
        return cells;
    }

    public void setCells(List<List<String>> cells) {
        this.cells = cells;
    }

    @Override
    public List<List<String>> cells() {
        return getCells();
    }

    @Override
    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }
}
