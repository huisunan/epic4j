package com.hsn.epic4j.core;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
@RequiredArgsConstructor
public class StartProxy implements InvocationHandler {

    private final IStart target;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Retry annotation = target.getClass().getDeclaredMethod(method.getName(), method.getParameterTypes()).getAnnotation(Retry.class);
        if (annotation == null) {
            return method.invoke(target, args);
        }
        int retryCount = annotation.value();
        for (int i = 0; i < retryCount; i++) {
            try {
                return method.invoke(target, args);
            } catch (Throwable throwable) {
                Throwable targetThrowable;
                if (throwable instanceof InvocationTargetException){
                    InvocationTargetException invocationTargetException  = (InvocationTargetException) throwable;
                    targetThrowable = invocationTargetException.getTargetException();
                }else {
                    targetThrowable = throwable;
                }
                if (i == (retryCount - 1)) {
                    log.error(annotation.message(), targetThrowable);
                    throw throwable;
                }
                log.error("重试异常信息:{}", targetThrowable.getMessage());
                log.debug("重试信息:{}, {} 第{}次执行", annotation.message(), method.getName(), i+1);
            }
        }
        return null;
    }
}
