package com.sqlapk.medicine.module.patient.service.homeeducation;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.homeeducation.vo.HomeEducationCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeeducation.vo.HomeEducationExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeeducation.vo.HomeEducationPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeeducation.vo.HomeEducationUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.homeeducation.HomeEducationDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 居家用药教育 Service 接口
 *
 * @author 管理员
 */
public interface HomeEducationService {

    /**
     * 创建居家用药教育
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createHomeEducation(@Valid HomeEducationCreateReqVO createReqVO);

    /**
     * 更新居家用药教育
     *
     * @param updateReqVO 更新信息
     */
    void updateHomeEducation(@Valid HomeEducationUpdateReqVO updateReqVO);

    /**
     * 删除居家用药教育
     *
     * @param id 编号
     */
    void deleteHomeEducation(Long id);

    /**
     * 获得居家用药教育
     *
     * @param id 编号
     * @return 居家用药教育
     */
    HomeEducationDO getHomeEducation(Long id);

    /**
     * 获得居家用药教育列表
     *
     * @param ids 编号
     * @return 居家用药教育列表
     */
    List<HomeEducationDO> getHomeEducationList(Collection<Long> ids);

    /**
     * 获得居家用药教育分页
     *
     * @param pageReqVO 分页查询
     * @return 居家用药教育分页
     */
    PageResult<HomeEducationDO> getHomeEducationPage(HomeEducationPageReqVO pageReqVO);

    /**
     * 获得居家用药教育列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 居家用药教育列表
     */
    List<HomeEducationDO> getHomeEducationList(HomeEducationExportReqVO exportReqVO);

}
