package com.hsn.epic4j.start;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.hsn.epic4j.aop.Retry;
import com.hsn.epic4j.bean.Item;
import com.hsn.epic4j.bean.PageSlug;
import com.hsn.epic4j.bean.SelectItem;
import com.hsn.epic4j.config.EpicConfig;
import com.hsn.epic4j.exception.ItemException;
import com.hsn.epic4j.exception.PermissionException;
import com.hsn.epic4j.exception.TimeException;
import com.hsn.epic4j.util.PageUtil;
import com.ruiyun.jvppeteer.core.Constant;
import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.browser.BrowserFetcher;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.LaunchOptionsBuilder;
import com.ruiyun.jvppeteer.options.Viewport;
import com.ruiyun.jvppeteer.util.FileUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
    public Browser getBrowser(String dataPath) {
        log.debug("driver data path :{}", dataPath);
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
            log.debug("all free items:{}", weekFreeItems.stream().map(Item::getTitle).collect(Collectors.joining(",")));
        }
        List<Item> receiveItem = new ArrayList<>();
        for (Item item : weekFreeItems) {
            String url = getItemUrl(item);
            String itemUrl = StrUtil.format(epicConfig.getStoreUrl(), url);
            log.debug("item url:{}", itemUrl);
            page.goTo(itemUrl);
            //age limit
            PageUtil.tryClick(page, "div[data-component=PDPAgeGate] Button", itemUrl, 8, 1000);
            PageUtil.waitForTextChange(page, "div[data-component=DesktopSticky] button[data-testid=purchase-cta-button]", "Loading");
            if (isInLibrary(page)) {
                log.debug("{} had in library", item.getTitle());
                continue;
            }
            page.waitForSelector("div[data-component=WithClickTracking] button").click();
            //epic user licence check
            log.debug("user licence check");
            PageUtil.tryClick(page, "div[data-component=makePlatformUnsupportedWarningStep] button[data-component=BaseButton", itemUrl);
            PageUtil.tryClick(page, "#agree", itemUrl);
            PageUtil.tryClick(page, "div[data-component=EulaModalActions] button[data-component=BaseButton]", itemUrl);
            String purchaseUrl = PageUtil.getStrProperty(page, "#webPurchaseContainer iframe", "src");
            log.debug("purchase url :{}", purchaseUrl);
            page.goTo(purchaseUrl);
            PageUtil.tryClick(page, "#purchase-app button[class*=confirm]:not([disabled])", page.mainFrame().url(), 20, 500);
            PageUtil.tryClick(page, "#purchaseAppContainer div.payment-overlay button.payment-btn--primary", page.mainFrame().url());
            PageUtil.findSelectors(page, 10_000, true,
                    () -> {
                        throw new TimeException("time out");
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
                    new SelectItem("#talon_frame_checkout_free_prod[style*=visible]", () -> {
                        //需要验证码
                        throw new PermissionException("CAPTCHA is required for unknown reasons when claiming");
                    }),
                    new SelectItem("#purchase-app > div", (p, i) -> p.$(i.getSelectors()) == null, () -> {
                        //当订单完成刷新时，该元素不存在，是订单完成后刷新到新页面
                        page.goTo(itemUrl);
                        PageUtil.waitForTextChange(page, "div[data-component=DesktopSticky] button[data-testid=purchase-cta-button]", "Loading");
                        if (!isInLibrary(page)) {
                            throw new ItemException("An item was mistakenly considered to have been claimed");
                        }
                        receiveItem.add(item);
                        return SelectItem.SelectCallBack.END;
                    })
            );
        }
        if (receiveItem.isEmpty()) {
            log.info("all free week games in your library:{}", weekFreeItems.stream().map(Item::getTitle).collect(Collectors.joining(",")));
        }
        return receiveItem;
    }

}
