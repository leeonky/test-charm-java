package com.github.leeonky.dal.extensions.inspector.cucumber.page;

import lombok.Getter;

import java.util.Objects;

import static java.lang.String.format;

public class WorkspacePage {
    private final Panel panel;
    @Getter
    private final Panel header;

    //TODO tab control PageContainer
    private final PageContainer<OutputPage> outputs = new PageContainer<OutputPage>() {
        @Override
        public OutputPage getCurrent() {
            try {
                return new OutputPage(
                        panel.allByCss(".tab-content.active").get(0),
                        panel.allByCss(".tab-header.active").get(0));
            } catch (Exception ignore) {
                return null;
            }
        }
    };

    public WorkspacePage(Panel panel, Panel header) {
        this.panel = panel;
        this.header = header;
    }

    public InputField DAL() {
        return new InputField(panel.byPlaceholder("DAL expression"));
    }

    public OutputPage Root() {
        return switchTo("Root");
    }

    public OutputPage Error() {
        return switchTo("Error");
    }

    public OutputPage Result() {
        return switchTo("Result");
    }

    public OutputPage Inspect() {
        return switchTo("Inspect");
    }

    public OutputPage Current() {
        return outputs.getCurrent();
    }

    public boolean isTarget(String type) {
        return Objects.equals(header.text(), type);
    }

    private OutputPage switchTo(String type) {
        return outputs.switchTo(new Target<OutputPage>() {
            @Override
            public OutputPage create() {
                return new OutputPage(panel.byCss(format(".tab-content[target='%s']", type.toLowerCase())), panel.byCss(format(".tab-header[target='%s']", type.toLowerCase())));
            }

            @Override
            public void navigateTo() {
                panel.byText(type).click();
            }

            @Override
            public boolean matches(OutputPage current) {
                return current.isType(type);
            }
        });
    }

    public void newWorkspace() {
        panel.byCss(".new").click();
    }

    public void dismiss() {
        panel.byCss(".dismiss").click();
    }
}
