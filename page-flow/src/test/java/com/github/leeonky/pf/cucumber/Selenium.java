package com.github.leeonky.pf.cucumber;

import com.github.leeonky.pf.SeleniumElement;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.Objects;
import java.util.function.Supplier;

public class Selenium {
    public static class SeleniumE extends SeleniumElement<SeleniumE> {
        public SeleniumE(WebElement element) {
            super(element);
        }
    }

    public static class BrowserSelenium {
        private final Supplier<WebDriver> driverFactory;
        private WebDriver webDriver;

        public BrowserSelenium(Supplier<WebDriver> driverFactory) {
            this.driverFactory = Objects.requireNonNull(driverFactory);
        }

        public void destroy() {
            if (webDriver != null) {
                webDriver.quit();
                webDriver = null;
            }
        }

        public SeleniumE open(String url) {
            if (webDriver == null)
                webDriver = driverFactory.get();
            webDriver.get(url);
            By by = By.tagName("html");
            SeleniumE e = new SeleniumE(webDriver.findElement(by));
            e.setLocator(com.github.leeonky.pf.By.css("html"));
            return e;
        }
    }
}
