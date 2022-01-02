package com.hsn.epic4j.util;

import com.ruiyun.jvppeteer.options.WaitForSelectorOptions;
import lombok.experimental.UtilityClass;

@UtilityClass
public class SelectorUtil {
    public WaitForSelectorOptions timeout(Integer timeout){
        WaitForSelectorOptions options = new WaitForSelectorOptions();
        options.setTimeout(timeout);
        return options;
    }
}
