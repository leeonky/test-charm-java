package org.testcharm.message;

public interface MessageConverter {
    String serialize(Object obj);

    Object deserialize(String str);

    <T> T deserialize(String str, Class<T> type);
}
