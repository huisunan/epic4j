package com.hsn.epic4j.start;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
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
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
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
    public void run(ApplicationArguments args) {
        doStart();
        initCron();
    }

    public void initCron(){
        String cronExpression = epicConfig.getCron();
        if (StrUtil.isBlank(cronExpression)){
            DateTime now = DateUtil.date();
            cronExpression =  StrUtil.format("{} {} {} * * ?", now.second(), now.minute(), now.hour(true));
        }
        log.info("use cron:{}",cronExpression);
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.initialize();
        String finalCronExpression = cronExpression;
        scheduler.schedule(this::doStart, context->new CronTrigger(finalCronExpression).nextExecutionTime(context));
    }

    public void doStart() {
        Browser browser = null;
        try {
            log.info("start work");
            //获取浏览器对象
            browser = iStart.getBrowser();
            //获取默认page
            Page page = iStart.getDefaultPage(browser);
            //反爬虫配置
            iStart.crawlerTest(page);
            //打开epic主页
            page.goTo(epicConfig.getEpicUrl());
            boolean needLogin = iStart.needLogin(browser);
            log.debug("needLogin:{}", needLogin);
            if (needLogin) {
                iLogin.login(page);
            }
            //领取游戏
            List<Item> receive = iStart.receive(page);
            for (INotify notify : notifies) {
                if (notify.notifyReceive(receive)) {
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
