package com.sqlapk.medicine.module.system.controller.admin.sms;

import com.sqlapk.medicine.framework.common.pojo.CommonResult;
import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.system.controller.admin.sms.vo.channel.SmsChannelCreateReqVO;
import com.sqlapk.medicine.module.system.controller.admin.sms.vo.channel.SmsChannelPageReqVO;
import com.sqlapk.medicine.module.system.controller.admin.sms.vo.channel.SmsChannelRespVO;
import com.sqlapk.medicine.module.system.controller.admin.sms.vo.channel.SmsChannelSimpleRespVO;
import com.sqlapk.medicine.module.system.controller.admin.sms.vo.channel.SmsChannelUpdateReqVO;
import com.sqlapk.medicine.module.system.convert.sms.SmsChannelConvert;
import com.sqlapk.medicine.module.system.dal.dataobject.sms.SmsChannelDO;
import com.sqlapk.medicine.module.system.service.sms.SmsChannelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Comparator;
import java.util.List;

import static com.sqlapk.medicine.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 短信渠道")
@RestController
@RequestMapping("system/sms-channel")
public class SmsChannelController {

    @Resource
    private SmsChannelService smsChannelService;

    @PostMapping("/create")
    @Operation(summary = "创建短信渠道")
    @PreAuthorize("@ss.hasPermission('system:sms-channel:create')")
    public CommonResult<Long> createSmsChannel(@Valid @RequestBody SmsChannelCreateReqVO createReqVO) {
        return success(smsChannelService.createSmsChannel(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新短信渠道")
    @PreAuthorize("@ss.hasPermission('system:sms-channel:update')")
    public CommonResult<Boolean> updateSmsChannel(@Valid @RequestBody SmsChannelUpdateReqVO updateReqVO) {
        smsChannelService.updateSmsChannel(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除短信渠道")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('system:sms-channel:delete')")
    public CommonResult<Boolean> deleteSmsChannel(@RequestParam("id") Long id) {
        smsChannelService.deleteSmsChannel(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得短信渠道")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:sms-channel:query')")
    public CommonResult<SmsChannelRespVO> getSmsChannel(@RequestParam("id") Long id) {
        SmsChannelDO smsChannel = smsChannelService.getSmsChannel(id);
        return success(SmsChannelConvert.INSTANCE.convert(smsChannel));
    }

    @GetMapping("/page")
    @Operation(summary = "获得短信渠道分页")
    @PreAuthorize("@ss.hasPermission('system:sms-channel:query')")
    public CommonResult<PageResult<SmsChannelRespVO>> getSmsChannelPage(@Valid SmsChannelPageReqVO pageVO) {
        PageResult<SmsChannelDO> pageResult = smsChannelService.getSmsChannelPage(pageVO);
        return success(SmsChannelConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/list-all-simple")
    @Operation(summary = "获得短信渠道精简列表", description = "包含被禁用的短信渠道")
    public CommonResult<List<SmsChannelSimpleRespVO>> getSimpleSmsChannelList() {
        List<SmsChannelDO> list = smsChannelService.getSmsChannelList();
        // 排序后，返回给前端
        list.sort(Comparator.comparing(SmsChannelDO::getId));
        return success(SmsChannelConvert.INSTANCE.convertList03(list));
    }

}
