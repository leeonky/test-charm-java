package com.github.leeonky.dal.extensions.basic.sync;

public class Await {
    private static int defaultWaitingTime = 5000;

    public static void setDefaultWaitingTime(int ms) {
        Await.defaultWaitingTime = ms;
    }
}
