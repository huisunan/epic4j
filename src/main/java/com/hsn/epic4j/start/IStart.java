package com.hsn.epic4j.start;

import com.hsn.epic4j.aop.Retry;
import com.hsn.epic4j.bean.Item;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.page.Page;
import lombok.SneakyThrows;

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


    @SneakyThrows
    boolean needLogin(Browser browser);

    /**
     * 领取游戏
     */
    List<Item> receive(Page page, List<Item> weekFreeItems);

    @Retry(message = "获取周末游戏失败")
    List<Item> getWeekFreeItems(Page page);
}
