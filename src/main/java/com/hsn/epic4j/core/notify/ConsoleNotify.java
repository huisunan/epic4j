package com.hsn.epic4j.core.notify;

import com.hsn.epic4j.core.bean.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ConsoleNotify implements INotify {
    @Override
    public boolean notifyReceive(List<Item> list) {
        log.info("所有领取到的游戏:{}",list.stream().map(Item::getTitle).collect(Collectors.joining(",")));
        return false;
    }
}
