package com.hsn.epic4j.core.util;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.hsn.epic4j.core.bean.SelectItem;
import com.hsn.epic4j.core.exception.TimeException;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.page.ElementHandle;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.core.page.Response;
import com.ruiyun.jvppeteer.protocol.PageEvaluateType;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@UtilityClass
public class PageUtil {

    //默认时间间隔 100ms
    private static final int DEFAULT_INTERVAL = 100;

    //默认超时时间 30s
    private static final int DEFAULT_TIME_OUT = 30 * 1000;

    @SneakyThrows
    public JSONObject getJson(String url, Browser browser) {
        Page page = browser.newPage();
        Response response = page.goTo(url);
        String text = response.text();
        log.trace("get {} json value : {}", url, text);
        JSONObject jsonObject = JSONUtil.parseObj(text);
        page.close();
        return jsonObject;
    }


    /**
     * 在指定范围时间内，遍历查找
     *
     * @param page        page
     * @param timeout     超时时间
     * @param ignore      true忽略异常
     * @param timeoutBack 超时后回调
     * @param selectItems 要查询的元素
     */
    @SneakyThrows
    public void findSelectors(Page page, Integer timeout, Boolean ignore, SelectItem.SelectCallBack timeoutBack, SelectItem... selectItems) {
        timer(timeout, DEFAULT_INTERVAL, i -> {
            for (SelectItem selectItem : selectItems) {
                boolean flag = false;
                try {
                    flag = selectItem.getPagePredicate().test(page, selectItem);
                } catch (Exception e) {
                    if (ignore)
                        log.debug("可以忽略到异常", e);
                    else
                        throw e;
                }
                if (flag) {
                    return selectItem.getCallback().run();
                }
            }
            return false;
        }, timeoutBack::run);


    }


    public void waitForTextChange(Page page, String selector, String text) {
        waitForTextChange(page, selector, text, 3_000, 100);
    }

    @SneakyThrows
    public void waitForTextChange(Page page, String selector, String text, Integer timeout, Integer interval) {
        timer(timeout, interval, i -> {
            ElementHandle elementHandle = page.$(selector);
            log.trace("wait {} text change count {}", selector, i);
            if (elementHandle != null) {
                String textContent = getElementStrProperty(elementHandle, "textContent");
                return !text.equals(textContent);
            }
            return false;
        }, () -> {
            throw new TimeException("wait text change timeout :" + text);
        });
    }


    public String getElementStrProperty(ElementHandle elementHandle, String property) {
        return (String) elementHandle.getProperty(property).jsonValue();
    }

    public String getTextContent(Page page, String selector) {
        return getStrProperty(page, selector, "textContent");
    }

    @SneakyThrows
    public String getStrProperty(Page page, String selector, String property) {
        AtomicReference<String> res = new AtomicReference<>();
        elementHandle(page, selector, 30, 1, e -> {
            res.set(getElementStrProperty(e, property));
        });
        return res.get();
    }

    public void click(Page page, String selector) {
        elementHandle(page, selector, null, null, ElementHandle::click);
    }

    public void type(Page page, String selector, String type) {
        elementHandle(page, selector, null, null, c -> c.type(type));
    }

    @SneakyThrows
    private void timer(Integer timeout, Integer interval, EFunction<Integer, Boolean> function, ERun end) {
        timeout = ObjectUtil.defaultIfNull(timeout, DEFAULT_TIME_OUT);
        interval = ObjectUtil.defaultIfNull(interval, DEFAULT_INTERVAL);
        for (int i = 0; (i * interval) < timeout; i++) {
            if (function.accept(i)) {
                return;
            }
            TimeUnit.MILLISECONDS.sleep(interval);
        }
        end.run();

    }

    @SneakyThrows
    public void elementHandle(Page page, String selector, Integer timeout, Integer interval, EConsumer<ElementHandle> consumer) {
        timer(timeout, interval, i -> {
            log.trace("[{}]wait for selector:{}", i, selector);
            ElementHandle elementHandle = page.$(selector);
            if (elementHandle != null) {
                log.trace("start consumer");
                consumer.accept(elementHandle);
                log.trace("end consumer");
                //点击后延迟2秒等待处理
                TimeUnit.SECONDS.sleep(2);
                return true;
            }
            return false;
        }, () -> {
            throw new TimeoutException("wait for selector " + selector + " time out");
        });

    }

    public void tryClick(Page page, String original, String selector) {
        tryClick(page, original, 3, 500, selector);
    }

    public void tryClick(Page page, String original, Integer retry, Integer interval, String selector) {
        tryClick(page, original, retry, interval, Collections.singletonList((p, c) -> {
            log.trace("try click {} count:{}", selector, c);
            page.click(selector);
        }));
    }

    @SneakyThrows
    public void tryClick(Page page, String original, Integer retry, Integer interval, List<EBiConsumer<Page, Integer>> consumers) {
        Set<EBiConsumer<Page, Integer>> successSet = new HashSet<>();
        for (int i = 0; i < retry; i++) {
            if (!page.mainFrame().url().equals(original)) {
                return;
            }
            for (EBiConsumer<Page, Integer> consumer : consumers) {
                try {
                    consumer.accept(page, i);
                    successSet.add(consumer);
                } catch (Exception ignored) {
                }
                TimeUnit.MILLISECONDS.sleep(interval);
            }
            if (successSet.size() >= consumers.size()) {
                return;
            }
        }
    }

    public void waitUrlChange(Page page, String originUrl) {
        timer(DEFAULT_TIME_OUT, DEFAULT_INTERVAL, i -> !originUrl.equals(page.mainFrame().url()), () -> {
            throw new TimeoutException("login url not change");
        });
    }

    public interface EConsumer<T> {
        void accept(T t) throws Exception;
    }

    public interface EBiConsumer<T, U> {
        void accept(T t, U u) throws Exception;
    }

    public interface ESupply<T> {
        T get() throws Exception;
    }

    public interface EFunction<T, R> {
        R accept(T t) throws Exception;
    }

    public interface ERun {
        void run() throws Exception;
    }

    public void crawSet(Page page) {
        String userAgent = (String) page.evaluate("navigator.userAgent");
        page.evaluateOnNewDocument("() => {Object.defineProperty(navigator, 'webdriver', {get: () => false})}", PageEvaluateType.FUNCTION);
        page.evaluateOnNewDocument("() => {Object.defineProperty(navigator, 'languages', {get: () => ['en','en-US']})}", PageEvaluateType.FUNCTION);
        page.evaluateOnNewDocument("() => {Object.defineProperty(navigator, 'plugins', {get: () => [{'description': 'Portable Document Format', 'filename': 'internal-pdf-viewer', 'length': 1, 'name': 'Chrome PDF Plugin'}, {'description': '', 'filename': 'mhjfbmdgcfjbbpaeojofohoefgiehjai', 'length': 1, 'name': 'Chromium PDF Viewer'}, {'description': '', 'filename': 'internal-nacl-plugin', 'length': 2, 'name': 'Native Client'}]})}", PageEvaluateType.FUNCTION);
        page.evaluateOnNewDocument("() => {Reflect.defineProperty(navigator, 'mimeTypes', {get: () => [{type: 'application/pdf', suffixes: 'pdf', description: '', enabledPlugin: Plugin}, {type: 'application/x-google-chrome-pdf', suffixes: 'pdf', description: 'Portable Document Format', enabledPlugin: Plugin}, {type: 'application/x-nacl', suffixes: '', description: 'Native Client Executable', enabledPlugin: Plugin}, {type: 'application/x-pnacl', suffixes: '', description: 'Portable Native Client Executable', enabledPlugin: Plugin}]})}", PageEvaluateType.FUNCTION);
        page.evaluateOnNewDocument("window.chrome = {'loadTimes': {}, 'csi': {}, 'app': {'isInstalled': false, 'getDetails': {}, 'getIsInstalled': {}, 'installState': {}, 'runningState': {}, 'InstallState': {'DISABLED': 'disabled', 'INSTALLED': 'installed', 'NOT_INSTALLED': 'not_installed'}, 'RunningState': {'CANNOT_RUN': 'cannot_run', 'READY_TO_RUN': 'ready_to_run', 'RUNNING': 'running'}}, 'webstore': {'onDownloadProgress': {'addListener': {}, 'removeListener': {}, 'hasListener': {}, 'hasListeners': {}, 'dispatch': {}}, 'onInstallStageChanged': {'addListener': {}, 'removeListener': {}, 'hasListener': {}, 'hasListeners': {}, 'dispatch': {}}, 'install': {}, 'ErrorCode': {'ABORTED': 'aborted', 'BLACKLISTED': 'blacklisted', 'BLOCKED_BY_POLICY': 'blockedByPolicy', 'ICON_ERROR': 'iconError', 'INSTALL_IN_PROGRESS': 'installInProgress', 'INVALID_ID': 'invalidId', 'INVALID_MANIFEST': 'invalidManifest', 'INVALID_WEBSTORE_RESPONSE': 'invalidWebstoreResponse', 'LAUNCH_FEATURE_DISABLED': 'launchFeatureDisabled', 'LAUNCH_IN_PROGRESS': 'launchInProgress', 'LAUNCH_UNSUPPORTED_EXTENSION_TYPE': 'launchUnsupportedExtensionType', 'MISSING_DEPENDENCIES': 'missingDependencies', 'NOT_PERMITTED': 'notPermitted', 'OTHER_ERROR': 'otherError', 'REQUIREMENT_VIOLATIONS': 'requirementViolations', 'USER_CANCELED': 'userCanceled', 'WEBSTORE_REQUEST_ERROR': 'webstoreRequestError'}, 'InstallStage': {'DOWNLOADING': 'downloading', 'INSTALLING': 'installing'}}}", PageEvaluateType.FUNCTION);
        page.evaluateOnNewDocument("() => {Reflect.defineProperty(navigator.connection,'rtt', {get: () => 200, enumerable: true})}", PageEvaluateType.FUNCTION);
        page.evaluateOnNewDocument("() => {const newProto = navigator.__proto__; delete newProto.webdriver; navigator.__proto__ = newProto}", PageEvaluateType.FUNCTION);
        page.evaluateOnNewDocument("const getParameter = WebGLRenderingContext.getParameter; WebGLRenderingContext.prototype.getParameter = function(parameter) {if (parameter === 37445) {return 'Intel Open Source Technology Center';}; if (parameter === 37446) {return 'Mesa DRI Intel(R) Ivybridge Mobile ';}; return getParameter(parameter);}", PageEvaluateType.FUNCTION);
        page.evaluateOnNewDocument("() => {const p = {'defaultRequest': null, 'receiver': null}; Reflect.defineProperty(navigator, 'presentation', {get: () => p})}", PageEvaluateType.FUNCTION);
        page.setExtraHTTPHeaders(Collections.singletonMap("Accept-Language", "en-GB,en-US;q=0.9,en;q=0.8"));
        page.setUserAgent(userAgent.replace("Headless", ""));
    }
}
