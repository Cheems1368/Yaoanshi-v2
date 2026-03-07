package com.sqlapk.medicine.module.patient.service.inpatient;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.test.core.ut.BaseDbUnitTest;
import com.sqlapk.medicine.module.patient.controller.admin.inpatient.vo.InpatientCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatient.vo.InpatientExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatient.vo.InpatientPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatient.vo.InpatientUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.inpatient.InpatientDO;
import com.sqlapk.medicine.module.patient.dal.mysql.inpatient.InpatientMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.List;

import static com.sqlapk.medicine.framework.common.util.date.LocalDateTimeUtils.buildBetweenTime;
import static com.sqlapk.medicine.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static com.sqlapk.medicine.framework.test.core.util.AssertUtils.assertPojoEquals;
import static com.sqlapk.medicine.framework.test.core.util.AssertUtils.assertServiceException;
import static com.sqlapk.medicine.framework.test.core.util.RandomUtils.randomLongId;
import static com.sqlapk.medicine.framework.test.core.util.RandomUtils.randomPojo;
import static com.sqlapk.medicine.module.patient.enums.ErrorCodeConstants.INPATIENT_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link InpatientServiceImpl} 的单元测试类
 *
 * @author 管理员
 */
@Import(InpatientServiceImpl.class)
public class InpatientServiceImplTest extends BaseDbUnitTest {

    @Resource
    private InpatientServiceImpl inpatientService;

    @Resource
    private InpatientMapper inpatientMapper;

    @Test
    public void testCreateInpatient_success() {
        // 准备参数
        InpatientCreateReqVO reqVO = randomPojo(InpatientCreateReqVO.class);

        // 调用
        Long inpatientId = inpatientService.createInpatient(reqVO);
        // 断言
        assertNotNull(inpatientId);
        // 校验记录的属性是否正确
        InpatientDO inpatient = inpatientMapper.selectById(inpatientId);
        assertPojoEquals(reqVO, inpatient);
    }

    @Test
    public void testUpdateInpatient_success() {
        // mock 数据
        InpatientDO dbInpatient = randomPojo(InpatientDO.class);
        inpatientMapper.insert(dbInpatient);// @Sql: 先插入出一条存在的数据
        // 准备参数
        InpatientUpdateReqVO reqVO = randomPojo(InpatientUpdateReqVO.class, o -> {
            o.setId(dbInpatient.getId()); // 设置更新的 ID
        });

        // 调用
        inpatientService.updateInpatient(reqVO);
        // 校验是否更新正确
        InpatientDO inpatient = inpatientMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, inpatient);
    }

    @Test
    public void testUpdateInpatient_notExists() {
        // 准备参数
        InpatientUpdateReqVO reqVO = randomPojo(InpatientUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> inpatientService.updateInpatient(reqVO), INPATIENT_NOT_EXISTS);
    }

    @Test
    public void testDeleteInpatient_success() {
        // mock 数据
        InpatientDO dbInpatient = randomPojo(InpatientDO.class);
        inpatientMapper.insert(dbInpatient);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbInpatient.getId();

        // 调用
        inpatientService.deleteInpatient(id);
       // 校验数据不存在了
       assertNull(inpatientMapper.selectById(id));
    }

    @Test
    public void testDeleteInpatient_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> inpatientService.deleteInpatient(id), INPATIENT_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetInpatientPage() {
       // mock 数据
       InpatientDO dbInpatient = randomPojo(InpatientDO.class, o -> { // 等会查询到
           o.setHospitalNumber(null);
           o.setBedNumber(null);
           o.setName(null);
           o.setGender(null);
           o.setAdmissionDate(null);
           o.setDiagnosis(null);
           o.setCompliance(null);
           o.setPatientCondition(null);
           o.setSpecialPhysiologicalStatus(null);
           o.setAdjustmentPlan(null);
           o.setCreateTime(null);
       });
       inpatientMapper.insert(dbInpatient);
       // 测试 hospitalNumber 不匹配
       inpatientMapper.insert(cloneIgnoreId(dbInpatient, o -> o.setHospitalNumber(null)));
       // 测试 bedNumber 不匹配
       inpatientMapper.insert(cloneIgnoreId(dbInpatient, o -> o.setBedNumber(null)));
       // 测试 name 不匹配
       inpatientMapper.insert(cloneIgnoreId(dbInpatient, o -> o.setName(null)));
       // 测试 gender 不匹配
       inpatientMapper.insert(cloneIgnoreId(dbInpatient, o -> o.setGender(null)));
       // 测试 admissionDate 不匹配
       inpatientMapper.insert(cloneIgnoreId(dbInpatient, o -> o.setAdmissionDate(null)));
       // 测试 diagnosis 不匹配
       inpatientMapper.insert(cloneIgnoreId(dbInpatient, o -> o.setDiagnosis(null)));
       // 测试 compliance 不匹配
       inpatientMapper.insert(cloneIgnoreId(dbInpatient, o -> o.setCompliance(null)));
       // 测试 patientCondition 不匹配
       inpatientMapper.insert(cloneIgnoreId(dbInpatient, o -> o.setPatientCondition(null)));
       // 测试 specialPhysiologicalStatus 不匹配
       inpatientMapper.insert(cloneIgnoreId(dbInpatient, o -> o.setSpecialPhysiologicalStatus(null)));
       // 测试 adjustmentPlan 不匹配
       inpatientMapper.insert(cloneIgnoreId(dbInpatient, o -> o.setAdjustmentPlan(null)));
       // 测试 createTime 不匹配
       inpatientMapper.insert(cloneIgnoreId(dbInpatient, o -> o.setCreateTime(null)));
       // 准备参数
       InpatientPageReqVO reqVO = new InpatientPageReqVO();
       reqVO.setHospitalNumber(null);
       reqVO.setBedNumber(null);
       reqVO.setName(null);
       reqVO.setGender(null);
       reqVO.setAdmissionDate(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setDiagnosis(null);
       reqVO.setCompliance(null);
       reqVO.setPatientCondition(null);
       reqVO.setSpecialPhysiologicalStatus(null);
       reqVO.setAdjustmentPlan(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<InpatientDO> pageResult = inpatientService.getInpatientPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbInpatient, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetInpatientList() {
       // mock 数据
       InpatientDO dbInpatient = randomPojo(InpatientDO.class, o -> { // 等会查询到
           o.setHospitalNumber(null);
           o.setBedNumber(null);
           o.setName(null);
           o.setGender(null);
           o.setAdmissionDate(null);
           o.setDiagnosis(null);
           o.setCompliance(null);
           o.setPatientCondition(null);
           o.setSpecialPhysiologicalStatus(null);
           o.setAdjustmentPlan(null);
           o.setCreateTime(null);
       });
       inpatientMapper.insert(dbInpatient);
       // 测试 hospitalNumber 不匹配
       inpatientMapper.insert(cloneIgnoreId(dbInpatient, o -> o.setHospitalNumber(null)));
       // 测试 bedNumber 不匹配
       inpatientMapper.insert(cloneIgnoreId(dbInpatient, o -> o.setBedNumber(null)));
       // 测试 name 不匹配
       inpatientMapper.insert(cloneIgnoreId(dbInpatient, o -> o.setName(null)));
       // 测试 gender 不匹配
       inpatientMapper.insert(cloneIgnoreId(dbInpatient, o -> o.setGender(null)));
       // 测试 admissionDate 不匹配
       inpatientMapper.insert(cloneIgnoreId(dbInpatient, o -> o.setAdmissionDate(null)));
       // 测试 diagnosis 不匹配
       inpatientMapper.insert(cloneIgnoreId(dbInpatient, o -> o.setDiagnosis(null)));
       // 测试 compliance 不匹配
       inpatientMapper.insert(cloneIgnoreId(dbInpatient, o -> o.setCompliance(null)));
       // 测试 patientCondition 不匹配
       inpatientMapper.insert(cloneIgnoreId(dbInpatient, o -> o.setPatientCondition(null)));
       // 测试 specialPhysiologicalStatus 不匹配
       inpatientMapper.insert(cloneIgnoreId(dbInpatient, o -> o.setSpecialPhysiologicalStatus(null)));
       // 测试 adjustmentPlan 不匹配
       inpatientMapper.insert(cloneIgnoreId(dbInpatient, o -> o.setAdjustmentPlan(null)));
       // 测试 createTime 不匹配
       inpatientMapper.insert(cloneIgnoreId(dbInpatient, o -> o.setCreateTime(null)));
       // 准备参数
       InpatientExportReqVO reqVO = new InpatientExportReqVO();
       reqVO.setHospitalNumber(null);
       reqVO.setBedNumber(null);
       reqVO.setName(null);
       reqVO.setGender(null);
       reqVO.setAdmissionDate(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setDiagnosis(null);
       reqVO.setCompliance(null);
       reqVO.setPatientCondition(null);
       reqVO.setSpecialPhysiologicalStatus(null);
       reqVO.setAdjustmentPlan(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       List<InpatientDO> list = inpatientService.getInpatientList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbInpatient, list.get(0));
    }

}
