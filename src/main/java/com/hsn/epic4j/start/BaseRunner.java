package com.hsn.epic4j.start;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.hsn.epic4j.aop.Retry;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
           if(!notCron)
               start();
        } catch (Exception ignore) {
            log.error("立即执行出错", ignore);
        }
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
        //获取周免游戏
        List<Item> weekFreeItems = getWeekFreeItems().stream().peek(i -> {
            if (StrUtil.endWith(i.getProductSlug(), "/home")) {
                i.setProductSlug(i.getProductSlug().replace("/home", ""));
            }
        }).collect(Collectors.toList());
        //处理 productSlug带/home的清空
        for (UserInfo info : userInfo) {
            doStart(info.getEmail(), info.getPassword(), weekFreeItems);
        }
    }

    public void doStart(String email, String password, List<Item> weekFreeItems) {

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
            List<Page> pages = browser.pages();
            for (Page p : pages) {
                if (!StrUtil.startWith(p.mainFrame().url(), "http")) {
                    p.close();
                }
            }
            boolean needLogin = iStart.needLogin(page);
            log.debug("needLogin:{}", needLogin);
            if (needLogin) {
                iLogin.login(page, email, password);
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

    /**
     * 获取免费游戏
     */
    @Retry(message = "获取周末游戏失败", value = 5)
    private List<Item> getWeekFreeItems() {
        //https://store-site-backend-static.ak.epicgames.com/freeGamesPromotions?locale=zh-CN-CN&country=CN&allowCountries=CN
        String userCountry = "CN";
        String locate = "zh-CN";
        String formatUrl = StrUtil.format(epicConfig.getFreeGameUrl(), locate, userCountry, userCountry);
        log.debug(formatUrl);
        String res = HttpUtil.get(formatUrl);
        log.trace("free game json:\n{}", res);
        JSONObject json = JSONUtil.parseObj(res);
        List<Item> list = new ArrayList<>();
        DateTime now = DateUtil.date();
        for (JSONObject element : json.getByPath("data.Catalog.searchStore.elements", JSONArray.class).jsonIter()) {
            if (!"ACTIVE".equals(element.getStr("status"))) {
                continue;
            }
            if (StreamSupport.stream(element.getJSONArray("categories").jsonIter().spliterator(), false)
                    .anyMatch(item -> "freegames".equals(item.getStr("path")))) {
                JSONObject promotions = element.getJSONObject("promotions");
                if (promotions == null) {
                    continue;
                }
                JSONArray promotionalOffers = promotions.getJSONArray("promotionalOffers");
                if (CollUtil.isNotEmpty(promotionalOffers)) {
                    if (StreamSupport.stream(promotionalOffers.jsonIter().spliterator(), false)
                            .flatMap(offerItem -> StreamSupport.stream(offerItem.getJSONArray("promotionalOffers").jsonIter().spliterator(), false))
                            .anyMatch(offerItem -> {
                                DateTime startDate = DateUtil.parse(offerItem.getStr("startDate")).setTimeZone(TimeZone.getDefault());
                                DateTime endDate = DateUtil.parse(offerItem.getStr("endDate")).setTimeZone(TimeZone.getDefault());
                                JSONObject discountSetting = offerItem.getJSONObject("discountSetting");
                                return DateUtil.isIn(now, startDate, endDate) && "PERCENTAGE".equals(discountSetting.getStr("discountType"))
                                        && discountSetting.getInt("discountPercentage") == 0;
                            })) {
                        list.add(element.toBean(Item.class));
                    }

                }
            }

        }
        return list;
    }
}
