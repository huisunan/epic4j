package com.hsn.epic4j.start;

import com.hsn.epic4j.config.EpicConfig;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.FileUrlResource;
import org.springframework.stereotype.Component;

import java.io.IOException;

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

    WebDriver webDriver = null;


    @Override
    public void run(ApplicationArguments args) {
        try {
            this.initDriver();
        } catch (Exception e) {
            log.error("程序异常", e);
        } finally {
            if (webDriver != null) {
                log.debug("close driver");
                webDriver.quit();
            }
        }
    }

    /**
     * 初始化浏览器，并打开epic
     * @throws IOException
     */
    private void initDriver() throws IOException {
        String driverPath = new FileUrlResource(epicConfig.getDriverPath()).getFile().getAbsolutePath();
        log.debug("chrome driver driverPath:{}", driverPath);
        System.setProperty("webdriver.chrome.driver", driverPath);
        String dataPath = new FileUrlResource(epicConfig.getDriverDataPath()).getFile().getAbsolutePath();
        log.debug("driver data path :{}", dataPath);
        ChromeOptions options = new ChromeOptions();
        options.addArguments(epicConfig.getDriverArgs());
        options.addArguments("user-data-dir=" + dataPath);
        webDriver = new ChromeDriver(options);
        webDriver.get(epicConfig.getEpicUrl());
        log.debug("open url:{}", epicConfig.getEpicUrl());
    }
}
