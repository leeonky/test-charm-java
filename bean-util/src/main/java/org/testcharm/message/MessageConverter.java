package org.testcharm.message;

public interface MessageConverter {

    String serialize(Object obj);

    Object deserialize(String str);
}
