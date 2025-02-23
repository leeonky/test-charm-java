package com.github.leeonky.dal.extensions.inspector.cucumber.page;

import java.util.List;

import static java.lang.String.format;

public class MainPage {
    private final Panel panel;
    private final PageContainer<Object> remotes = new PageContainer();

    public MainPage(SeleniumWebDriver driver) {
        driver.open("http://host.docker.internal:10081");
        panel = new Panel(driver.findAll(new By("css", "body")).get(0));
    }

    public Panel title() {
        return panel.byCss(".main-title");
    }

    public List<Panel> instances() {
        return panel.allByCss(".instance-monitors .switch");
    }

    public TryPage TryIt() {
        return remotes.switchTo(new Target<TryPage>() {
            @Override
            public TryPage create() {
                return new TryPage(panel.byCss(format(".tab-content[target='%s']", "Try It!")));
            }

            @Override
            public void navigateTo() {
                panel.byText("Try It!").click();
            }
        });
    }

    public TryPage WorkBench(String name) {
        return remotes.switchTo(new Target<TryPage>() {
            @Override
            public TryPage create() {
                return new TryPage(panel.byCss(format(".tab-content[target='%s']", name)));
            }

            @Override
            public void navigateTo() {
                panel.byCss(".work-bench-headers").byText(name).click();
            }
        });
    }
}
