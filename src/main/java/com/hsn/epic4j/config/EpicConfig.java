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
     * webDriver的路径
     */
    private String driverPath;
    /**
     * webDriver的用户数据
     */
    private String driverDataPath = "./data";
    /**
     * webDriver启动参数
     */
    private List<String> driverArgs;
    /**
     * epic url
     */
    private String epicUrl;
}
