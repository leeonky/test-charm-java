package org.testcharm.message;

public class JsonMessageConverter implements MessageConverter {
    @Override
    public String serialize(Object obj) {
        return "";
    }

    @Override
    public Object deserialize(String str) {
        return null;
    }

    @Override
    public <T> T deserialize(String str, Class<T> type) {
        return null;
    }
}
