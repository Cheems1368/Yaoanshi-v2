package com.sqlapk.medicine.module.patient.service.inpatientround;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientround.vo.InpatientRoundCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientround.vo.InpatientRoundExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientround.vo.InpatientRoundPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientround.vo.InpatientRoundUpdateReqVO;
import com.sqlapk.medicine.module.patient.convert.inpatientround.InpatientRoundConvert;
import com.sqlapk.medicine.module.patient.dal.dataobject.inpatientround.InpatientRoundDO;
import com.sqlapk.medicine.module.patient.dal.mysql.inpatientround.InpatientRoundMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static com.sqlapk.medicine.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sqlapk.medicine.module.patient.enums.ErrorCodeConstants.INPATIENT_ROUND_NOT_EXISTS;

/**
 * 住院药学查房记录 Service 实现类
 *
 * @author 管理员
 */
@Service
@Validated
public class InpatientRoundServiceImpl implements InpatientRoundService {

    @Resource
    private InpatientRoundMapper inpatientRoundMapper;

    @Override
    public Long createInpatientRound(InpatientRoundCreateReqVO createReqVO) {
        // 插入
        InpatientRoundDO inpatientRound = InpatientRoundConvert.INSTANCE.convert(createReqVO);
        inpatientRoundMapper.insert(inpatientRound);
        // 返回
        return inpatientRound.getId();
    }

    @Override
    public void updateInpatientRound(InpatientRoundUpdateReqVO updateReqVO) {
        // 校验存在
        validateInpatientRoundExists(updateReqVO.getId());
        // 更新
        InpatientRoundDO updateObj = InpatientRoundConvert.INSTANCE.convert(updateReqVO);
        inpatientRoundMapper.updateById(updateObj);
    }

    @Override
    public void deleteInpatientRound(Long id) {
        // 校验存在
        validateInpatientRoundExists(id);
        // 删除
        inpatientRoundMapper.deleteById(id);
    }

    private void validateInpatientRoundExists(Long id) {
        if (inpatientRoundMapper.selectById(id) == null) {
            throw exception(INPATIENT_ROUND_NOT_EXISTS);
        }
    }

    @Override
    public InpatientRoundDO getInpatientRound(Long id) {
        return inpatientRoundMapper.selectById(id);
    }

    @Override
    public List<InpatientRoundDO> getInpatientRoundList(Collection<Long> ids) {
        return inpatientRoundMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<InpatientRoundDO> getInpatientRoundPage(InpatientRoundPageReqVO pageReqVO) {
        return inpatientRoundMapper.selectPage(pageReqVO);
    }

    @Override
    public List<InpatientRoundDO> getInpatientRoundList(InpatientRoundExportReqVO exportReqVO) {
        return inpatientRoundMapper.selectList(exportReqVO);
    }

}
