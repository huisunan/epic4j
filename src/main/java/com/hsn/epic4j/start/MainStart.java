package com.hsn.epic4j.start;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.hsn.epic4j.aop.Retry;
import com.hsn.epic4j.bean.Item;
import com.hsn.epic4j.config.EpicConfig;
import com.hsn.epic4j.util.PageUtil;
import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.browser.BrowserFetcher;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.LaunchOptionsBuilder;
import com.ruiyun.jvppeteer.options.Viewport;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileUrlResource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * 主启动类
 *
 * @author hsn
 * 2021/12/27
 * EpicStart
 */
@Slf4j
@Component
public class MainStart implements IStart {
    @Autowired
    EpicConfig epicConfig;


    @Override
    @SneakyThrows
    @Retry(message = "浏览器打开异常")
    public Browser getBrowser() {
        String dataPath = new FileUrlResource(epicConfig.getDataPath()).getFile().getAbsolutePath();
        log.debug("driver data path :{}", dataPath);
        //自动下载，第一次下载后不会再下载
        BrowserFetcher.downloadIfNotExist(epicConfig.getBrowserVersion());
        LaunchOptions options = new LaunchOptionsBuilder()
                .withArgs(epicConfig.getDriverArgs())
                .withHeadless(epicConfig.getHeadLess())
                .withUserDataDir(dataPath)
                .withIgnoreDefaultArgs(Collections.singletonList("--enable-automation"))
                .build();
        return Puppeteer.launch(options);
    }

    @Override
    @Retry(message = "默认页面打开失败")
    public Page getDefaultPage(Browser browser) {
        List<Page> pages = browser.pages();
        Page page = CollUtil.isNotEmpty(pages) ? pages.get(0) : browser.newPage();
        Viewport viewport = new Viewport();
        viewport.setWidth(600);
        viewport.setHeight(1000);
        page.setViewport(viewport);
        return page;
    }

    /**
     * 反爬虫
     */
    @Override
    @Retry(message = "反爬虫设置失败")
    public void crawlerTest(Page page) {
        String userAgent = (String) page.evaluate("navigator.userAgent");
        page.evaluateOnNewDocument("() => {Object.defineProperty(navigator, 'webdriver', {get: () => false})}");
        page.evaluateOnNewDocument("window.chrome = {'loadTimes': {}, 'csi': {}, 'app': {'isInstalled': false, 'getDetails': {}, 'getIsInstalled': {}, 'installState': {}, 'runningState': {}, 'InstallState': {'DISABLED': 'disabled', 'INSTALLED': 'installed', 'NOT_INSTALLED': 'not_installed'}, 'RunningState': {'CANNOT_RUN': 'cannot_run', 'READY_TO_RUN': 'ready_to_run', 'RUNNING': 'running'}}, 'webstore': {'onDownloadProgress': {'addListener': {}, 'removeListener': {}, 'hasListener': {}, 'hasListeners': {}, 'dispatch': {}}, 'onInstallStageChanged': {'addListener': {}, 'removeListener': {}, 'hasListener': {}, 'hasListeners': {}, 'dispatch': {}}, 'install': {}, 'ErrorCode': {'ABORTED': 'aborted', 'BLACKLISTED': 'blacklisted', 'BLOCKED_BY_POLICY': 'blockedByPolicy', 'ICON_ERROR': 'iconError', 'INSTALL_IN_PROGRESS': 'installInProgress', 'INVALID_ID': 'invalidId', 'INVALID_MANIFEST': 'invalidManifest', 'INVALID_WEBSTORE_RESPONSE': 'invalidWebstoreResponse', 'LAUNCH_FEATURE_DISABLED': 'launchFeatureDisabled', 'LAUNCH_IN_PROGRESS': 'launchInProgress', 'LAUNCH_UNSUPPORTED_EXTENSION_TYPE': 'launchUnsupportedExtensionType', 'MISSING_DEPENDENCIES': 'missingDependencies', 'NOT_PERMITTED': 'notPermitted', 'OTHER_ERROR': 'otherError', 'REQUIREMENT_VIOLATIONS': 'requirementViolations', 'USER_CANCELED': 'userCanceled', 'WEBSTORE_REQUEST_ERROR': 'webstoreRequestError'}, 'InstallStage': {'DOWNLOADING': 'downloading', 'INSTALLING': 'installing'}}}");
        page.evaluateOnNewDocument("() => {Reflect.defineProperty(navigator.connection,'rtt', {get: () => 200, enumerable: true})}");
        page.evaluateOnNewDocument("() => {Object.defineProperty(navigator, 'plugins', {get: () => [{'description': 'Portable Document Format', 'filename': 'internal-pdf-viewer', 'length': 1, 'name': 'Chrome PDF Plugin'}, {'description': '', 'filename': 'mhjfbmdgcfjbbpaeojofohoefgiehjai', 'length': 1, 'name': 'Chromium PDF Viewer'}, {'description': '', 'filename': 'internal-nacl-plugin', 'length': 2, 'name': 'Native Client'}]})}");
        page.evaluateOnNewDocument("() => {const newProto = navigator.__proto__; delete newProto.webdriver; navigator.__proto__ = newProto}");
        page.evaluateOnNewDocument("const getParameter = WebGLRenderingContext.getParameter; WebGLRenderingContext.prototype.getParameter = function(parameter) {if (parameter === 37445) {return 'Intel Open Source Technology Center';}; if (parameter === 37446) {return 'Mesa DRI Intel(R) Ivybridge Mobile ';}; return getParameter(parameter);}");
        page.evaluateOnNewDocument("() => {Reflect.defineProperty(navigator, 'mimeTypes', {get: () => [{type: 'application/pdf', suffixes: 'pdf', description: '', enabledPlugin: Plugin}, {type: 'application/x-google-chrome-pdf', suffixes: 'pdf', description: 'Portable Document Format', enabledPlugin: Plugin}, {type: 'application/x-nacl', suffixes: '', description: 'Native Client Executable', enabledPlugin: Plugin}, {type: 'application/x-pnacl', suffixes: '', description: 'Portable Native Client Executable', enabledPlugin: Plugin}]})}");
        page.evaluateOnNewDocument("() => {const p = {'defaultRequest': null, 'receiver': null}; Reflect.defineProperty(navigator, 'presentation', {get: () => p})}");
        page.setExtraHTTPHeaders(Collections.singletonMap("Accept-Language", "en-GB,en-US;q=0.9,en;q=0.8"));
        page.setUserAgent(userAgent);
    }


    @SneakyThrows
    @Override
    @Retry(message = "登录检查失败")
    public boolean needLogin(Browser browser) {
        return PageUtil.getJsonValue(browser, epicConfig.getCheckLoginUrl(), "needLogin", Boolean.class);
    }


    @Override
    @SneakyThrows
    @Retry(message = "领取失败")
    public List<Item> receive(Page page) {
        List<Item> weekFreeItems = getWeekFreeItems(page);
        if (log.isDebugEnabled()) {
            log.debug("all free items:{}", weekFreeItems.stream().map(Item::getTitle).collect(Collectors.joining(",")));
        }
        List<Item> receiveItem = new ArrayList<>();
        for (Item item : weekFreeItems) {
            String itemUrl = StrUtil.format(epicConfig.getStoreUrl(), item.getProductSlug());
            log.debug("item url:{}", itemUrl);
            page.goTo(itemUrl);
            String textContent = PageUtil.getStrProperty(page, "div[data-component=DesktopSticky] button[data-testid=purchase-cta-button]", "textContent");
            if ("In Library".equals(textContent)) {
                log.debug("{} had receive", item.getTitle());
                continue;
            }
            page.waitForSelector("div[data-component=WithClickTracking] button").click();
            //epic user licence check
            log.debug("user licence check");
            PageUtil.tryClick(page,"div[data-component=makePlatformUnsupportedWarningStep] button[data-component=BaseButton",itemUrl);
            PageUtil.tryClick(page,"#agree",itemUrl);
            PageUtil.tryClick(page,"div[data-component=EulaModalActions] button[data-component=BaseButton]",itemUrl);
            String purchaseUrl = PageUtil.getStrProperty(page, "#webPurchaseContainer iframe", "src");
            log.debug("purchase url :{}",purchaseUrl);
            page.goTo(purchaseUrl);
            page.waitForSelector("#purchase-app button[class*=confirm]:not([disabled])").click();
            receiveItem.add(item);
        }
        return receiveItem;
    }

    /**
     * 获取周免游戏
     */
    @Retry(message = "获取周末游戏失败")
    private List<Item> getWeekFreeItems(Page page) {
        Browser browser = page.browser();
        String userCountry = "CN";
        String locate = "zh-CN";
        String country = PageUtil.getJsonValue(browser, epicConfig.getUserInfoUrl(), "userInfo.country.value", String.class);
        if (StrUtil.isNotEmpty(country)) {
            userCountry = country;
            locate = locate + "-" + userCountry;
        }
        String formatUrl = StrUtil.format(epicConfig.getFreeGameUrl(), locate, userCountry, userCountry);
        log.debug(formatUrl);
        JSONObject json = PageUtil.getJson(formatUrl, browser);
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
