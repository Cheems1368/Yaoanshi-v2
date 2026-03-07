package com.sqlapk.medicine.module.patient.dal.mysql.homemedicine;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.mybatis.core.mapper.BaseMapperX;
import com.sqlapk.medicine.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sqlapk.medicine.module.patient.controller.admin.homemedicine.vo.HomeMedicineExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homemedicine.vo.HomeMedicinePageReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.homemedicine.HomeMedicineDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 用药清单 Mapper
 *
 * @author 管理员
 */
@Mapper
public interface HomeMedicineMapper extends BaseMapperX<HomeMedicineDO> {

    default PageResult<HomeMedicineDO> selectPage(HomeMedicinePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<HomeMedicineDO>()
                .eqIfPresent(HomeMedicineDO::getHomeId, reqVO.getHomeId())
                .likeIfPresent(HomeMedicineDO::getMedicineName, reqVO.getMedicineName())
                .eqIfPresent(HomeMedicineDO::getDoctorId, reqVO.getDoctorId())
                .likeIfPresent(HomeMedicineDO::getDoctorName, reqVO.getDoctorName())
                .eqIfPresent(HomeMedicineDO::getDosage, reqVO.getDosage())
                .eqIfPresent(HomeMedicineDO::getIndications, reqVO.getIndications())
                .eqIfPresent(HomeMedicineDO::getMedicineUsage, reqVO.getMedicineUsage())
                .betweenIfPresent(HomeMedicineDO::getStartTime, reqVO.getStartTime())
                .betweenIfPresent(HomeMedicineDO::getEndTime, reqVO.getEndTime())
                .eqIfPresent(HomeMedicineDO::getComment, reqVO.getComment())
                .betweenIfPresent(HomeMedicineDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(HomeMedicineDO::getId));
    }

    default List<HomeMedicineDO> selectList(HomeMedicineExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<HomeMedicineDO>()
                .eqIfPresent(HomeMedicineDO::getHomeId, reqVO.getHomeId())
                .likeIfPresent(HomeMedicineDO::getMedicineName, reqVO.getMedicineName())
                .eqIfPresent(HomeMedicineDO::getDoctorId, reqVO.getDoctorId())
                .likeIfPresent(HomeMedicineDO::getDoctorName, reqVO.getDoctorName())
                .eqIfPresent(HomeMedicineDO::getDosage, reqVO.getDosage())
                .eqIfPresent(HomeMedicineDO::getIndications, reqVO.getIndications())
                .eqIfPresent(HomeMedicineDO::getMedicineUsage, reqVO.getMedicineUsage())
                .betweenIfPresent(HomeMedicineDO::getStartTime, reqVO.getStartTime())
                .betweenIfPresent(HomeMedicineDO::getEndTime, reqVO.getEndTime())
                .eqIfPresent(HomeMedicineDO::getComment, reqVO.getComment())
                .betweenIfPresent(HomeMedicineDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(HomeMedicineDO::getId));
    }

}
