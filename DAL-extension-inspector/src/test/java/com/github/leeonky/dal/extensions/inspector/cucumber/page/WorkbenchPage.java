package com.github.leeonky.dal.extensions.inspector.cucumber.page;

import lombok.Getter;

import static java.lang.String.format;

public class WorkbenchPage {
    private final Panel panel;
    @Getter
    private final Panel header;

    //TODO tab control PageContainer
    private final PageContainer<WorkspacePage> outputs = new PageContainer<WorkspacePage>() {
        @Override
        public WorkspacePage getCurrent() {
            try {
                return new WorkspacePage(
                        panel.allByCss(".workspace-contents > .tab-content.active").get(0),
                        panel.allByCss(".workspace-headers > .tab-header.active").get(0));
            } catch (Exception ignore) {
                return null;
            }
        }
    };

    public WorkbenchPage(Panel panel, Panel header) {
        this.panel = panel;
        this.header = header;
    }

    public InputField DAL() {
        return new InputField(panel.byPlaceholder("DAL expression"));
    }

    public OutputPage Root() {
        return outputs.getCurrent().Root();
    }

    public OutputPage Error() {
        return outputs.getCurrent().Error();
    }

    public OutputPage Result() {
        return outputs.getCurrent().Result();
    }

    public OutputPage Inspect() {
        return outputs.getCurrent().Inspect();
    }

    public OutputPage Current() {
        return outputs.getCurrent().Current();
    }

    public WorkspacePage Workspace(String target) {
        if (target.equals("Current"))
            return outputs.getCurrent();
        return outputs.switchTo(new Target<WorkspacePage>() {
            @Override
            public WorkspacePage create() {
                return new WorkspacePage(panel.byCss(format(".tab-content[target='%s']", target)), panel.byCss(format(".tab-header[target='%s']", target)));
            }

            @Override
            public void navigateTo() {
                panel.byText(target).click();
            }

            @Override
            public boolean matches(WorkspacePage current) {
                return current.isTarget(target);
            }
        });
    }

    public void Release() {
        panel.byCss(".release").click();
    }

    public boolean isConnected() {
        return !header.allByCss(".session-state.connected").isEmpty();
    }

    public void Pass() {
        panel.byCss(".pass").click();
    }

    public void execute() {
        panel.byCss(".run").click();
    }

    public void newWorkspace() {
        panel.byCss(".new").click();
    }
}
