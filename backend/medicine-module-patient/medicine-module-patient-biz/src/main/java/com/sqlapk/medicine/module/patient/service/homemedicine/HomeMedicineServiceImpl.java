package com.sqlapk.medicine.module.patient.service.homemedicine;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.homemedicine.vo.HomeMedicineCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homemedicine.vo.HomeMedicineExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homemedicine.vo.HomeMedicinePageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homemedicine.vo.HomeMedicineUpdateReqVO;
import com.sqlapk.medicine.module.patient.convert.homemedicine.HomeMedicineConvert;
import com.sqlapk.medicine.module.patient.dal.dataobject.homemedicine.HomeMedicineDO;
import com.sqlapk.medicine.module.patient.dal.mysql.homemedicine.HomeMedicineMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static com.sqlapk.medicine.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sqlapk.medicine.module.patient.enums.ErrorCodeConstants.HOME_MEDICINE_NOT_EXISTS;

/**
 * 用药清单 Service 实现类
 *
 * @author 管理员
 */
@Service
@Validated
public class HomeMedicineServiceImpl implements HomeMedicineService {

    @Resource
    private HomeMedicineMapper homeMedicineMapper;

    @Override
    public Long createHomeMedicine(HomeMedicineCreateReqVO createReqVO) {
        // 插入
        HomeMedicineDO homeMedicine = HomeMedicineConvert.INSTANCE.convert(createReqVO);
        homeMedicineMapper.insert(homeMedicine);
        // 返回
        return homeMedicine.getId();
    }

    @Override
    public void updateHomeMedicine(HomeMedicineUpdateReqVO updateReqVO) {
        // 校验存在
        validateHomeMedicineExists(updateReqVO.getId());
        // 更新
        HomeMedicineDO updateObj = HomeMedicineConvert.INSTANCE.convert(updateReqVO);
        homeMedicineMapper.updateById(updateObj);
    }

    @Override
    public void deleteHomeMedicine(Long id) {
        // 校验存在
        validateHomeMedicineExists(id);
        // 删除
        homeMedicineMapper.deleteById(id);
    }

    private void validateHomeMedicineExists(Long id) {
        if (homeMedicineMapper.selectById(id) == null) {
            throw exception(HOME_MEDICINE_NOT_EXISTS);
        }
    }

    @Override
    public HomeMedicineDO getHomeMedicine(Long id) {
        return homeMedicineMapper.selectById(id);
    }

    @Override
    public List<HomeMedicineDO> getHomeMedicineList(Collection<Long> ids) {
        return homeMedicineMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<HomeMedicineDO> getHomeMedicinePage(HomeMedicinePageReqVO pageReqVO) {
        return homeMedicineMapper.selectPage(pageReqVO);
    }

    @Override
    public List<HomeMedicineDO> getHomeMedicineList(HomeMedicineExportReqVO exportReqVO) {
        return homeMedicineMapper.selectList(exportReqVO);
    }

}
