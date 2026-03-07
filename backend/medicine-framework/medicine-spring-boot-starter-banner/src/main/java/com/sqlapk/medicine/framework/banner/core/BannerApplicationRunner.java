package com.sqlapk.medicine.framework.banner.core;

import cn.hutool.core.thread.ThreadUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.util.ClassUtils;

import java.util.concurrent.TimeUnit;

/**
 * 项目启动成功后，提供文档相关的地址
 *
 * @author 芋道源码
 */
@Slf4j
public class BannerApplicationRunner implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) throws Exception {
        ThreadUtil.execute(() -> {
            ThreadUtil.sleep(1, TimeUnit.SECONDS); // 延迟 1 秒，保证输出到结尾
            log.info("\n----------------------------------------------------------\n\t" +
                            "项目启动成功！\n\t" +
                            "接口文档: \t{} \n\t" +
                            "开发文档: \t{} \n\t" +
                            "视频教程: \t{} \n\t" +
                            "源码解析: \t{} \n" +
                            "----------------------------------------------------------",
                    "https://doc.iocoder.cn/api-doc/",
                    "https://doc.iocoder.cn",
                    "https://t.zsxq.com/02Yf6M7Qn",
                    "https://t.zsxq.com/02B6ujIee");

            // 数据报表
            if (isNotPresent("com.sqlapk.medicine.module.report.framework.security.config.SecurityConfiguration")) {
                System.out.println("[报表模块 medicine-module-report - 已禁用][参考 https://doc.iocoder.cn/report/ 开启]");
            }
            // 工作流
            if (isNotPresent("com.sqlapk.medicine.framework.flowable.config.MedicineFlowableConfiguration")) {
                System.out.println("[工作流模块 medicine-module-bpm - 已禁用][参考 https://doc.iocoder.cn/bpm/ 开启]");
            }
            // 微信公众号
            if (isNotPresent("com.sqlapk.medicine.module.mp.framework.mp.config.MpConfiguration")) {
                System.out.println("[微信公众号 medicine-module-mp - 已禁用][参考 https://doc.iocoder.cn/mp/build/ 开启]");
            }
            // 商城
            if (isNotPresent("com.sqlapk.medicine.module.trade.framework.web.config.TradeWebConfiguration")) {
                System.out.println("[商城系统 medicine-module-mall - 已禁用][参考 https://doc.iocoder.cn/mall/build/ 开启]");
            }
        });
    }

    private static boolean isNotPresent(String className) {
        return !ClassUtils.isPresent(className, ClassUtils.getDefaultClassLoader());
    }

}
