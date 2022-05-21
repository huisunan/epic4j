package com.hsn.epic4j.core;

import com.hsn.epic4j.core.util.ScreenShootUtil;
import com.ruiyun.jvppeteer.core.browser.Browser;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class WatchDogThread extends Thread {
    private final Browser browser;

    private final Thread workThread;

    private final Boolean screenShoot;

    public WatchDogThread(Browser browser, Thread workThread, Boolean screenShoot) {
        this.browser = browser;
        this.workThread = workThread;
        this.screenShoot = screenShoot;
    }

    @Override
    public void run() {
        while (true) {
            if (screenShoot) {
                ScreenShootUtil.screen(browser, "data/status", "status.jpeg");
            }
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
