package com.sqlapk.medicine.module.drugs.dal.mysql.drug;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.mybatis.core.mapper.BaseMapperX;
import com.sqlapk.medicine.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.sqlapk.medicine.module.drugs.controller.admin.drug.vo.DrugsExportReqVO;
import com.sqlapk.medicine.module.drugs.controller.admin.drug.vo.DrugsPageReqVO;
import com.sqlapk.medicine.module.drugs.dal.dataobject.drug.DrugsDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 药品 Mapper
 *
 * @author 管理员
 */
@Mapper
public interface DrugsMapper extends BaseMapperX<DrugsDO> {

    default PageResult<DrugsDO> selectPage(DrugsPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<DrugsDO>()
                .likeIfPresent(DrugsDO::getDrugName, reqVO.getDrugName())
                .eqIfPresent(DrugsDO::getSpecification, reqVO.getSpecification())
                .eqIfPresent(DrugsDO::getManufacturer, reqVO.getManufacturer())
                .eqIfPresent(DrugsDO::getApprovalNumber, reqVO.getApprovalNumber())
                .betweenIfPresent(DrugsDO::getExpiryDate, reqVO.getExpiryDate())
                .eqIfPresent(DrugsDO::getCategory, reqVO.getCategory())
                .eqIfPresent(DrugsDO::getDosageForm, reqVO.getDosageForm())
                .eqIfPresent(DrugsDO::getIngredients, reqVO.getIngredients())
                .eqIfPresent(DrugsDO::getIndications, reqVO.getIndications())
                .eqIfPresent(DrugsDO::getDosage, reqVO.getDosage())
                .eqIfPresent(DrugsDO::getSideEffects, reqVO.getSideEffects())
                .eqIfPresent(DrugsDO::getContraindications, reqVO.getContraindications())
                .eqIfPresent(DrugsDO::getPrecautions, reqVO.getPrecautions())
                .eqIfPresent(DrugsDO::getInteractions, reqVO.getInteractions())
                .eqIfPresent(DrugsDO::getImageUrl, reqVO.getImageUrl())
                .orderByDesc(DrugsDO::getDrugId));
    }

    default List<DrugsDO> selectList(DrugsExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<DrugsDO>()
                .likeIfPresent(DrugsDO::getDrugName, reqVO.getDrugName())
                .eqIfPresent(DrugsDO::getSpecification, reqVO.getSpecification())
                .eqIfPresent(DrugsDO::getManufacturer, reqVO.getManufacturer())
                .eqIfPresent(DrugsDO::getApprovalNumber, reqVO.getApprovalNumber())
                .betweenIfPresent(DrugsDO::getExpiryDate, reqVO.getExpiryDate())
                .eqIfPresent(DrugsDO::getCategory, reqVO.getCategory())
                .eqIfPresent(DrugsDO::getDosageForm, reqVO.getDosageForm())
                .eqIfPresent(DrugsDO::getIngredients, reqVO.getIngredients())
                .eqIfPresent(DrugsDO::getIndications, reqVO.getIndications())
                .eqIfPresent(DrugsDO::getDosage, reqVO.getDosage())
                .eqIfPresent(DrugsDO::getSideEffects, reqVO.getSideEffects())
                .eqIfPresent(DrugsDO::getContraindications, reqVO.getContraindications())
                .eqIfPresent(DrugsDO::getPrecautions, reqVO.getPrecautions())
                .eqIfPresent(DrugsDO::getInteractions, reqVO.getInteractions())
                .eqIfPresent(DrugsDO::getImageUrl, reqVO.getImageUrl())
                .orderByDesc(DrugsDO::getDrugId));
    }

}
