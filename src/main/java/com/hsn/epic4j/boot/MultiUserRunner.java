package com.hsn.epic4j.boot;

import com.hsn.epic4j.core.EpicStarter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * @author hsn
 * 2022/1/6
 * MultiUserRunner
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "epic", name = "multi-user", havingValue = "true")
public class MultiUserRunner  implements ApplicationRunner {
    @Autowired
    private SpringEpicConfig epicConfig;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        EpicStarter.withConfig(epicConfig, epicConfig.getUsers());
    }
}
