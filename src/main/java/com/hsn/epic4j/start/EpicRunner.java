package com.hsn.epic4j.start;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hsn.epic4j.bean.Item;
import com.hsn.epic4j.config.EpicConfig;
import com.hsn.epic4j.notify.INotify;
import com.hsn.epic4j.util.PageUtil;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.options.ScreenshotOptions;
import com.ruiyun.jvppeteer.protocol.network.CookieParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.FileUrlResource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

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

    CountDownLatch lock = new CountDownLatch(1);

    ThreadPoolTaskScheduler scheduler;

    @Override
    public void run(ApplicationArguments args) throws InterruptedException {
        doStart();
        initCron();
        lock.await();
    }

    @PreDestroy
    public void free() {
        lock.countDown();
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }

    public void initCron() {
        String cronExpression = epicConfig.getCron();
        if (StrUtil.isBlank(cronExpression)) {
            DateTime now = DateUtil.date();
            cronExpression = StrUtil.format("{} {} {} * * ?", now.second(), now.minute(), now.hour(true));
        }
        log.info("use cron:{}", cronExpression);
        scheduler = new ThreadPoolTaskScheduler();
        scheduler.initialize();
        String finalCronExpression = cronExpression;
        scheduler.schedule(this::doStart, context -> new CronTrigger(finalCronExpression).nextExecutionTime(context));
    }

    public void doStart() {
        Browser browser = null;
        try {
            log.info("start work");
            //获取浏览器对象
            browser = iStart.getBrowser();
            //获取默认page
            Page page = iStart.getDefaultPage(browser);
            //加载cookie
            if (StrUtil.isNotBlank(epicConfig.getCookiePath()) && FileUtil.exist(epicConfig.getCookiePath())) {
                log.debug("load cookie");
                List<CookieParam> cookies = JSONUtil.toBean(IoUtil.read(new FileReader(epicConfig.getCookiePath())),
                        new TypeReference<List<CookieParam>>() {
                        }, false);
                page.setCookie(cookies);
            }
            //反爬虫设置
            PageUtil.crawSet(page);
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
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                Optional.ofNullable(browser)
                        .map(Browser::pages)
                        .filter(CollUtil::isNotEmpty)
                        .map(pages -> pages.get(0))
                        .ifPresent(page -> {
                            try {
                                FileUrlResource errorDir = new FileUrlResource("error");
                                log.debug("create error dir {}", errorDir.getFile().mkdirs());
                                ScreenshotOptions options = new ScreenshotOptions();
                                options.setQuality(100);
                                options.setPath(errorDir.getFile().getAbsolutePath() + File.separator + System.currentTimeMillis() + ".jpg");
                                options.setType("jpeg");
                                page.screenshot(options);
                            } catch (IOException ioException) {
                                log.error("截图失败");
                            }
                        });
            }
            log.error("程序异常", e);
        } finally {
            if (browser != null) {
                browser.close();
            }
        }
    }
}
