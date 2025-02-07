package com.github.leeonky.dal.extensions.inspector;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.util.Suppressor;

import static com.github.leeonky.dal.Assertions.expect;

public class InspectorContext {
    private boolean running = true;
    private DAL dal;
    private Object input;
    private String code;

    public void inspect(DAL dal, Object input, String code) {
        this.dal = dal;
        this.input = input;
        this.code = code;
        while (running)
            Suppressor.run(() -> Thread.sleep(100));
    }

    public ApiProvider apiProvider() {
        return new ApiProvider();
    }

    public class ApiProvider {
        public String fetchCode() {
            return code;
        }

        public String execute(String code) {
            try {
                expect(input).use(dal).should(code);
                return "";
            } catch (Throwable e) {
                return e.getMessage();
            }
        }
    }
}
