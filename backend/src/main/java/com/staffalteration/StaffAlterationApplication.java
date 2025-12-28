package com.staffalteration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.staffalteration")
public class StaffAlterationApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(StaffAlterationApplication.class, args);
    }
}
