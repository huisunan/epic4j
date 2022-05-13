package com.hsn.epic4j.core.notify;

import com.hsn.epic4j.core.bean.Item;

import java.util.List;

public interface INotify {
    /**
     * 领取成功通知
     * @param list 成功的列表
     * @return 是否阻断 true阻断
     */
    boolean notifyReceive(List<Item> list);
}
