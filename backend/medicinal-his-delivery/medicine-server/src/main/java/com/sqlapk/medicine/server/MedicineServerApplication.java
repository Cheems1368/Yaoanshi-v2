package com.sqlapk.medicine.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 项目的启动类
 */
@SuppressWarnings("SpringComponentScan") // 忽略 IDEA 无法识别 ${medicine.info.base-package}
@SpringBootApplication(scanBasePackages = {"${medicine.info.base-package}.server", "${medicine.info.base-package}.module"})
public class MedicineServerApplication {

    public static void main(String[] args) {

        SpringApplication.run(MedicineServerApplication.class, args);
//        new SpringApplicationBuilder(MedicineServerApplication.class)
//                .applicationStartup(new BufferingApplicationStartup(20480))
//                .run(args);

    }

}
