package com.sqlapk.medicine.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 项目的启动类
 */
@SpringBootApplication(scanBasePackages = {
    "com.sqlapk.medicine.server", 
    "com.sqlapk.medicine.module",
    "com.sqlapk.medicine.framework"
})
public class MedicineServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MedicineServerApplication.class, args);
    }

}
