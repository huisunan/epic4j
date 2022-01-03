package com.hsn.epic4j;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy
@SpringBootApplication
public class Epic4jApplication {

    public static void main(String[] args) {
        SpringApplication.run(Epic4jApplication.class, args);
    }

}
