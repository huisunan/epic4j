package com.hsn.epic4j.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author hsn
 * 2021/12/27
 * EpicConig
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "epic")
public class EpicConfig {
    /**
     * webDriver的用户数据
     */
    private String dataPath = "./data";
    /**
     * webDriver启动参数
     */
    private List<String> driverArgs;
    /**
     * epic url
     */
    private String epicUrl;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 密码
     */
    private String password;
    /**
     * 无头模式
     */
    private Boolean headLess = true;
    /**
     * 浏览器版本
     */
    private String browserVersion;
    /**
     * 是否登录判断url
     */
    private String checkLoginUrl = "https://www.epicgames.com/account/v2/ajaxCheckLogin";
    /**
     * 获取用户信息url
     */
    private String userInfoUrl = "https://www.epicgames.com/account/v2/personal/ajaxGet?sessionInvalidated=true";
    /**
     * 免费游戏url
     */
    private String freeGameUrl = "https://store-site-backend-static.ak.epicgames.com/freeGamesPromotions?locale={}&country={}&allowCountries={}";
    /**
     * 商店项url
     */
    private String storeUrl = "https://www.epicgames.com/store/en-US/p/{}";
    /**
     * crontab 表达式
     */
    private String cron;
    /**
     * 非沙盒运行
     */
    private Boolean noSandbox = true;
    /**
     * cookiePath
     */
    private String cookiePath;
    /**
     * 版本
     */
    private String version;
}
