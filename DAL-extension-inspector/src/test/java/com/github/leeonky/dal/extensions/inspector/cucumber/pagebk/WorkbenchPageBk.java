package com.github.leeonky.dal.extensions.inspector.cucumber.pagebk;

import com.github.leeonky.dal.extensions.inspector.cucumber.ui.Pages;
import com.github.leeonky.dal.extensions.inspector.cucumber.ui.Target;
import lombok.Getter;

import static java.lang.String.format;

public class WorkbenchPageBk {
    private final Panel panel;
    @Getter
    private final Panel header;

    //TODO tab control PageContainer
    private final Pages<WorkspacePageBk> outputs = new Pages<WorkspacePageBk>() {
        @Override
        public WorkspacePageBk getCurrent() {
            try {
                return new WorkspacePageBk(
                        panel.allByCss(".workspace-contents > .tab-content.active").get(0),
                        panel.allByCss(".workspace-headers > .tab-header.active").get(0));
            } catch (Exception ignore) {
                return null;
            }
        }
    };

    public WorkbenchPageBk(Panel panel, Panel header) {
        this.panel = panel;
        this.header = header;
    }

    public InputFieldBk DAL() {
        return new InputFieldBk(panel.byPlaceholder("DAL expression"));
    }

    public OutputPageBk Root() {
        return outputs.getCurrent().Root();
    }

    public OutputPageBk Error() {
        return outputs.getCurrent().Error();
    }

    public OutputPageBk Result() {
        return outputs.getCurrent().Result();
    }

    public OutputPageBk Inspect() {
        return outputs.getCurrent().Inspect();
    }

    public WatchesPageBk Watches() {
        return outputs.getCurrent().Watches();
    }

    public OutputPageBk Current() {
        return outputs.getCurrent().Current();
    }

    public WorkspacePageBk Workspace(String target) {
        if (target.equals("Current"))
            return outputs.getCurrent();
        return outputs.switchTo(new Target<WorkspacePageBk>() {
            @Override
            public WorkspacePageBk create() {
                return new WorkspacePageBk(panel.byCss(format(".tab-content[target='%s']", target)), panel.byCss(format(".tab-header[target='%s']", target)));
            }

            @Override
            public void navigateTo() {
                panel.byText(target).click();
            }

            @Override
            public boolean matches(WorkspacePageBk current) {
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
