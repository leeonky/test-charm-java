package com.github.leeonky.dal.extensions.inspector.cucumber.page;

public class TryPage {
    private final Panel panel;
    private final SubPageSwitcher outputs;

    public TryPage(Panel panel) {
        this.panel = panel;
        outputs = new SubPageSwitcher();
    }

    public StringSetter DAL() {
        return value -> panel.byPlaceholder("DAL expression").fillIn(value);
    }

    public StringSetter appendDAL() {
        return value -> panel.byPlaceholder("DAL expression").typeIn(value);
    }

    public OutputPage Root() {
        return switchTo("Root");
    }

    public OutputPage Error() {
        return switchTo("Error");
    }

    public OutputPage Result() {
        return switchTo("Result");
    }

    public OutputPage Inspect() {
        return switchTo("Inspect");
    }

    private OutputPage switchTo(String type) {
        return outputs.switchTo(() -> panel.byText(type).click(), () -> new OutputPage(panel, type),
                instance -> instance instanceof OutputPage && ((OutputPage) instance).isType(type));
    }
}
