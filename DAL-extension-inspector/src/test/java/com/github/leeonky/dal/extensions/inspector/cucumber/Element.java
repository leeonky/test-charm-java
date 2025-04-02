package com.github.leeonky.dal.extensions.inspector.cucumber;

import com.github.leeonky.dal.extensions.inspector.cucumber.ui.SeleniumElement;
import org.openqa.selenium.WebElement;

import static java.util.Arrays.binarySearch;

public class Element extends SeleniumElement<Element> {
    public Element(WebElement element) {
        super(element);
    }

    @Override
    public boolean isInput() {
        return isCheckedBox() || super.isInput();
    }

    @Override
    public Element typeIn(String value) {
        if (isCheckedBox()) {
            if (!value.equals(value()))
                click();
            return this;
        } else
            return super.typeIn(value);
    }

    private boolean isCheckedBox() {
        return binarySearch((String[]) attribute("class"), "switch") >= 0;
    }
}
