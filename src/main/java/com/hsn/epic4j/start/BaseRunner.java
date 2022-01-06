package com.hsn.epic4j.start;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hsn.epic4j.bean.Item;
import com.hsn.epic4j.bean.UserInfo;
import com.hsn.epic4j.config.EpicConfig;
import com.hsn.epic4j.notify.INotify;
import com.hsn.epic4j.util.PageUtil;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.options.ScreenshotOptions;
import com.ruiyun.jvppeteer.protocol.network.CookieParam;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

/**
 * @author hsn
 * 2022/1/6
 * BaseRunner
 */
@Slf4j
@Component
public abstract class BaseRunner {
    @Autowired
    IStart iStart;

    @Autowired
    ILogin iLogin;

    @Autowired
    EpicConfig epicConfig;

    @Autowired
    List<INotify> notifies;

    @Autowired
    IUpdate update;

    ThreadPoolTaskScheduler scheduler;


    @PreDestroy
    public void free() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }

    protected void checkForUpdate() {
        if (epicConfig.getAutoUpdate()) {
            update.checkForUpdate();
        }
    }

    public void initCron() {
        String cronExpression = epicConfig.getCron();
        if (StrUtil.isBlank(cronExpression)) {
            DateTime now = DateUtil.date().offset(DateField.SECOND, 10);
            cronExpression = StrUtil.format("{} {} {} * * ?", now.second(), now.minute(), now.hour(true));
        }
        log.info("use cron:{}", cronExpression);
        scheduler = new ThreadPoolTaskScheduler();
        scheduler.initialize();
        String finalCronExpression = cronExpression;
        scheduler.schedule(this::start, context -> new CronTrigger(finalCronExpression).nextExecutionTime(context));
    }

    /**
     * 获取用户信息
     */
    protected abstract List<UserInfo> getUserInfo();

    /**
     * 默认的启动流程
     */
    @SneakyThrows
    protected void baseStart() {
        initCron();
    }

    protected void start() {
        checkForUpdate();
        List<UserInfo> userInfo = getUserInfo();
        for (UserInfo info : userInfo) {
            doStart(info.getEmail(), info.getPassword());
        }
    }

    public void doStart(String email, String password) {

        Browser browser = null;
        try {
            log.info("start {} work", email);
            //用户文件路径
            String userDataPath = new FileUrlResource(epicConfig.getDataPath() + File.separator + email).getFile().getAbsolutePath();
            //获取浏览器对象
            browser = iStart.getBrowser(userDataPath);
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
                iLogin.login(page, email, password);
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
                                FileUrlResource errorDir = new FileUrlResource("data/error");
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
