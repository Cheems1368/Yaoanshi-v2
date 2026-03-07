package com.sqlapk.medicine.module.patient.service.homeconsultation;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.homeconsultation.vo.HomeConsultationCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeconsultation.vo.HomeConsultationExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeconsultation.vo.HomeConsultationPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeconsultation.vo.HomeConsultationUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.homeconsultation.HomeConsultationDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 居家用药咨询 Service 接口
 *
 * @author 管理员
 */
public interface HomeConsultationService {

    /**
     * 创建居家用药咨询
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createHomeConsultation(@Valid HomeConsultationCreateReqVO createReqVO);

    /**
     * 更新居家用药咨询
     *
     * @param updateReqVO 更新信息
     */
    void updateHomeConsultation(@Valid HomeConsultationUpdateReqVO updateReqVO);

    /**
     * 删除居家用药咨询
     *
     * @param id 编号
     */
    void deleteHomeConsultation(Long id);

    /**
     * 获得居家用药咨询
     *
     * @param id 编号
     * @return 居家用药咨询
     */
    HomeConsultationDO getHomeConsultation(Long id);

    /**
     * 获得居家用药咨询列表
     *
     * @param ids 编号
     * @return 居家用药咨询列表
     */
    List<HomeConsultationDO> getHomeConsultationList(Collection<Long> ids);

    /**
     * 获得居家用药咨询分页
     *
     * @param pageReqVO 分页查询
     * @return 居家用药咨询分页
     */
    PageResult<HomeConsultationDO> getHomeConsultationPage(HomeConsultationPageReqVO pageReqVO);

    /**
     * 获得居家用药咨询列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 居家用药咨询列表
     */
    List<HomeConsultationDO> getHomeConsultationList(HomeConsultationExportReqVO exportReqVO);

}
