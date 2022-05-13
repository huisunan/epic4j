package com.hsn.epic4j.core.util;

import cn.hutool.core.collection.CollUtil;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.options.ScreenshotOptions;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileUrlResource;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

@Slf4j
@UtilityClass
public class ScreenShootUtil {
    public void screen(Browser browser, String path, String fileName) {
        Optional.ofNullable(browser)
                .map(Browser::pages)
                .filter(CollUtil::isNotEmpty)
                .map(pages -> pages.get(0))
                .ifPresent(page -> {
                    try {
                        FileUrlResource errorDir = new FileUrlResource(path);
                        if (errorDir.getFile().mkdirs()){
                            log.debug("创建目录：{}", path);
                        }
                        ScreenshotOptions options = new ScreenshotOptions();
                        options.setQuality(100);
                        options.setPath(errorDir.getFile().getAbsolutePath() + File.separator + fileName);
                        options.setType("jpeg");
                        page.screenshot(options);
                    } catch (IOException ioException) {
                        log.error("截图失败");
                    }
                });
    }
}
