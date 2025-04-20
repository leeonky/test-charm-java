package com.github.leeonky.pf.cucumber;

import com.github.leeonky.pf.By;
import com.github.leeonky.pf.PlaywrightElement;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.util.function.Supplier;

public class Playwright {
    static final com.microsoft.playwright.Playwright playwright = com.microsoft.playwright.Playwright.create();

    public static class BrowserPlaywright {
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

        public PlaywrightE open(String url) {
            browser = browserSupplier.get();
            Page page = browser.newContext().newPage();
            page.navigate(url);
            PlaywrightE e = new PlaywrightE(page.locator("html"));
            e.setLocator(By.css("html"));
            return e;
        }
    }

    public static class PlaywrightE extends PlaywrightElement<PlaywrightE> {
        public PlaywrightE(Locator locator) {
            super(locator);
        }
    }
}
