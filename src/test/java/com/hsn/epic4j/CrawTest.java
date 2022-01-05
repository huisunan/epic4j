package com.hsn.epic4j;

import cn.hutool.core.io.FileUtil;
import com.hsn.epic4j.util.PageUtil;
import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.browser.BrowserFetcher;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.LaunchOptionsBuilder;
import com.ruiyun.jvppeteer.options.ScreenshotOptions;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.FileUrlResource;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

/**
 * @author hsn
 * 2022/1/5
 * CrawTest
 */
@Slf4j
public class CrawTest {

    @Test
    @SneakyThrows
    public void test() {
        BrowserFetcher.downloadIfNotExist("938248");
        String dataPath = new FileUrlResource("./data/chrome").getFile().getAbsolutePath();
        LaunchOptions options = new LaunchOptionsBuilder()
                .withArgs(Arrays.asList("--blink-settings=imagesEnabled=false", "--no-first-run", "--disable-gpu", "--no-default-browser-check", "--no-sandbox"))
                .withHeadless(true)
                .withUserDataDir(dataPath)
                .withIgnoreDefaultArgs(Collections.singletonList("--enable-automation"))
                .build();
        Browser browser = Puppeteer.launch(options);
        Page page = browser.pages().get(0);
        PageUtil.crawSet(page);
        page.goTo("http://localhost:9999/");
        FileUrlResource errorDir = new FileUrlResource("data/test");
        File file = errorDir.getFile();
        file.mkdirs();
        ScreenshotOptions screenshotOptions = new ScreenshotOptions();
        screenshotOptions.setQuality(100);
        screenshotOptions.setPath(file.getAbsolutePath() + File.separator + "report.jpg");
        screenshotOptions.setType("jpeg");
        page.screenshot(screenshotOptions);
        FileUtil.writeUtf8String(page.content(), new File(file.getAbsolutePath() + File.separator + "report.html"));
        log.info("success");
    }
}
