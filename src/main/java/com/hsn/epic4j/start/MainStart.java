package com.hsn.epic4j.start;

import cn.hutool.Hutool;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.hsn.epic4j.config.EpicConfig;
import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.browser.BrowserFetcher;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.core.page.Response;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.LaunchOptionsBuilder;
import com.ruiyun.jvppeteer.options.Viewport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.FileUrlResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 主启动类
 *
 * @author hsn
 * 2021/12/27
 * EpicStart
 */
@Slf4j
@Component
public class MainStart implements ApplicationRunner {
    @Autowired
    EpicConfig epicConfig;
    Browser browser = null;

    @Override
    public void run(ApplicationArguments args) {
        try {
            this.openBrowser();
        } catch (Exception e) {
            log.error("程序异常", e);
        } finally {
            if (browser != null) {
                browser.close();
            }
        }
    }

    /**
     * 初始化浏览器，并打开epic
     */
    private void openBrowser() throws IOException, ExecutionException, InterruptedException {

        String dataPath = new FileUrlResource(epicConfig.getDataPath()).getFile().getAbsolutePath();
        log.debug("driver data path :{}", dataPath);
        //自动下载，第一次下载后不会再下载
        BrowserFetcher.downloadIfNotExist(epicConfig.getBrowserVersion());
        LaunchOptions options = new LaunchOptionsBuilder()
                .withArgs(epicConfig.getDriverArgs())
                .withHeadless(epicConfig.getHeadLess())
                .withUserDataDir(dataPath)
                .build();

        browser = Puppeteer.launch(options);
        List<Page> pages = browser.pages();
        Page page = CollUtil.isNotEmpty(pages) ? pages.get(0) : browser.newPage();
        Viewport viewport = new Viewport();
        viewport.setWidth(600);
        viewport.setHeight(1000);
        page.setViewport(viewport);
        //无头处理
        if (epicConfig.getHeadLess()) {
            handleHeadless(page);
        }
        //TODO 加载cookie

        //判断是否要登录
        if (needLogin()){
            login(page);
        }

    }

    /**
     * 无头处理
     */
    private void handleHeadless(Page page) {
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

    /**
     * 获取登录状态
     */
    private void login(Page page) throws InterruptedException, ExecutionException {
        log.debug("start login");
        if (StrUtil.isEmpty(epicConfig.getEmail())){
            log.error("账号不能为空");
            return;
        }
        if (StrUtil.isEmpty(epicConfig.getPassword())){
            log.error("密码不能为空");
            return;
        }
        page.goTo(epicConfig.getEpicUrl());

        page.waitForSelector("div.menu-icon").click();
        page.waitForSelector("div.mobile-buttons a[href='/login']").click();
        page.waitForSelector("#login-with-epic").click();
        page.waitForSelector("#email").type(epicConfig.getEmail());
        page.waitForSelector("#password").type(epicConfig.getPassword());
        page.waitForSelector("#rememberMe").click();
        page.waitForSelector("#sign-in[tabindex='0']").click();
    }

    private boolean needLogin() throws InterruptedException {
        Page page = browser.newPage();
        Response response = page.goTo(epicConfig.getCheckLoginUrl());
        JSON json = JSONUtil.parse(response.text());
        Boolean needLogin = json.getByPath("needLogin", Boolean.class);
        //使用page.close()会出现超时情况
        page.close(true);
        log.debug("close need login page");
        return needLogin;
    }

}
