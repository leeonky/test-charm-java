package com.github.leeonky.pf;

import com.github.leeonky.dal.DAL;

public class PageFlow {
    private static DAL dal;

    static DAL dal() {
        if (dal == null)
            dal = DAL.dal("PageFlow");
        return dal;
    }

    public static void setDal(DAL dal) {
        PageFlow.dal = dal;
    }
}
