package com.hsn.epic4j.core;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.hsn.epic4j.core.bean.Item;
import com.hsn.epic4j.core.bean.PageSlug;
import com.hsn.epic4j.core.bean.SelectItem;
import com.hsn.epic4j.core.config.EpicConfig;
import com.hsn.epic4j.core.exception.ItemException;
import com.hsn.epic4j.core.exception.PermissionException;
import com.hsn.epic4j.core.exception.TimeException;
import com.hsn.epic4j.core.util.PageUtil;
import com.ruiyun.jvppeteer.core.Constant;
import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.browser.BrowserFetcher;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.LaunchOptionsBuilder;
import com.ruiyun.jvppeteer.options.Viewport;
import com.ruiyun.jvppeteer.util.FileUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
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
@RequiredArgsConstructor
public class MainStart implements IStart {
    private final EpicConfig epicConfig;


    @Override
    @SneakyThrows
    public Browser getBrowser(String dataPath) {
        log.debug("chrome用户数据路径 :{}", dataPath);
        if (epicConfig.getNoSandbox()) {
            epicConfig.getDriverArgs().add("--no-sandbox");
        }
        //自动下载，第一次下载后不会再下载
        if (Arrays.stream(Constant.EXECUTABLE_ENV).noneMatch(env -> {
            String chromeExecutable = System.getenv(env);
            return StrUtil.isNotBlank(chromeExecutable) && FileUtil.assertExecutable(chromeExecutable);
        })) {
            BrowserFetcher.downloadIfNotExist(epicConfig.getBrowserVersion());
        }

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
        viewport.setHasTouch(true);
        page.setViewport(viewport);
        return page;
    }


    @SneakyThrows
    @Override
    @Retry(message = "登录检查失败")
    public boolean needLogin(Page page) {
        return page.$("div.mobile-buttons a[href='/login']") != null;
//        return PageUtil.getJsonValue(browser, epicConfig.getCheckLoginUrl(), "needLogin", Boolean.class);
    }


    private Boolean isInLibrary(Page page) {
        String textContent = PageUtil.getTextContent(page, "div[data-component=DesktopSticky] button[data-testid=purchase-cta-button]");
        return "In Library".equals(textContent);
    }


    private String getItemUrl(Item item) {
        String url = null;
        if (Item.BASE_GAME.equals(item.getOfferType())) {
            url = item.getProductSlug();
        } else if (Item.DLC.equals(item.getOfferType())) {
            url = item.getUrlSlug();
        }
        if (url != null) {
            return url;
        }
        //url为空尝试加载 offerMappings
        if (CollUtil.isNotEmpty(item.getOfferMappings())) {
            if ((url = findMappingUrl(item.getOfferMappings())) != null) {
                return url;
            }
        }
        //url为空尝试加载 catalogNs
        if (item.getCatalogNs() != null && CollUtil.isNotEmpty(item.getCatalogNs().getMappings())) {
            if ((url = findMappingUrl(item.getCatalogNs().getMappings())) != null) {
                return url;
            }
        }
        return null;
    }

    private String findMappingUrl(List<PageSlug> pageSlugs) {
        return pageSlugs.stream().filter(i -> "productHome".equals(i.getPageType()))
                .findFirst().map(PageSlug::getPageSlug).orElse(null);
    }

    @Override
    @SneakyThrows
    @Retry(message = "领取失败")
    public List<Item> receive(Page page, List<Item> weekFreeItems) {
        if (log.isDebugEnabled()) {
            log.debug("所有免费的游戏:{}", weekFreeItems.stream().map(Item::getTitle).collect(Collectors.joining(",")));
        }
        List<Item> receiveItem = new ArrayList<>();
        for (Item item : weekFreeItems) {
            String url = getItemUrl(item);
            String itemUrl = StrUtil.format(epicConfig.getStoreUrl(), url);
            log.debug("游戏url:{}", itemUrl);
            page.goTo(itemUrl);
            //age limit
            PageUtil.tryClick(page, "div[data-component=PDPAgeGate] Button", itemUrl, 8, 1000);
            PageUtil.waitForTextChange(page, "div[data-component=DesktopSticky] button[data-testid=purchase-cta-button]", "Loading");
            if (isInLibrary(page)) {
                log.debug("游戏[{}]已经在库里", item.getTitle());
                continue;
            }
            page.waitForSelector("div[data-component=DesktopSticky] button[data-testid=purchase-cta-button]").click();
//            page.waitForSelector("div[data-component=WithClickTracking] button").click();
            //epic user licence check
            log.debug("协议检测开始");
            PageUtil.tryClick(page, "div[data-component=makePlatformUnsupportedWarningStep] button[data-component=BaseButton", itemUrl);
            PageUtil.tryClick(page, "#agree", itemUrl);
            PageUtil.tryClick(page, "div[data-component=EulaModalActions] button[data-component=BaseButton]", itemUrl);
            String purchaseUrl = PageUtil.getStrProperty(page, "#webPurchaseContainer iframe", "src");
            log.debug("订单链接 :{}", purchaseUrl);
            page.goTo(purchaseUrl);
            PageUtil.tryClick(page, "#purchase-app button[class*=confirm]:not([disabled])", page.mainFrame().url(), 20, 500);
            PageUtil.tryClick(page, "#purchaseAppContainer div.payment-overlay button.payment-btn--primary", page.mainFrame().url());
            PageUtil.findSelectors(page, 30_000, true,
                    () -> {
                        throw new TimeException("订单状态检测超时");
                    },
                    new SelectItem("#purchase-app div[class*=alert]", () -> {
                        if (item.isDLC()) {
                            //DLC情况下,在没有本体的情况下也也可以领取
                            return SelectItem.SelectCallBack.CONTINUE;
                        } else {
                            String message = PageUtil.getStrProperty(page, "#purchase-app div[class*=alert]:not([disabled])", "textContent");
                            throw new PermissionException(message);
                        }
                    }),
                    //#talon_container_checkout_free_prod
                    //talon_frame_checkout_free_prod
                    new SelectItem("#talon_container_checkout_free_prod[style*=visible]", () -> {
                        //需要验证码
                        throw new PermissionException("检测到需要验证码");
                    }),
                    new SelectItem("#purchase-app > div", (p, i) -> p.$(i.getSelectors()) == null, () -> {
                        //当订单完成刷新时，该元素不存在，是订单完成后刷新到新页面
                        page.goTo(itemUrl);
                        PageUtil.waitForTextChange(page, "div[data-component=DesktopSticky] button[data-testid=purchase-cta-button]", "Loading");
                        if (!isInLibrary(page)) {
                            throw new ItemException("该游戏被误认为已经认领");
                        }
                        log.info("游戏领取成功:{}",item.getTitle());
                        receiveItem.add(item);
                        return SelectItem.SelectCallBack.END;
                    })
            );
        }
        return receiveItem;
    }

    /**
     * 获取免费游戏
     */
    @Override
    @Retry(message = "获取周末游戏失败", value = 10)
    public List<Item> getFreeItems() {
        //https://store-site-backend-static.ak.epicgames.com/freeGamesPromotions?locale=zh-CN-CN&country=CN&allowCountries=CN
        String userCountry = "CN";
        String locate = "zh-CN";
        String formatUrl = StrUtil.format(epicConfig.getFreeGameUrl(), locate, userCountry, userCountry);
        log.debug(formatUrl);
        String res = HttpUtil.get(formatUrl);
        log.trace("免费游戏json串:\n{}", res);
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
