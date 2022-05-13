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
     * 无头模式
     */
    private Boolean headLess = true;
    /**
     * 浏览器版本
     */
    private String browserVersion;

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
    /**
     * 超时时间ms
     */
    private Integer timeout = 30_000;
    /**
     * 间隔时间ms
     */
    private Integer interval = 100;
}
