package com.sqlapk.medicine.module.patient.service.medicinereconciliation;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.test.core.ut.BaseDbUnitTest;
import com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliation.vo.MedicineReconciliationCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliation.vo.MedicineReconciliationExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliation.vo.MedicineReconciliationPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliation.vo.MedicineReconciliationUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.medicinereconciliation.MedicineReconciliationDO;
import com.sqlapk.medicine.module.patient.dal.mysql.medicinereconciliation.MedicineReconciliationMapper;
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
import static com.sqlapk.medicine.module.patient.enums.ErrorCodeConstants.MEDICINE_RECONCILIATION_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link MedicineReconciliationServiceImpl} 的单元测试类
 *
 * @author 管理员
 */
@Import(MedicineReconciliationServiceImpl.class)
public class MedicineReconciliationServiceImplTest extends BaseDbUnitTest {

    @Resource
    private MedicineReconciliationServiceImpl medicineReconciliationService;

    @Resource
    private MedicineReconciliationMapper medicineReconciliationMapper;

    @Test
    public void testCreateMedicineReconciliation_success() {
        // 准备参数
        MedicineReconciliationCreateReqVO reqVO = randomPojo(MedicineReconciliationCreateReqVO.class);

        // 调用
        Long medicineReconciliationId = medicineReconciliationService.createMedicineReconciliation(reqVO);
        // 断言
        assertNotNull(medicineReconciliationId);
        // 校验记录的属性是否正确
        MedicineReconciliationDO medicineReconciliation = medicineReconciliationMapper.selectById(medicineReconciliationId);
        assertPojoEquals(reqVO, medicineReconciliation);
    }

    @Test
    public void testUpdateMedicineReconciliation_success() {
        // mock 数据
        MedicineReconciliationDO dbMedicineReconciliation = randomPojo(MedicineReconciliationDO.class);
        medicineReconciliationMapper.insert(dbMedicineReconciliation);// @Sql: 先插入出一条存在的数据
        // 准备参数
        MedicineReconciliationUpdateReqVO reqVO = randomPojo(MedicineReconciliationUpdateReqVO.class, o -> {
            o.setId(dbMedicineReconciliation.getId()); // 设置更新的 ID
        });

        // 调用
        medicineReconciliationService.updateMedicineReconciliation(reqVO);
        // 校验是否更新正确
        MedicineReconciliationDO medicineReconciliation = medicineReconciliationMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, medicineReconciliation);
    }

    @Test
    public void testUpdateMedicineReconciliation_notExists() {
        // 准备参数
        MedicineReconciliationUpdateReqVO reqVO = randomPojo(MedicineReconciliationUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> medicineReconciliationService.updateMedicineReconciliation(reqVO), MEDICINE_RECONCILIATION_NOT_EXISTS);
    }

    @Test
    public void testDeleteMedicineReconciliation_success() {
        // mock 数据
        MedicineReconciliationDO dbMedicineReconciliation = randomPojo(MedicineReconciliationDO.class);
        medicineReconciliationMapper.insert(dbMedicineReconciliation);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbMedicineReconciliation.getId();

        // 调用
        medicineReconciliationService.deleteMedicineReconciliation(id);
       // 校验数据不存在了
       assertNull(medicineReconciliationMapper.selectById(id));
    }

    @Test
    public void testDeleteMedicineReconciliation_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> medicineReconciliationService.deleteMedicineReconciliation(id), MEDICINE_RECONCILIATION_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetMedicineReconciliationPage() {
       // mock 数据
       MedicineReconciliationDO dbMedicineReconciliation = randomPojo(MedicineReconciliationDO.class, o -> { // 等会查询到
           o.setPatientId(null);
           o.setDoctorId(null);
           o.setName(null);
           o.setGender(null);
           o.setBirthday(null);
           o.setPhone(null);
           o.setClinicTime(null);
           o.setDischargeTime(null);
           o.setDiagnose(null);
           o.setAllergyHistory(null);
           o.setMedicineQuestion(null);
           o.setMedicineDoctor(null);
           o.setCreateTime(null);
       });
       medicineReconciliationMapper.insert(dbMedicineReconciliation);
       // 测试 patientId 不匹配
       medicineReconciliationMapper.insert(cloneIgnoreId(dbMedicineReconciliation, o -> o.setPatientId(null)));
       // 测试 doctorId 不匹配
       medicineReconciliationMapper.insert(cloneIgnoreId(dbMedicineReconciliation, o -> o.setDoctorId(null)));
       // 测试 name 不匹配
       medicineReconciliationMapper.insert(cloneIgnoreId(dbMedicineReconciliation, o -> o.setName(null)));
       // 测试 gender 不匹配
       medicineReconciliationMapper.insert(cloneIgnoreId(dbMedicineReconciliation, o -> o.setGender(null)));
       // 测试 birthday 不匹配
       medicineReconciliationMapper.insert(cloneIgnoreId(dbMedicineReconciliation, o -> o.setBirthday(null)));
       // 测试 phone 不匹配
       medicineReconciliationMapper.insert(cloneIgnoreId(dbMedicineReconciliation, o -> o.setPhone(null)));
       // 测试 clinicTime 不匹配
       medicineReconciliationMapper.insert(cloneIgnoreId(dbMedicineReconciliation, o -> o.setClinicTime(null)));
       // 测试 dischargeTime 不匹配
       medicineReconciliationMapper.insert(cloneIgnoreId(dbMedicineReconciliation, o -> o.setDischargeTime(null)));
       // 测试 diagnose 不匹配
       medicineReconciliationMapper.insert(cloneIgnoreId(dbMedicineReconciliation, o -> o.setDiagnose(null)));
       // 测试 allergyHistory 不匹配
       medicineReconciliationMapper.insert(cloneIgnoreId(dbMedicineReconciliation, o -> o.setAllergyHistory(null)));
       // 测试 medicineQuestion 不匹配
       medicineReconciliationMapper.insert(cloneIgnoreId(dbMedicineReconciliation, o -> o.setMedicineQuestion(null)));
       // 测试 medicineDoctor 不匹配
       medicineReconciliationMapper.insert(cloneIgnoreId(dbMedicineReconciliation, o -> o.setMedicineDoctor(null)));
       // 测试 createTime 不匹配
       medicineReconciliationMapper.insert(cloneIgnoreId(dbMedicineReconciliation, o -> o.setCreateTime(null)));
       // 准备参数
       MedicineReconciliationPageReqVO reqVO = new MedicineReconciliationPageReqVO();
       reqVO.setPatientId(null);
       reqVO.setDoctorId(null);
       reqVO.setName(null);
       reqVO.setGender(null);
       reqVO.setBirthday(null);
       reqVO.setPhone(null);
       reqVO.setClinicTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setDischargeTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setDiagnose(null);
       reqVO.setAllergyHistory(null);
       reqVO.setMedicineQuestion(null);
       reqVO.setMedicineDoctor(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<MedicineReconciliationDO> pageResult = medicineReconciliationService.getMedicineReconciliationPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbMedicineReconciliation, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetMedicineReconciliationList() {
       // mock 数据
       MedicineReconciliationDO dbMedicineReconciliation = randomPojo(MedicineReconciliationDO.class, o -> { // 等会查询到
           o.setPatientId(null);
           o.setDoctorId(null);
           o.setName(null);
           o.setGender(null);
           o.setBirthday(null);
           o.setPhone(null);
           o.setClinicTime(null);
           o.setDischargeTime(null);
           o.setDiagnose(null);
           o.setAllergyHistory(null);
           o.setMedicineQuestion(null);
           o.setMedicineDoctor(null);
           o.setCreateTime(null);
       });
       medicineReconciliationMapper.insert(dbMedicineReconciliation);
       // 测试 patientId 不匹配
       medicineReconciliationMapper.insert(cloneIgnoreId(dbMedicineReconciliation, o -> o.setPatientId(null)));
       // 测试 doctorId 不匹配
       medicineReconciliationMapper.insert(cloneIgnoreId(dbMedicineReconciliation, o -> o.setDoctorId(null)));
       // 测试 name 不匹配
       medicineReconciliationMapper.insert(cloneIgnoreId(dbMedicineReconciliation, o -> o.setName(null)));
       // 测试 gender 不匹配
       medicineReconciliationMapper.insert(cloneIgnoreId(dbMedicineReconciliation, o -> o.setGender(null)));
       // 测试 birthday 不匹配
       medicineReconciliationMapper.insert(cloneIgnoreId(dbMedicineReconciliation, o -> o.setBirthday(null)));
       // 测试 phone 不匹配
       medicineReconciliationMapper.insert(cloneIgnoreId(dbMedicineReconciliation, o -> o.setPhone(null)));
       // 测试 clinicTime 不匹配
       medicineReconciliationMapper.insert(cloneIgnoreId(dbMedicineReconciliation, o -> o.setClinicTime(null)));
       // 测试 dischargeTime 不匹配
       medicineReconciliationMapper.insert(cloneIgnoreId(dbMedicineReconciliation, o -> o.setDischargeTime(null)));
       // 测试 diagnose 不匹配
       medicineReconciliationMapper.insert(cloneIgnoreId(dbMedicineReconciliation, o -> o.setDiagnose(null)));
       // 测试 allergyHistory 不匹配
       medicineReconciliationMapper.insert(cloneIgnoreId(dbMedicineReconciliation, o -> o.setAllergyHistory(null)));
       // 测试 medicineQuestion 不匹配
       medicineReconciliationMapper.insert(cloneIgnoreId(dbMedicineReconciliation, o -> o.setMedicineQuestion(null)));
       // 测试 medicineDoctor 不匹配
       medicineReconciliationMapper.insert(cloneIgnoreId(dbMedicineReconciliation, o -> o.setMedicineDoctor(null)));
       // 测试 createTime 不匹配
       medicineReconciliationMapper.insert(cloneIgnoreId(dbMedicineReconciliation, o -> o.setCreateTime(null)));
       // 准备参数
       MedicineReconciliationExportReqVO reqVO = new MedicineReconciliationExportReqVO();
       reqVO.setPatientId(null);
       reqVO.setDoctorId(null);
       reqVO.setName(null);
       reqVO.setGender(null);
       reqVO.setBirthday(null);
       reqVO.setPhone(null);
       reqVO.setClinicTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setDischargeTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setDiagnose(null);
       reqVO.setAllergyHistory(null);
       reqVO.setMedicineQuestion(null);
       reqVO.setMedicineDoctor(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       List<MedicineReconciliationDO> list = medicineReconciliationService.getMedicineReconciliationList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbMedicineReconciliation, list.get(0));
    }

}
