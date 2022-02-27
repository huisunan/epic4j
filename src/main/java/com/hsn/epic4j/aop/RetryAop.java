package com.hsn.epic4j.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@Slf4j
public class RetryAop {
    @Pointcut("@annotation(Retry)")
    public void retryEntry() {
    }

    @Around("retryEntry()")
    public Object retryAround(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();

        Method method = joinPoint.getTarget().getClass().getMethod(methodSignature.getName(), methodSignature.getParameterTypes());
        Retry annotation = method.getAnnotation(Retry.class);
        int retryCount = annotation.value();
        for (int i = 0; i < retryCount; i++) {
            try {
                return joinPoint.proceed();
            } catch (Throwable throwable) {
                if (i == (retryCount - 1)) {
                    log.error(annotation.message(), throwable);
                    throw throwable;
                }
                log.debug("message:{}, {} retry count :{}", annotation.message(), method.getName(), i);
            }
        }
        return null;
    }
}
