package com.sqlapk.medicine.module.system.controller.admin.notice;

import com.sqlapk.medicine.framework.common.pojo.CommonResult;
import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.system.controller.admin.notice.vo.NoticeCreateReqVO;
import com.sqlapk.medicine.module.system.controller.admin.notice.vo.NoticePageReqVO;
import com.sqlapk.medicine.module.system.controller.admin.notice.vo.NoticeRespVO;
import com.sqlapk.medicine.module.system.controller.admin.notice.vo.NoticeUpdateReqVO;
import com.sqlapk.medicine.module.system.convert.notice.NoticeConvert;
import com.sqlapk.medicine.module.system.dal.dataobject.notice.NoticeDO;
import com.sqlapk.medicine.module.system.service.notice.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
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
import java.util.Collections;
import java.util.List;

import static com.sqlapk.medicine.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 通知公告")
@RestController
@RequestMapping("/system/notice")
@Validated
public class NoticeController {

    @Resource
    private NoticeService noticeService;

    @PostMapping("/create")
    @Operation(summary = "创建通知公告")
    @PreAuthorize("@ss.hasPermission('system:notice:create')")
    public CommonResult<Long> createNotice(@Valid @RequestBody NoticeCreateReqVO reqVO) {
        Long noticeId = noticeService.createNotice(reqVO);
        return success(noticeId);
    }

    @PutMapping("/update")
    @Operation(summary = "修改通知公告")
    @PreAuthorize("@ss.hasPermission('system:notice:update')")
    public CommonResult<Boolean> updateNotice(@Valid @RequestBody NoticeUpdateReqVO reqVO) {
        noticeService.updateNotice(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除通知公告")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:notice:delete')")
    public CommonResult<Boolean> deleteNotice(@RequestParam("id") Long id) {
        noticeService.deleteNotice(id);
        return success(true);
    }

    @GetMapping("/page")
    @Operation(summary = "获取通知公告列表")
    @PreAuthorize("@ss.hasPermission('system:notice:query')")
    public CommonResult<PageResult<NoticeRespVO>> getNoticePage(@Validated NoticePageReqVO reqVO) {

        return success(NoticeConvert.INSTANCE.convertPage(noticeService.getNoticePage(reqVO)));

    }

    @GetMapping("/get")
    @Operation(summary = "获得通知公告")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:notice:query')")
    public CommonResult<NoticeRespVO> getNotice(@RequestParam("id") Long id) {
        return success(NoticeConvert.INSTANCE.convert(noticeService.getNotice(id)));
    }

    @GetMapping("/display/page")
    @Operation(summary = "获取通知公告列表")
    @PreAuthorize("@ss.hasPermission('system:notice:query')")
    public CommonResult<PageResult<NoticeRespVO>> getDisplayNoticePage(@Validated NoticePageReqVO reqVO) {
        PageResult<NoticeDO> noticePage = noticeService.getNoticePage(reqVO);
        PageResult<NoticeRespVO> noticeRespVOPageResult = NoticeConvert.INSTANCE.convertPage(noticePage);
        List<NoticeRespVO> response = noticeRespVOPageResult.getList();
        response.forEach(item -> {
            item.setDate(item.getCreateTime());
            item.setTypeName(item.getType() == 1? "通知": "公告");
            item.setKeys(Collections.singletonList(item.getTypeName()));
        });
        return success(noticeRespVOPageResult);

    }
}
