package com.sqlapk.medicine.module.medication.api;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.medication.api.dto.CheckinSubmitReqDTO;
import com.sqlapk.medicine.module.medication.api.dto.CheckinRevokeReqDTO;
import com.sqlapk.medicine.module.medication.api.dto.CheckinRespDTO;
import com.sqlapk.medicine.module.medication.api.dto.CheckinVerifyReqDTO;
import com.sqlapk.medicine.module.medication.api.dto.CheckinPageReqDTO;

import java.time.LocalDate;
import java.util.List;

/**
 * 用药打卡服务 API
 *
 * @author sqlapk
 */
public interface MedCheckinService {

    /**
     * 提交打卡
     *
     * @param submitReqDTO 打卡提交请求
     * @return 打卡记录ID
     */
    Long submitCheckin(CheckinSubmitReqDTO submitReqDTO);

    /**
     * 撤销打卡
     *
     * @param revokeReqDTO 撤销请求
     */
    void revokeCheckin(CheckinRevokeReqDTO revokeReqDTO);

    /**
     * 核实打卡
     *
     * @param verifyReqDTO 核实请求
     */
    void verifyCheckin(CheckinVerifyReqDTO verifyReqDTO);

    /**
     * 根据患者ID和日期范围获取打卡记录
     *
     * @param patientId 患者ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 打卡记录列表
     */
    List<CheckinRespDTO> getCheckinsByPatientIdAndDateRange(Long patientId, LocalDate startDate, LocalDate endDate);

    /**
     * 根据患者ID和日期获取打卡记录
     *
     * @param patientId 患者ID
     * @param taskDate 任务日期
     * @return 打卡记录列表
     */
    List<CheckinRespDTO> getCheckinsByPatientIdAndTaskDate(Long patientId, LocalDate taskDate);

    /**
     * 获取打卡记录详情
     *
     * @param checkinId 打卡记录ID
     * @return 打卡记录详情
     */
    CheckinRespDTO getCheckinById(Long checkinId);

    /**
     * 检查客户端唯一标识是否已使用
     *
     * @param clientNonce 客户端唯一标识
     * @return 是否已使用
     */
    boolean isClientNonceUsed(String clientNonce);

    /**
     * 根据患者ID获取打卡记录
     *
     * @param patientId 患者ID
     * @return 打卡记录列表
     */
    List<CheckinRespDTO> getCheckinsByPatientId(Long patientId);

    /**
     * 获取最近打卡记录
     *
     * @param patientId 患者ID
     * @param days 天数
     * @return 打卡记录列表
     */
    List<CheckinRespDTO> getRecentCheckins(Long patientId, Integer days);

    /**
     * 统计患者指定日期范围内的打卡次数
     *
     * @param patientId 患者ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 打卡次数
     */
    Integer countCompletedCheckins(Long patientId, LocalDate startDate, LocalDate endDate);

    /**
     * 统计患者指定日期范围内的跳过次数
     *
     * @param patientId 患者ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 跳过次数
     */
    Integer countSkippedCheckins(Long patientId, LocalDate startDate, LocalDate endDate);

    /**
     * 分页查询打卡记录
     *
     * @param pageReqDTO 分页查询请求
     * @return 分页结果
     */
    PageResult<CheckinRespDTO> getCheckinPage(CheckinPageReqDTO pageReqDTO);

}
