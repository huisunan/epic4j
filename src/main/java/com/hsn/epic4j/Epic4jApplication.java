package com.hsn.epic4j;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.io.IOException;

@EnableAspectJAutoProxy
@SpringBootApplication
public class Epic4jApplication {

    public static void main(String[] args) throws IOException {
        SpringApplication.run(Epic4jApplication.class, args);
        System.in.read();
    }

}
