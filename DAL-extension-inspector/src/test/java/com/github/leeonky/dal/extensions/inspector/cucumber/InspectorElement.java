package com.github.leeonky.dal.extensions.inspector.cucumber;

import com.github.leeonky.dal.extensions.inspector.cucumber.page.SeleniumElement;
import org.openqa.selenium.WebElement;

public class InspectorElement extends SeleniumElement<InspectorElement, WebElement> {
    public InspectorElement(WebElement element) {
        super(element);
    }
}
