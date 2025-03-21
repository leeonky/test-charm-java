package com.github.leeonky.dal.extensions.inspector.cucumber.page;

import java.util.List;
import java.util.stream.Collectors;

import static com.github.leeonky.util.function.Extension.not;
import static org.awaitility.Awaitility.await;

public class Panel {
    public final SeleniumWebElement element;

    public Panel(SeleniumWebElement element) {
        this.element = element;
    }

    public List<Panel> allBy(By by) {
        return element.findAll(by).stream().map(Panel::new).collect(Collectors.toList());
    }

    public Panel by(By by) {
        List<Panel> list = await("No elements found by: " + by).ignoreExceptions().until(() -> allBy(by), not(List::isEmpty));
        if (list.size() > 1)
            throw new IllegalStateException("Found more than one elements: " + by.toString());
        return list.get(0);
    }

    public Panel byXpath(String xpath) {
        return by(new By(By.XPATH, xpath));
    }

    public List<Panel> allByXpath(String xpath) {
        return allBy(new By(By.XPATH, xpath));
    }

    public Panel byPlaceholder(String dalExpression) {
        return byXpath(String.format(".//*[@placeholder='%s']", dalExpression));
    }

    public Panel byText(String text) {
        return byXpath(String.format(".//*[normalize-space(@value)='%s' or normalize-space(text())='%s']", text, text));
    }

    public Panel byTag(String tag) {
        return by(new By(By.TAG, tag));
    }

    public List<Panel> allByTag(String tag) {
        return allBy(new By(By.TAG, tag));
    }

    public String attribute(String name) {
        return element.getAttribute(name);
    }

    public List<Panel> allByCss(String css) {
        return allBy(new By(By.CSS, css));
    }

    public Panel byCss(String css) {
        return by(new By(By.CSS, css));
    }

    public void click() {
        element.click();
    }

    public String text() {
        return element.getText();
    }

    public void fillIn(String text) {
        element.clear();
        element.sendKeys(text);
    }

    public void typeIn(String text) {
        element.sendKeys(text);
    }
}