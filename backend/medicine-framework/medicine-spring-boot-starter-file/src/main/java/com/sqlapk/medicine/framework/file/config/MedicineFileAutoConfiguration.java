package com.sqlapk.medicine.framework.file.config;

import com.sqlapk.medicine.framework.file.core.client.FileClientFactory;
import com.sqlapk.medicine.framework.file.core.client.FileClientFactoryImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * 文件配置类
 *
 * @author 芋道源码
 */
@AutoConfiguration
@Slf4j
public class MedicineFileAutoConfiguration {

    // #region agent log
    public MedicineFileAutoConfiguration() {
        log.info("[DEBUG] MedicineFileAutoConfiguration constructor called [hypothesisId=D, autoConfigLoaded=true]");
    }
    // #endregion

    @Bean
    public FileClientFactory fileClientFactory() {
        // #region agent log
        log.info("[DEBUG] Creating FileClientFactory bean [hypothesisId=D]");
        // #endregion
        return new FileClientFactoryImpl();
    }

}
