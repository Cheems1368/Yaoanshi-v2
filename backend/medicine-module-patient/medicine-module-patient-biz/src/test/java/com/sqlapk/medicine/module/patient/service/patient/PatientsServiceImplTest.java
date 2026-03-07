package com.sqlapk.medicine.module.patient.service.patient;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.test.core.ut.BaseDbUnitTest;
import com.sqlapk.medicine.module.patient.controller.admin.patient.vo.PatientsCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.patient.vo.PatientsExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.patient.vo.PatientsPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.patient.vo.PatientsUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.patient.PatientsDO;
import com.sqlapk.medicine.module.patient.dal.mysql.patient.PatientsMapper;
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
import static com.sqlapk.medicine.module.patient.enums.ErrorCodeConstants.PATIENT_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link PatientsServiceImpl} 的单元测试类
 *
 * @author 管理员
 */
@Import(PatientsServiceImpl.class)
public class PatientsServiceImplTest extends BaseDbUnitTest {

    @Resource
    private PatientsServiceImpl sService;

    @Resource
    private PatientsMapper sMapper;

    @Test
    public void testCreates_success() {
        // 准备参数
        PatientsCreateReqVO reqVO = randomPojo(PatientsCreateReqVO.class);

        // 调用
        Long sId = sService.creates(reqVO);
        // 断言
        assertNotNull(sId);
        // 校验记录的属性是否正确
        PatientsDO s = sMapper.selectById(sId);
        assertPojoEquals(reqVO, s);
    }

    @Test
    public void testUpdates_success() {
        // mock 数据
        PatientsDO dbs = randomPojo(PatientsDO.class);
        sMapper.insert(dbs);// @Sql: 先插入出一条存在的数据
        // 准备参数
        PatientsUpdateReqVO reqVO = randomPojo(PatientsUpdateReqVO.class, o -> {
            o.setId(dbs.getId()); // 设置更新的 ID
        });

        // 调用
        sService.updates(reqVO);
        // 校验是否更新正确
        PatientsDO s = sMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, s);
    }

    @Test
    public void testUpdates_notExists() {
        // 准备参数
        PatientsUpdateReqVO reqVO = randomPojo(PatientsUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> sService.updates(reqVO), PATIENT_NOT_EXISTS);
    }

    @Test
    public void testDeletes_success() {
        // mock 数据
        PatientsDO dbs = randomPojo(PatientsDO.class);
        sMapper.insert(dbs);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbs.getId();

        // 调用
        sService.deletes(id);
       // 校验数据不存在了
       assertNull(sMapper.selectById(id));
    }

    @Test
    public void testDeletes_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> sService.deletes(id), PATIENT_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetsPage() {
       // mock 数据
       PatientsDO dbs = randomPojo(PatientsDO.class, o -> { // 等会查询到
           o.setUserId(null);
           o.setAddress(null);
           o.setInsuranceCardNumber(null);
           o.setAllergyHistory(null);
           o.setMedicalHistory(null);
           o.setFamilyHistory(null);
           o.setPersonalHistory(null);
           o.setCreateTime(null);
           o.setPatientType(null);
           o.setInsurance(null);
       });
       sMapper.insert(dbs);
       // 测试 userId 不匹配
       sMapper.insert(cloneIgnoreId(dbs, o -> o.setUserId(null)));
       // 测试 address 不匹配
       sMapper.insert(cloneIgnoreId(dbs, o -> o.setAddress(null)));
       // 测试 insuranceCardNumber 不匹配
       sMapper.insert(cloneIgnoreId(dbs, o -> o.setInsuranceCardNumber(null)));
       // 测试 allergyHistory 不匹配
       sMapper.insert(cloneIgnoreId(dbs, o -> o.setAllergyHistory(null)));
       // 测试 medicalHistory 不匹配
       sMapper.insert(cloneIgnoreId(dbs, o -> o.setMedicalHistory(null)));
       // 测试 familyHistory 不匹配
       sMapper.insert(cloneIgnoreId(dbs, o -> o.setFamilyHistory(null)));
       // 测试 personalHistory 不匹配
       sMapper.insert(cloneIgnoreId(dbs, o -> o.setPersonalHistory(null)));
       // 测试 createTime 不匹配
       sMapper.insert(cloneIgnoreId(dbs, o -> o.setCreateTime(null)));
       // 测试 patientType 不匹配
       sMapper.insert(cloneIgnoreId(dbs, o -> o.setPatientType(null)));
       // 测试 insurance 不匹配
       sMapper.insert(cloneIgnoreId(dbs, o -> o.setInsurance(null)));
       // 准备参数
       PatientsPageReqVO reqVO = new PatientsPageReqVO();
       reqVO.setUserId(null);
       reqVO.setAddress(null);
       reqVO.setInsuranceCardNumber(null);
       reqVO.setAllergyHistory(null);
       reqVO.setMedicalHistory(null);
       reqVO.setFamilyHistory(null);
       reqVO.setPersonalHistory(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setPatientType(null);
       reqVO.setInsurance(null);

       // 调用
       PageResult<PatientsDO> pageResult = sService.getsPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbs, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetsList() {
       // mock 数据
       PatientsDO dbs = randomPojo(PatientsDO.class, o -> { // 等会查询到
           o.setUserId(null);
           o.setAddress(null);
           o.setInsuranceCardNumber(null);
           o.setAllergyHistory(null);
           o.setMedicalHistory(null);
           o.setFamilyHistory(null);
           o.setPersonalHistory(null);
           o.setCreateTime(null);
           o.setPatientType(null);
           o.setInsurance(null);
       });
       sMapper.insert(dbs);
       // 测试 userId 不匹配
       sMapper.insert(cloneIgnoreId(dbs, o -> o.setUserId(null)));
       // 测试 address 不匹配
       sMapper.insert(cloneIgnoreId(dbs, o -> o.setAddress(null)));
       // 测试 insuranceCardNumber 不匹配
       sMapper.insert(cloneIgnoreId(dbs, o -> o.setInsuranceCardNumber(null)));
       // 测试 allergyHistory 不匹配
       sMapper.insert(cloneIgnoreId(dbs, o -> o.setAllergyHistory(null)));
       // 测试 medicalHistory 不匹配
       sMapper.insert(cloneIgnoreId(dbs, o -> o.setMedicalHistory(null)));
       // 测试 familyHistory 不匹配
       sMapper.insert(cloneIgnoreId(dbs, o -> o.setFamilyHistory(null)));
       // 测试 personalHistory 不匹配
       sMapper.insert(cloneIgnoreId(dbs, o -> o.setPersonalHistory(null)));
       // 测试 createTime 不匹配
       sMapper.insert(cloneIgnoreId(dbs, o -> o.setCreateTime(null)));
       // 测试 patientType 不匹配
       sMapper.insert(cloneIgnoreId(dbs, o -> o.setPatientType(null)));
       // 测试 insurance 不匹配
       sMapper.insert(cloneIgnoreId(dbs, o -> o.setInsurance(null)));
       // 准备参数
       PatientsExportReqVO reqVO = new PatientsExportReqVO();
       reqVO.setUserId(null);
       reqVO.setAddress(null);
       reqVO.setInsuranceCardNumber(null);
       reqVO.setAllergyHistory(null);
       reqVO.setMedicalHistory(null);
       reqVO.setFamilyHistory(null);
       reqVO.setPersonalHistory(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setPatientType(null);
       reqVO.setInsurance(null);

       // 调用
       List<PatientsDO> list = sService.getsList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbs, list.get(0));
    }

}
