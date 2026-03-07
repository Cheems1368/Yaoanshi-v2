package com.sqlapk.medicine.module.patient.service.options;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import com.sqlapk.medicine.module.patient.controller.admin.options.vo.*;
import com.sqlapk.medicine.module.patient.dal.dataobject.options.OptionsDO;
import com.sqlapk.medicine.framework.common.pojo.PageResult;

import com.sqlapk.medicine.module.patient.convert.options.OptionsConvert;
import com.sqlapk.medicine.module.patient.dal.mysql.options.OptionsMapper;

import static com.sqlapk.medicine.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sqlapk.medicine.module.patient.enums.ErrorCodeConstants.*;

/**
 * 问题选项 Service 实现类
 *
 * @author 管理员
 */
@Service
@Validated
public class OptionsServiceImpl implements OptionsService {

    @Resource
    private OptionsMapper optionsMapper;

    @Override
    public Long createOptions(OptionsCreateReqVO createReqVO) {
        // 插入
        OptionsDO options = OptionsConvert.INSTANCE.convert(createReqVO);
        optionsMapper.insert(options);
        // 返回
        return options.getId();
    }

    @Override
    public void updateOptions(OptionsUpdateReqVO updateReqVO) {
        // 校验存在
        validateOptionsExists(updateReqVO.getId());
        // 更新
        OptionsDO updateObj = OptionsConvert.INSTANCE.convert(updateReqVO);
        optionsMapper.updateById(updateObj);
    }

    @Override
    public void deleteOptions(Long id) {
        // 校验存在
        validateOptionsExists(id);
        // 删除
        optionsMapper.deleteById(id);
    }

    private void validateOptionsExists(Long id) {
        if (optionsMapper.selectById(id) == null) {
            throw exception(OPTIONS_NOT_EXISTS);
        }
    }

    @Override
    public OptionsDO getOptions(Long id) {
        return optionsMapper.selectById(id);
    }

    @Override
    public List<OptionsDO> getOptionsList(Collection<Long> ids) {
        return optionsMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<OptionsDO> getOptionsPage(OptionsPageReqVO pageReqVO) {
        return optionsMapper.selectPage(pageReqVO);
    }

    @Override
    public List<OptionsDO> getOptionsList(OptionsExportReqVO exportReqVO) {
        return optionsMapper.selectList(exportReqVO);
    }

}
