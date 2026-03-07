package com.sqlapk.medicine.module.drugs.service.drug;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.drugs.controller.admin.drug.vo.DrugsCreateReqVO;
import com.sqlapk.medicine.module.drugs.controller.admin.drug.vo.DrugsExportReqVO;
import com.sqlapk.medicine.module.drugs.controller.admin.drug.vo.DrugsPageReqVO;
import com.sqlapk.medicine.module.drugs.controller.admin.drug.vo.DrugsUpdateReqVO;
import com.sqlapk.medicine.module.drugs.convert.drug.DrugsConvert;
import com.sqlapk.medicine.module.drugs.dal.dataobject.drug.DrugsDO;
import com.sqlapk.medicine.module.drugs.dal.mysql.drug.DrugsMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static com.sqlapk.medicine.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sqlapk.medicine.module.drugs.enums.ErrorCodeConstants.DRUGS_NOT_EXISTS;

/**
 * 药品 Service 实现类
 *
 * @author 管理员
 */
@Service
@Validated
public class DrugsServiceImpl implements DrugsService {

    @Resource
    private DrugsMapper Mapper;

    @Override
    public Long create(DrugsCreateReqVO createReqVO) {
        // 插入
        DrugsDO drugsDO  = DrugsConvert.INSTANCE.convert(createReqVO);
        Mapper.insert(drugsDO);
        // 返回
        return drugsDO.getDrugId();
    }

    @Override
    public void update(DrugsUpdateReqVO updateReqVO) {
        // 校验存在
        validateExists(updateReqVO.getDrugId());
        // 更新
        DrugsDO updateObj = DrugsConvert.INSTANCE.convert(updateReqVO);
        Mapper.updateById(updateObj);
    }

    @Override
    public void delete(Long id) {
        // 校验存在
        validateExists(id);
        // 删除
        Mapper.deleteById(id);
    }

    private void validateExists(Long id) {
        if (Mapper.selectById(id) == null) {
            throw exception(DRUGS_NOT_EXISTS);
        }
    }

    @Override
    public DrugsDO get(Long id) {
        return Mapper.selectById(id);
    }

    @Override
    public List<DrugsDO> getList(Collection<Long> ids) {
        return Mapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<DrugsDO> getPage(DrugsPageReqVO pageReqVO) {
        return Mapper.selectPage(pageReqVO);
    }

    @Override
    public List<DrugsDO> getList(DrugsExportReqVO exportReqVO) {
        return Mapper.selectList(exportReqVO);
    }

}
