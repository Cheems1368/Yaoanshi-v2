package com.sqlapk.medicine.module.patient.service.patient;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.patient.controller.admin.patient.vo.PatientsCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.patient.vo.PatientsExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.patient.vo.PatientsPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.patient.vo.PatientsSimpleRespVO;
import com.sqlapk.medicine.module.patient.controller.admin.patient.vo.PatientsUpdateReqVO;
import com.sqlapk.medicine.module.patient.convert.patient.PatientsConvert;
import com.sqlapk.medicine.module.patient.dal.dataobject.patient.PatientsDO;
import com.sqlapk.medicine.module.patient.dal.mysql.patient.PatientsMapper;
import com.sqlapk.medicine.module.system.api.user.AdminUserApi;
import com.sqlapk.medicine.module.system.api.user.dto.AdminUserRespDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static com.sqlapk.medicine.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.sqlapk.medicine.module.patient.enums.ErrorCodeConstants.PATIENT_NOT_EXISTS;

/**
 * 患者 Service 实现类
 *
 * @author 管理员
 */
@Service
@Validated
public class PatientsServiceImpl implements PatientsService {

    @Resource
    private PatientsMapper sMapper;
    @Autowired
    private AdminUserApi adminUserService;
    @Resource
    private PatientsConvert patientsConvert;

    @Override
    public Long creates(PatientsCreateReqVO createReqVO) {
        // 插入
        PatientsDO s = patientsConvert.convert(createReqVO);
        sMapper.insert(s);
        // 返回
        return s.getId();
    }

    @Override
    public void updates(PatientsUpdateReqVO updateReqVO) {
        // 校验存在
        validatesExists(updateReqVO.getId());
        // 更新
        PatientsDO updateObj = patientsConvert.convert(updateReqVO);
        sMapper.updateById(updateObj);
    }

    @Override
    public void deletes(Long id) {
        // 校验存在
        validatesExists(id);
        // 删除
        sMapper.deleteById(id);
    }

    private void validatesExists(Long id) {
        if (sMapper.selectById(id) == null) {
            throw exception(PATIENT_NOT_EXISTS);
        }
    }

    @Override
    public PatientsDO gets(Long id) {
        return sMapper.selectById(id);
    }

    @Override
    public List<PatientsDO> getsList(Collection<Long> ids) {
        return sMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<PatientsDO> getsPage(PatientsPageReqVO pageReqVO) {
        return sMapper.selectPage(pageReqVO);
    }

    @Override
    public List<PatientsDO> getsList(PatientsExportReqVO exportReqVO) {
        return sMapper.selectList(exportReqVO);
    }


    @Override
    public List<PatientsSimpleRespVO> getPatientSimple() {
        List<PatientsDO> patientsDOS = sMapper.selectList();
        List<PatientsSimpleRespVO> patientsSimpleRespVOS = patientsConvert.convertSimple(patientsDOS);
        patientsSimpleRespVOS.forEach(item -> {
            AdminUserRespDTO user = adminUserService.getUser(item.getUserId());
            item.setAge(user.getAge());
            item.setSex(user.getSex());
            item.setNickname(user.getNickname());
            item.setPhone(user.getMobile());
        });

        return patientsSimpleRespVOS;
    }

    @Override
    public PatientsDO getByUserId(Long userId) {
        return sMapper.selectByUserId(userId);
    }
}
