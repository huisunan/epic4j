package com.hsn.epic4j.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.function.Consumer;

/**
 * @author hsn
 * 2022/1/14
 * SelectItem
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SelectItem {
    private String selectors;
    private boolean exist;
    private SelectCallBack callback;

    public SelectItem(String selectors, SelectCallBack callback) {
        this(selectors, true, callback);
    }


    public interface SelectCallBack {
        boolean CONTINUE = true;
        boolean END = false;

        /**
         * @return 是否继续运行 true继续运行
         * @throws RuntimeException     RuntimeException
         * @throws InterruptedException InterruptedException
         */
        boolean run() throws RuntimeException, InterruptedException;
    }
}
