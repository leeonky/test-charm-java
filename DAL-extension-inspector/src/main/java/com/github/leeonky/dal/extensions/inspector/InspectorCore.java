package com.github.leeonky.dal.extensions.inspector;

import com.github.leeonky.dal.DAL;
import com.github.leeonky.dal.ast.node.DALNode;
import com.github.leeonky.dal.runtime.RuntimeContextBuilder;
import com.github.leeonky.util.Suppressor;

import static com.github.leeonky.dal.Accessors.get;
import static com.github.leeonky.dal.Assertions.expect;
import static java.lang.String.format;

public class InspectorCore {
    private boolean running = true;
    private DAL dal = DAL.create(InspectorExtension.class);
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
                RuntimeContextBuilder.DALRuntimeContext runtimeContext = dal.getRuntimeContextBuilder().build(input);
                DALNode node = dal.compileSingle(code, runtimeContext);
                if (node.isVerification()) {
                    expect(input).use(dal).should(code);
                } else {
                    Object result = get(code).by(dal).from(input);
                    return runtimeContext.wrap(result).dumpAll();
                }
                return "";
            } catch (Throwable e) {
                return format("%s:%s", e.getClass().getName(), e.getMessage());
            }
        }
    }
}
