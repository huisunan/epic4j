package com.hsn.epic4j.start;

import com.hsn.epic4j.bean.Item;
import com.hsn.epic4j.config.EpicConfig;
import com.hsn.epic4j.notify.INotify;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.options.PageNavigateOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class EpicRunner implements ApplicationRunner {
    @Autowired
    IStart iStart;

    @Autowired
    ILogin iLogin;

    @Autowired
    EpicConfig epicConfig;

    @Autowired
    List<INotify> notifies;


    @Override
    public void run(ApplicationArguments args){
        Browser browser = null;
        try {
            //获取浏览器对象
            browser = iStart.getBrowser();
            //获取默认page
            Page page = iStart.getDefaultPage(browser);
            //反爬虫配置
            iStart.crawlerTest(page);
            //打开epic主页
            page.goTo(epicConfig.getEpicUrl());
            boolean needLogin = iStart.needLogin(browser);
            log.debug("needLogin:{}",needLogin);
            if (needLogin) {
                iLogin.login(page);
            }
            //领取游戏
            List<Item> receive = iStart.receive(page);
            for (INotify notify : notifies) {
                if (notify.notifyReceive(receive)){
                    break;
                }
            }
            TimeUnit.SECONDS.sleep(30);
        } catch (Exception e) {
            log.error("程序异常", e);
        } finally {
            if (browser != null) {
                browser.close();
            }
        }
    }
}
