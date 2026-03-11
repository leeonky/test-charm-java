package org.testcharm.pf.cucumber;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.testcharm.pf.By;
import org.testcharm.pf.PlaywrightElement;

import java.util.function.Function;

public class Playwright {
    public static class BrowserContextPlaywright {
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

        public PlaywrightE open(String url) {
            if (playwright == null)
                playwright = com.microsoft.playwright.Playwright.create();
            if (browser == null)
                browser = browserSupplier.apply(playwright);
            if (browserContext == null)
                browserContext = browser.newContext();

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
