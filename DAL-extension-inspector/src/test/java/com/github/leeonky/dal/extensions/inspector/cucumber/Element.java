package com.github.leeonky.dal.extensions.inspector.cucumber;

import com.github.leeonky.dal.extensions.inspector.cucumber.ui.SeleniumElement;
import org.openqa.selenium.WebElement;

public class Element extends SeleniumElement<Element, WebElement> {
    public Element(WebElement element) {
        super(element);
    }
}
