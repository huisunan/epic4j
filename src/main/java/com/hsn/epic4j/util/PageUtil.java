package com.hsn.epic4j.util;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.hsn.epic4j.bean.SelectItem;
import com.hsn.epic4j.exception.TimeException;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.page.ElementHandle;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.core.page.Response;
import com.ruiyun.jvppeteer.options.WaitForSelectorOptions;
import com.ruiyun.jvppeteer.protocol.PageEvaluateType;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import javax.security.auth.callback.Callback;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Slf4j
@UtilityClass
public class PageUtil {
    public <T> T getJsonValue(Browser browser, String url, String path, Class<T> tClass) {
        JSONObject json = getJson(url, browser);
        return json.getByPath(path, tClass);
    }

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
        WaitForSelectorOptions options = new WaitForSelectorOptions();
        options.setTimeout(timeout);
        int interval = 100;//100ms
        for (int i = 0; i < timeout; i += interval) {
            for (SelectItem selectItem : selectItems) {
                boolean flag = false;
                try {
                    flag = (page.$(selectItem.getSelectors()) != null) == selectItem.isExist();
                } catch (Exception e) {
                    if (ignore)
                        log.debug("ignore exception", e);
                    else
                        throw e;
                }
                if (flag) {
                    boolean c = selectItem.getCallback().run();
                    if (!c) {
                        return;
                    }
                }
            }
            TimeUnit.MILLISECONDS.sleep(interval);
        }
        if (timeoutBack != null) {
            timeoutBack.run();
        }
    }


    public Boolean waitForTextChange(Page page, String selector, String text) {
        return waitForTextChange(page, selector, text, 3_000, 100);
    }

    @SneakyThrows
    public Boolean waitForTextChange(Page page, String selector, String text, Integer timeout, Integer interval) {
        for (int i = 0; i < timeout; i += interval) {
            ElementHandle elementHandle = page.$(selector);
            log.trace("wait {} text change count {}", selector, i);
            if (elementHandle != null) {
                String textContent = getElementStrProperty(elementHandle, "textContent");
                if (!text.equals(textContent)) {
                    return true;
                }
            }
            TimeUnit.MILLISECONDS.sleep(interval);
        }
        throw new TimeException("wait text change timeout :" + text);

    }


    public String getElementStrProperty(ElementHandle elementHandle, String property) {
        return (String) elementHandle.getProperty(property).jsonValue();
    }

    public String getTextContent(Page page, String selector) {
        return getStrProperty(page, selector, "textContent");
    }

    @SneakyThrows
    public String getStrProperty(Page page, String selector, String property) {
        return getElementStrProperty(page.waitForSelector(selector), property);
    }

    public void click(Page page, String selector) {
        elementHandle(page, selector, null, null, ElementHandle::click);
    }

    public void type(Page page, String selector, String type) {
        elementHandle(page, selector, null, null, c -> c.type(type));
    }

    @SneakyThrows
    public void elementHandle(Page page, String selector, Integer timeout, Integer sleep, EConsumer<ElementHandle> consumer) {
        ElementHandle elementHandle = page.waitForSelector(selector, SelectorUtil.timeout(ObjectUtil.defaultIfNull(timeout, 30000)));
        consumer.accept(elementHandle);
        TimeUnit.MILLISECONDS.sleep(ObjectUtil.defaultIfNull(sleep, 2000));
    }

    public void tryClick(Page page, String selector, String original) {
        tryClick(page, selector, original, 3, 500);
    }

    @SneakyThrows
    public void tryClick(Page page, String selector, String original, Integer retry, Integer interval) {
        for (int i = 0; i < retry; i++) {
            if (page.mainFrame().url().equals(original)) {
                try {
                    log.trace("try click {} count:{}", selector, i);
                    page.click(selector);
                    return;
                } catch (Exception ignore) {
                    Thread.sleep(interval);
                }
            } else {
                return;
            }
        }
    }

    public interface EConsumer<T> {
        void accept(T t) throws Exception;
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
