package com.sqlapk.medicine.server;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import com.sqlapk.medicine.framework.file.config.MedicineFileAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 项目的启动类
 */
@SpringBootApplication(scanBasePackages = {
    "com.sqlapk.medicine.server", 
    "com.sqlapk.medicine.module",
    "com.sqlapk.medicine.framework"
})
@MapperScan(value = "com.sqlapk.medicine.module", annotationClass = Mapper.class)
@Import(MedicineFileAutoConfiguration.class)
@Slf4j
public class MedicineAppServerApplication {

    public static void main(String[] args) {
        // #region agent log
        ApplicationContext ctx = SpringApplication.run(MedicineAppServerApplication.class, args);
        boolean hasPasswordEncoder = ctx.getBeanNamesForType(PasswordEncoder.class).length > 0;
        String[] beanNames = ctx.getBeanNamesForType(PasswordEncoder.class);
        log.info("[DEBUG] PasswordEncoder bean check [hypothesisId=A, hasPasswordEncoder={}, beanNames={}]", hasPasswordEncoder, java.util.Arrays.toString(beanNames));
        // #endregion
    }

}
