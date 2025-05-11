package com.github.leeonky.pf.cucumber;

import com.github.leeonky.pf.By;
import com.github.leeonky.pf.PlaywrightElement;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.util.function.Supplier;

public class Playwright {
    static final com.microsoft.playwright.Playwright playwright = com.microsoft.playwright.Playwright.create();

    public static class BrowserPlaywright {
        private final Supplier<BrowserContext> browserContextSupplier;
        private BrowserContext browserContext;

        public BrowserPlaywright(Supplier<BrowserContext> browserContextSupplier) {
            this.browserContextSupplier = browserContextSupplier;
        }

        public void destroy() {
            if (browserContext != null) {
                browserContext.close();
                browserContext = null;
            }
        }

        public PlaywrightE open(String url) {
            BrowserContext browserContext = browserContextSupplier.get();
            Page page = browserContext.newPage();
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
