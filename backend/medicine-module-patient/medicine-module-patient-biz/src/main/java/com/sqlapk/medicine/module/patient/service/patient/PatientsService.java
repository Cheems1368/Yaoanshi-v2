package com.sqlapk.medicine.module.patient.service.patient;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.patient.vo.PatientsCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.patient.vo.PatientsExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.patient.vo.PatientsPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.patient.vo.PatientsSimpleRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.patient.vo.PatientsUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.patient.PatientsDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 患者 Service 接口
 *
 * @author 管理员
 */
public interface PatientsService {

    /**
     * 创建患者
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long creates(@Valid PatientsCreateReqVO createReqVO);

    /**
     * 更新患者
     *
     * @param updateReqVO 更新信息
     */
    void updates(@Valid PatientsUpdateReqVO updateReqVO);

    /**
     * 删除患者
     *
     * @param id 编号
     */
    void deletes(Long id);

    /**
     * 获得患者
     *
     * @param id 编号
     * @return 患者
     */
    PatientsDO gets(Long id);

    /**
     * 获得患者列表
     *
     * @param ids 编号
     * @return 患者列表
     */
    List<PatientsDO> getsList(Collection<Long> ids);

    /**
     * 获得患者分页
     *
     * @param pageReqVO 分页查询
     * @return 患者分页
     */
    PageResult<PatientsDO> getsPage(PatientsPageReqVO pageReqVO);

    /**
     * 获得患者列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 患者列表
     */
    List<PatientsDO> getsList(PatientsExportReqVO exportReqVO);

    List<PatientsSimpleRespVO> getPatientSimple();

    /**
     * 根据用户ID获得患者信息
     *
     * @param userId 用户ID
     * @return 患者信息
     */
    PatientsDO getByUserId(Long userId);

}
