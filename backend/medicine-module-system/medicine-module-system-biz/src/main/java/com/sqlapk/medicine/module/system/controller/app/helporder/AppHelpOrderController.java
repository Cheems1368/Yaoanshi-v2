package com.sqlapk.medicine.module.system.controller.app.helporder;

import com.sqlapk.medicine.framework.common.pojo.CommonResult;
import com.sqlapk.medicine.module.system.api.helporder.dto.HelpOrderCreateReqDTO;
import com.sqlapk.medicine.module.system.controller.app.helporder.vo.AppHelpOrderCreateReqVO;
import com.sqlapk.medicine.module.system.service.helporder.HelpOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

import java.util.Optional;

import static com.sqlapk.medicine.framework.common.pojo.CommonResult.success;

/**
 * App 端 - 用药求助工单接口（无需认证）
 */
@Tag(name = "用户 App - 用药求助")
@RestController
@RequestMapping("/help-order")
@Validated
@Slf4j
public class AppHelpOrderController {

    @Resource
    private HelpOrderService helpOrderService;

    @PostMapping("/create")
    @Operation(summary = "创建用药求助工单")
    public CommonResult<Long> createHelpOrder(@Valid @RequestBody AppHelpOrderCreateReqVO reqVO) {
        HelpOrderCreateReqDTO reqDTO = new HelpOrderCreateReqDTO();
        BeanUtils.copyProperties(reqVO, reqDTO);
        reqDTO.setToDoctor(Optional.ofNullable(reqVO.getToDoctor()).orElse(Boolean.FALSE));

        if (reqDTO.getPatientId() == null) {
            log.warn("App 工单创建缺少 patientId，标题: {}，后端将从 Token 中自动获取", reqDTO.getTitle());
        }

        Long orderId = helpOrderService.createHelpOrder(reqDTO);
        return success(orderId);
    }
}
