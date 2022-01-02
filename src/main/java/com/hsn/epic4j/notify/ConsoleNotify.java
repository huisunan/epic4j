package com.hsn.epic4j.notify;

import com.hsn.epic4j.bean.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class ConsoleNotify implements INotify {
    @Override
    public boolean notifyReceive(List<Item> list) {
        for (Item item : list) {
            log.info("success receive game:{}", item.getTitle());
        }
        return false;
    }
}
