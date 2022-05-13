package com.hsn.epic4j.boot;

import com.hsn.epic4j.core.config.EpicConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@EqualsAndHashCode(callSuper = true)
@Data
@Configuration
@ConfigurationProperties(prefix = "epic")
public class SpringEpicConfig extends EpicConfig {
}
