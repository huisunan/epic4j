package com.hsn.epic4j.core;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hsn.epic4j.core.bean.Item;
import com.hsn.epic4j.core.bean.UserInfo;
import com.hsn.epic4j.core.config.EpicConfig;
import com.hsn.epic4j.core.notify.ConsoleNotify;
import com.hsn.epic4j.core.notify.INotify;
import com.hsn.epic4j.core.util.PageUtil;
import com.hsn.epic4j.core.util.ScreenShootUtil;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.protocol.network.CookieParam;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileUrlResource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用于一次运行一次领取游戏
 */
@Slf4j
@Data
public class EpicStarter {
    private final EpicConfig epicConfig;
    private final IStart iStart;
    private final ILogin iLogin;
    private final IUpdate update;
    private final List<UserInfo> userInfos;


    private ThreadPoolTaskScheduler scheduler;
    private List<INotify> notifies = new ArrayList<>();


    public EpicStarter(EpicConfig epicConfig, IStart iStart, ILogin iLogin, IUpdate update, List<UserInfo> userInfos) {
        this.epicConfig = epicConfig;
        //处理代理
        StartProxy startProxy = new StartProxy(iStart);
        this.iStart = (IStart) Proxy.newProxyInstance(iStart.getClass().getClassLoader(), iStart.getClass().getInterfaces(), startProxy);
        this.iLogin = iLogin;
        this.update = update;
        this.userInfos = userInfos;
        this.notifies.add(new ConsoleNotify());
        this.initCron();
    }

    public static EpicStarter withConfig(EpicConfig epicConfig, List<UserInfo> userInfos) {
        return new EpicStarter(
                epicConfig,
                new MainStart(epicConfig),
                new PasswordLogin(),
                new MavenUpdate(epicConfig.getVersion()),
                userInfos
        );
    }

    protected void checkForUpdate() {
        if (epicConfig.getAutoUpdate()) {
            update.checkForUpdate();
        }
    }

    /**
     * 初始化cron
     */
    protected void initCron() {
        String cronExpression = epicConfig.getCron();
        boolean notCron = StrUtil.isBlank(cronExpression);
        if (notCron) {
            DateTime now = DateUtil.date().offset(DateField.SECOND, 3);
            cronExpression = StrUtil.format("{} {} {} * * ?", now.second(), now.minute(), now.hour(true));
        }
        log.info("use cron:{}", cronExpression);
        scheduler = new ThreadPoolTaskScheduler();
        scheduler.initialize();
        String finalCronExpression = cronExpression;
        scheduler.schedule(this::start, context -> new CronTrigger(finalCronExpression).nextExecutionTime(context));
        //立即执行一次
        try {
            if (!notCron)
                start();
        } catch (Exception e) {
            log.error("立即执行出错", e);
        }
    }

    protected void start() {
        checkForUpdate();
        //获取周免游戏
        List<Item> weekFreeItems = iStart.getFreeItems().stream().peek(i -> {
            if (StrUtil.endWith(i.getProductSlug(), "/home")) {
                i.setProductSlug(i.getProductSlug().replace("/home", ""));
            }
        }).collect(Collectors.toList());
        //处理 productSlug带/home的清空
        for (UserInfo info : userInfos) {
            doStart(info, weekFreeItems);
        }
    }

    public void doStart(UserInfo userInfo, List<Item> weekFreeItems) {
        Browser browser = null;
        WatchDogThread watchDogThread = null;
        try {
            log.info("账号[{}]开始工作", userInfo.getEmail());
            //用户文件路径
            String userDataPath = new FileUrlResource(epicConfig.getDataPath() + File.separator + userInfo.getEmail()).getFile().getAbsolutePath();
            //获取浏览器对象
            browser = iStart.getBrowser(userDataPath);
            //获取默认page
            Page page = iStart.getDefaultPage(browser);
            //加载cookie
            if (StrUtil.isNotBlank(epicConfig.getCookiePath()) && FileUtil.exist(epicConfig.getCookiePath())) {
                log.debug("加载cookie");
                List<CookieParam> cookies = JSONUtil.toBean(IoUtil.read(new FileReader(epicConfig.getCookiePath())),
                        new TypeReference<List<CookieParam>>() {
                        }, false);
                page.setCookie(cookies);
            }
            //反爬虫设置
            PageUtil.crawSet(page);
            if(epicConfig.getHeadLess()){
                watchDogThread = new WatchDogThread(browser);
                watchDogThread.start();
            }
            //打开epic主页
            page.goTo(epicConfig.getEpicUrl());
            List<Page> pages = browser.pages();
            for (Page p : pages) {
                if (!StrUtil.startWith(p.mainFrame().url(), "http")) {
                    p.close();
                }
            }
            boolean needLogin = iStart.needLogin(page);
            log.debug("是否需要登录:{}", needLogin ? "是" : "否");
            if (needLogin) {
                iLogin.login(page, userInfo.getEmail(), userInfo.getPassword());
            }
            //领取游戏
            List<Item> receive = iStart.receive(page, weekFreeItems);
            for (INotify notify : notifies) {
                if (notify.notifyReceive(receive)) {
                    break;
                }
            }
        } catch (Exception e) {
            if (epicConfig.getErrorScreenShoot()) {
                ScreenShootUtil.screen(browser, "data/error", System.currentTimeMillis() + ".jpeg");
            }
            log.error("程序异常", e);
        } finally {
            if (watchDogThread != null){
                watchDogThread.interrupt();
            }
            if (browser != null) {
                browser.close();
            }
        }
    }

}
