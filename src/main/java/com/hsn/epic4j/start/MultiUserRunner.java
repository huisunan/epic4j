package com.hsn.epic4j.start;

import com.hsn.epic4j.bean.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author hsn
 * 2022/1/6
 * MultiUserRunner
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "epic", name = "multi-user", havingValue = "true")
public class MultiUserRunner extends BaseRunner implements ApplicationRunner {
    @Override
    protected List<UserInfo> getUserInfo() {
        return epicConfig.getUsers();
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        baseStart();
    }
}
