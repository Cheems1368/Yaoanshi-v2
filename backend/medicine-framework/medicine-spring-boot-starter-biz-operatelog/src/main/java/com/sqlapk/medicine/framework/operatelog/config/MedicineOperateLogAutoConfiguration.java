package com.sqlapk.medicine.framework.operatelog.config;

import com.sqlapk.medicine.framework.operatelog.core.aop.OperateLogAspect;
import com.sqlapk.medicine.framework.operatelog.core.service.OperateLogFrameworkService;
import com.sqlapk.medicine.framework.operatelog.core.service.OperateLogFrameworkServiceImpl;
import com.sqlapk.medicine.module.system.api.logger.OperateLogApi;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class MedicineOperateLogAutoConfiguration {

    @Bean
    public OperateLogAspect operateLogAspect() {
        return new OperateLogAspect();
    }

    @Bean
    public OperateLogFrameworkService operateLogFrameworkService(OperateLogApi operateLogApi) {
        return new OperateLogFrameworkServiceImpl(operateLogApi);
    }

}
