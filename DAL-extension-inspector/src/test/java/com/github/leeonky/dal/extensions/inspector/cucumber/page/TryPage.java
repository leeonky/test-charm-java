package com.github.leeonky.dal.extensions.inspector.cucumber.page;

public class TryPage {
    private final Browser browser;
    private final ViewFrame outputs;

    public TryPage(Browser browser) {
        this.browser = browser;
        outputs = new ViewFrame(browser);
    }

    public StringSetter DAL() {
        return value -> browser.byPlaceholder("DAL expression").fillIn(value);
    }

    public StringSetter appendDAL() {
        return value -> browser.byPlaceholder("DAL expression").typeIn(value);
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
        return outputs.switchTo(() -> browser.byText(type).click(), () -> new OutputPage(browser, type),
                instance -> instance instanceof OutputPage && ((OutputPage) instance).isType(type));
    }
}
