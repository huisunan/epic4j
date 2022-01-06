package com.hsn.epic4j.start;

import com.hsn.epic4j.bean.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "epic", name = "multi-user", havingValue = "false")
public class SingleUserRunner extends BaseRunner implements ApplicationRunner {
    @Override
    protected List<UserInfo> getUserInfo() {
        return Collections.singletonList(new UserInfo(epicConfig.getEmail(), epicConfig.getPassword()));
    }

    @Override
    public void run(ApplicationArguments args) throws InterruptedException {
        baseStart();
    }
}
