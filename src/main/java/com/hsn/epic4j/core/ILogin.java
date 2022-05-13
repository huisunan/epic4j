package com.hsn.epic4j.core;

import com.ruiyun.jvppeteer.core.page.Page;

public interface ILogin {
    void login(Page page, String email, String password);
}
