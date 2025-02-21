package com.github.leeonky.dal.extensions.inspector.cucumber.page;

public class TryPage {
    private final Panel panel;
    private final SubPageSwitcher<OutputPage> outputs = new SubPageSwitcher<>();

    public TryPage(Panel panel) {
        this.panel = panel;
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
        return outputs.switchTo(new Target<OutputPage>() {
            @Override
            public OutputPage create() {
                return new OutputPage(panel.byCss(".tab-content[target='" + type.toLowerCase() + "']"), type);
            }

            @Override
            public void navigateTo() {
                panel.byText(type).click();
            }

            @Override
            public boolean matches(OutputPage current) {
                return current.isType(type);
            }
        });
    }
}
