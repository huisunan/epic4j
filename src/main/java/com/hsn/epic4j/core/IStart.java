package com.hsn.epic4j.core;

import com.hsn.epic4j.core.bean.Item;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.page.Page;

import java.util.List;

public interface IStart {
    /**
     * 获取浏览器
     */
    Browser getBrowser(String dataPath);

    /**
     * 获取默认页面
     */
    Page getDefaultPage(Browser browser);

    /**
     * 判断是否要登录
     */
    boolean needLogin(Page page);

    /**
     * 领取游戏
     */
    List<Item> receive(Page page, List<Item> weekFreeItems);

    /**
     * 获取免费游戏
     */
    List<Item> getFreeItems();

    /**
     * 跳转到epic
     */
    void goToEpic(Page page);
}
