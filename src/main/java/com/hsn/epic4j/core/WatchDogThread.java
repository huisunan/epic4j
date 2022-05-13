package com.hsn.epic4j.core;

import com.hsn.epic4j.core.util.ScreenShootUtil;
import com.ruiyun.jvppeteer.core.browser.Browser;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class WatchDogThread extends Thread {
    private final Browser browser;


    @Override
    public void run() {
        while (true) {
            ScreenShootUtil.screen(browser, "data/status", "status.jpeg");
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
