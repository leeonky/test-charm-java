package com.github.leeonky.dal.extensions.inspector.cucumber;

import com.github.leeonky.dal.extensions.inspector.cucumber.page.e.Element;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;

import java.util.function.Supplier;

public class BrowserPlaywright {
    private final Supplier<Browser> browserSupplier;
    private Browser browser;

    public BrowserPlaywright(Supplier<Browser> browserSupplier) {
        this.browserSupplier = browserSupplier;
    }

    public void destroy() {
        if (browser != null) {
            browser.close();
            browser = null;
        }
    }

    public Element open(String url) {
        browser = browserSupplier.get();
        Page page = browser.newContext().newPage();
        page.navigate(url);
        return new Element(page.locator("html"));
    }
}
