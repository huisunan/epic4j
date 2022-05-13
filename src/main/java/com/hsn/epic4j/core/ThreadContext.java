package com.hsn.epic4j.core;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ThreadContext {
    private final ThreadLocal<Integer> timeout = new ThreadLocal<>();
    private final ThreadLocal<Integer> interval = new ThreadLocal<>();

    public Integer getTimeout() {
        return timeout.get();
    }

    public Integer getInterval() {
        return interval.get();
    }

    public void init(Integer t,Integer i){
        timeout.set(t);
        interval.set(i);
    }

    public void clear(){
        timeout.remove();
        interval.remove();
    }
}
