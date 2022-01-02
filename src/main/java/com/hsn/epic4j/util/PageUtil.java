package com.hsn.epic4j.util;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.page.ElementHandle;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.core.page.Response;
import com.ruiyun.jvppeteer.options.WaitForSelectorOptions;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

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
        JSONObject jsonObject = JSONUtil.parseObj(response.text());
        page.close();
        return jsonObject;
    }

    public Integer findSelectors(Page page, Integer timeout, String... selectors) throws InterruptedException {
        WaitForSelectorOptions options = new WaitForSelectorOptions();
        options.setTimeout(timeout);
        for (int i = 0; i < selectors.length; i++) {
            if (page.waitForSelector(selectors[i], options) != null) {
                return i;
            }
        }
        return -1;
    }

    @SneakyThrows
    public String getStrProperty(Page page, String selector, String property) {
        return (String) page.waitForSelector(selector).getProperty(property).jsonValue();
    }

    public void tryClick(Page page,String selector,String original){
        tryClick(page,selector,original,3,500);
    }

    @SneakyThrows
    public void tryClick(Page page,String selector,String original,Integer retry,Integer interval){
        for (int i = 0; i < retry; i++) {
            if(page.mainFrame().url().equals(original)){
                try {
                    page.click(selector);
                    return;
                }catch (Exception ignore){
                    Thread.sleep(interval);
                }
            }else{
                return;
            }
        }
    }
}
