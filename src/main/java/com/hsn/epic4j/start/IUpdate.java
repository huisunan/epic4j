package com.hsn.epic4j.start;

/**
 * @author hsn
 * 2022/1/4
 * IUpdate
 */
public interface IUpdate {
    String CONFIG = "update-type";
    int UPDATE_EXIT_CODE = 66;

    void checkForUpdate();
}
