package com.github.leeonky.dal.extensions.inspector.cucumber;

import com.github.leeonky.dal.extensions.inspector.cucumber.page.e.Element;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import java.util.HashMap;
import java.util.function.Supplier;

public class BrowserPlaywright {
    private final Supplier<Page> pageFactory;
    private Playwright playwright;

    public BrowserPlaywright() {
        pageFactory = () -> playwright.chromium().connect("ws://www.s.com:3000/")
                .newContext().newPage();
    }

    public void destroy() {
        if (playwright != null) {
            playwright.close();
            playwright = null;
        }
    }

    public Element open(String url) {
//        return null;
        playwright = Playwright.create();
        Page page = playwright.chromium().connect("ws://www.s.com:3000/", new BrowserType.ConnectOptions().setHeaders(
                        new HashMap<String, String>() {{
                            put("x-playwright-launch-options", "{ \"headless\": false }");
                        }}))
                .newContext().newPage();
        page.navigate(url);
        return new Element(page.locator("html"));
    }
}
