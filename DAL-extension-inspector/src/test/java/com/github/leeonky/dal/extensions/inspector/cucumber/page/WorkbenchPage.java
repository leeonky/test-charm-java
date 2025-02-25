package com.github.leeonky.dal.extensions.inspector.cucumber.page;

import lombok.Getter;

import static java.lang.String.format;

public class WorkbenchPage {
    private final Panel panel;
    @Getter
    private final String name;

    //TODO tab control PageContainer
    private final PageContainer<OutputPage> outputs = new PageContainer<OutputPage>() {
        @Override
        public OutputPage getCurrent() {
            return new OutputPage(panel.byCss(".tab-content.active"), panel.byCss(".tab-header.active").text());
        }
    };

    public WorkbenchPage(Panel panel, String name) {
        this.panel = panel;
        this.name = name;
    }

    public InputField DAL() {
        return new InputField(panel.byPlaceholder("DAL expression"));
    }

//    public Map<String, InputField> Execute() {
//        return panel.allByXpath("//*[@placeholder]").stream().collect(Collectors.toMap(
//                p -> p.attribute("placeholder"), InputField::new, notAllowParallelReduce(), LinkedHashMap::new));
//    }

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

    private OutputPage switchTo(String type) {
        return outputs.switchTo(new Target<OutputPage>() {
            @Override
            public OutputPage create() {
                return new OutputPage(panel.byCss(format(".tab-content[target='%s']", type.toLowerCase())), type);
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
}
