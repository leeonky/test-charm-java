package org.testcharm.dal.extensions.inspector.cucumber;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import org.testcharm.dal.extensions.inspector.cucumber.page.e.Element;
import org.testcharm.pf.By;

import java.util.function.Function;

public class BrowserContextPlaywright {
    private com.microsoft.playwright.Playwright playwright;
    private Browser browser;
    private BrowserContext browserContext;
    private final Function<com.microsoft.playwright.Playwright, Browser> browserSupplier;

    public BrowserContextPlaywright(Function<com.microsoft.playwright.Playwright, Browser> browserSupplier) {
        this.browserSupplier = browserSupplier;
    }

    public void destroy() {
        if (browserContext != null) {
            browserContext.close();
            browserContext = null;
        }
    }

    public void destroyAll() {
        destroy();
        if (browser != null) {
            browser.close();
            browser = null;
        }
        if (playwright != null) {
            playwright.close();
            playwright = null;
        }
    }

    public Element open(String url) {
        if (playwright == null)
            playwright = com.microsoft.playwright.Playwright.create();
        if (browser == null)
            browser = browserSupplier.apply(playwright);
        if (browserContext == null)
            browserContext = browser.newContext();

        Page page = browserContext.newPage();
        page.navigate(url);
        Element e = new Element(page.locator("html"));
        e.setLocator(By.css("html"));
        return e;
    }
}
