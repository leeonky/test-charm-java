package com.github.leeonky.dal.extensions.inspector.cucumber.pagebk;

import com.github.leeonky.dal.extensions.inspector.cucumber.ui.Pages;
import com.github.leeonky.dal.extensions.inspector.cucumber.ui.Target;
import lombok.Getter;

import java.util.Objects;

import static java.lang.String.format;

public class WorkspacePageBk {
    private final Panel panel;
    @Getter
    private final Panel header;

    //TODO tab control PageContainer
    private final Pages<OutputPageBk> outputs = new Pages<OutputPageBk>() {
        @Override
        public OutputPageBk getCurrent() {
            try {
                return new OutputPageBk(
                        panel.allByCss(".tab-content.active").get(0),
                        panel.allByCss(".tab-header.active").get(0));
            } catch (Exception ignore) {
                return null;
            }
        }
    };

    public WorkspacePageBk(Panel panel, Panel header) {
        this.panel = panel;
        this.header = header;
    }

    public InputFieldBk DAL() {
        return new InputFieldBk(panel.byPlaceholder("DAL expression"));
    }

    public OutputPageBk Root() {
        return switchTo("Root");
    }

    public OutputPageBk Error() {
        return switchTo("Error");
    }

    public OutputPageBk Result() {
        return switchTo("Result");
    }

    public OutputPageBk Inspect() {
        return switchTo("Inspect");
    }

    public WatchesPage Watches() {
        OutputPageBk outputPage = switchTo("Watches");
        return new WatchesPage(outputPage.getContent());
    }

    public OutputPageBk Current() {
        return outputs.getCurrent();
    }

    public boolean isTarget(String type) {
        return Objects.equals(header.text(), type);
    }

    private OutputPageBk switchTo(String type) {
        return outputs.switchTo(new Target<OutputPageBk>() {
            @Override
            public OutputPageBk create() {
                return new OutputPageBk(panel.byCss(format(".tab-content[target='%s']", type.toLowerCase())), panel.byCss(format(".tab-header[target='%s']", type.toLowerCase())));
            }

            @Override
            public void navigateTo() {
                panel.byText(type).click();
            }

            @Override
            public boolean matches(OutputPageBk current) {
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
