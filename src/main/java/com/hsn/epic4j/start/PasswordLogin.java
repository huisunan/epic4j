package com.hsn.epic4j.start;

import cn.hutool.core.util.StrUtil;
import com.hsn.epic4j.bean.SelectItem;
import com.hsn.epic4j.config.EpicConfig;
import com.hsn.epic4j.exception.CheckException;
import com.hsn.epic4j.exception.PermissionException;
import com.hsn.epic4j.exception.TimeException;
import com.hsn.epic4j.util.PageUtil;
import com.ruiyun.jvppeteer.core.page.Page;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PasswordLogin implements ILogin {
    @Autowired
    EpicConfig epicConfig;

    @Override
    @SneakyThrows
    public void login(Page page, String email, String password) {

        if (StrUtil.isEmpty(email)) {
            throw new CheckException("账号不能为空");
        }
        if (StrUtil.isEmpty(password)) {
            throw new CheckException(email + " 密码不能为空");
        }
        log.debug("login start");
        String originUrl = page.mainFrame().url();
        PageUtil.click(page, "div.menu-icon");
        PageUtil.click(page, "div.mobile-buttons a[href='/login']");
        PageUtil.waitUrlChange(page,originUrl);
        PageUtil.click(page, "#login-with-epic");
        PageUtil.type(page, "#email", email);
        PageUtil.type(page, "#password", password);
        PageUtil.click(page, "#sign-in[tabindex='0']");
        PageUtil.findSelectors(page, 30000, true,
                () -> {
                    throw new TimeException("Check login result timeout.");
                },
                new SelectItem("#talon_frame_login_prod[style*=visible]", () -> {
                    throw new PermissionException("CAPTCHA is required for unknown reasons when logging in");
                }),
                new SelectItem("div.MuiPaper-root[role=alert] h6[class*=subtitle1]", () -> {
                    Object jsonValue = page.waitForSelector("div.MuiPaper-root[role=alert] h6[class*=subtitle1]").getProperty("textContent").jsonValue();
                    throw new PermissionException("From Epic Games: " + jsonValue);
                }),
                new SelectItem("input[name=code-input-0]", () -> {
                    throw new PermissionException("From Epic Games need code");
                }),
                new SelectItem(".signed-in", () -> {
                    log.info("login success");
                    return SelectItem.SelectCallBack.END;
                })
        );

        log.debug("login end");
    }
}
