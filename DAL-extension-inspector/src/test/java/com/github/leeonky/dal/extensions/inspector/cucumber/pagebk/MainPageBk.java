package com.github.leeonky.dal.extensions.inspector.cucumber.pagebk;

import com.github.leeonky.dal.extensions.inspector.cucumber.ui.By;
import com.github.leeonky.dal.extensions.inspector.cucumber.ui.Pages;
import com.github.leeonky.dal.extensions.inspector.cucumber.ui.Target;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.github.leeonky.util.function.Extension.notAllowParallelReduce;
import static java.lang.String.format;

@Deprecated
public class MainPageBk {
    private final Panel panel;

    //TODO tab control PageContainer
//            TODO support dynamic tabs
    private final Pages<WorkbenchPageBk> remotes = new Pages<WorkbenchPageBk>() {
        @Override
        public WorkbenchPageBk getCurrent() {
            return new WorkbenchPageBk(panel.byCss(".work-bench-contents > .tab-content.active"),
                    panel.byCss(".work-bench-headers > .tab-header.active"));
        }
    };

    public MainPageBk(SeleniumWebDriver driver) {
        driver.open("http://host.docker.internal:10082");
        panel = new Panel(driver.findAll(new By("css", "body")).get(0));
    }

    public Panel title() {
        return panel.byCss(".main-title");
    }

    public Map<String, InputFieldBk> Monitors() {
        return panel.allByCss(".instance-monitors .switch").stream().collect(Collectors.toMap(
                Panel::text, InputFieldBk::new, notAllowParallelReduce(), LinkedHashMap::new));
    }

    public InputFieldBk AutoExecute() {
        return new InputFieldBk(panel.byCss(".auto-execute.switch"));
    }

    public WorkbenchPageBk WorkBench(String name) {
        if ("Current".contains(name))
            return remotes.getCurrent();

        return remotes.switchTo(new Target<WorkbenchPageBk>() {
            @Override
            public WorkbenchPageBk create() {
                return new WorkbenchPageBk(panel.byCss(format(".tab-content[target='%s']", name)), panel.byCss(format(".tab-header[target='%s']", name)));
            }

            @Override
            public void navigateTo() {
                panel.byCss(".work-bench-headers").byText(name).click();
            }
        });
    }

    public void Release(String workbenchName) {
        WorkBench(workbenchName).Release();
    }

    public void Pass(String workbenchName) {
        WorkBench(workbenchName).Pass();
    }

    public void ReleaseAll() {
        panel.byCss(".release.release-all").click();
    }
}
