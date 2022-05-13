package com.hsn.epic4j.core.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.options.ScreenshotOptions;
import lombok.Cleanup;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileUrlResource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Slf4j
@UtilityClass
public class ScreenShootUtil {
    public void screen(Browser browser, String path, String fileName, String htmlName) {
        Optional.ofNullable(browser)
                .map(Browser::pages)
                .filter(CollUtil::isNotEmpty)
                .map(pages -> pages.get(0))
                .ifPresent(page -> {
                    try {
                        FileUrlResource errorDir = new FileUrlResource(path);
                        if (errorDir.getFile().mkdirs()) {
                            log.debug("创建目录：{}", path);
                        }
                        ScreenshotOptions options = new ScreenshotOptions();
                        options.setQuality(100);
                        String absolutePath = errorDir.getFile().getAbsolutePath() + File.separator;
                        options.setPath(absolutePath + fileName);
                        options.setType("jpeg");
                        page.screenshot(options);
                        if (StrUtil.isNotBlank(htmlName)) {
                            @Cleanup FileOutputStream fileOutputStream = new FileOutputStream(absolutePath + htmlName);
                            fileOutputStream.write(page.content().getBytes(StandardCharsets.UTF_8));
                        }
                    } catch (IOException ioException) {
                        log.error("截图失败");
                    }
                });
    }

    public void screen(Browser browser, String path, String fileName) {
        screen(browser, path, fileName, null);
    }
}
