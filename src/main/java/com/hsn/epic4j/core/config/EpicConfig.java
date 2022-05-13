package com.hsn.epic4j.core.config;

import com.hsn.epic4j.core.bean.UserInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author hsn
 * 2021/12/27
 * EpicConig
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class EpicConfig extends UserInfo {
    /**
     * webDriver的用户数据
     */
    private String dataPath = "./data/chrome";
    /**
     * webDriver启动参数
     */
    private List<String> driverArgs;
    /**
     * epic url
     */
    private String epicUrl;
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
    private String storeUrl = "https://store.epicgames.com/en-US/p/{}";
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
    /**
     * git tag url
     */
    private String gitTagUrl = "https://github.com/huisunan/epic4j/tags";
    /**
     * 更新类型
     */
    private String updateType;
    /**
     * 自动更新
     */
    private Boolean autoUpdate;
    /**
     * 多用户模式
     */
    private Boolean multiUser;
    /**
     * 多用户配置
     */
    private List<UserInfo> users;
    /**
     * 错误输出截图
     */
    private Boolean errorScreenShoot = true;
}
