package com.github.leeonky.dal.extensions.inspector.cucumber.page;

public class Panel {
    private final SeleniumWebDriver seleniumWebDriver;

    public Panel(SeleniumWebDriver seleniumWebDriver) {
        this.seleniumWebDriver = seleniumWebDriver;
    }

    public Locator byText(String text) {
        return byXpath(String.format("//*[normalize-space(@value)='%s' or normalize-space(text())='%s']", text, text));
    }

    private Locator byXpath(String xpath) {
        return by(new By(By.XPATH, xpath));
    }

    public Locator byPlaceholder(String dalExpression) {
        return byXpath(String.format("//*[@placeholder='%s']", dalExpression));
    }

    public Locator byCss(String cssSelector) {
        return by(new By(By.CSS, cssSelector));
    }

    public Locator by(By by) {
        return new Locator(by, seleniumWebDriver);
    }
}