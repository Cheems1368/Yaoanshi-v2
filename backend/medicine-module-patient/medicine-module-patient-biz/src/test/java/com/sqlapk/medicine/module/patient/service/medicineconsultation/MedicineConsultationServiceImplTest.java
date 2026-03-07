package com.sqlapk.medicine.module.patient.service.medicineconsultation;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.test.core.ut.BaseDbUnitTest;
import com.sqlapk.medicine.module.patient.controller.admin.medicineconsultation.vo.MedicineConsultationCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.medicineconsultation.vo.MedicineConsultationExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.medicineconsultation.vo.MedicineConsultationPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.medicineconsultation.vo.MedicineConsultationUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.medicineconsultation.MedicineConsultationDO;
import com.sqlapk.medicine.module.patient.dal.mysql.medicineconsultation.MedicineConsultationMapper;
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
import static com.sqlapk.medicine.module.patient.enums.ErrorCodeConstants.MEDICINE_CONSULTATION_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link MedicineConsultationServiceImpl} 的单元测试类
 *
 * @author 管理员
 */
@Import(MedicineConsultationServiceImpl.class)
public class MedicineConsultationServiceImplTest extends BaseDbUnitTest {

    @Resource
    private MedicineConsultationServiceImpl medicineConsultationService;

    @Resource
    private MedicineConsultationMapper medicineConsultationMapper;

    @Test
    public void testCreateMedicineConsultation_success() {
        // 准备参数
        MedicineConsultationCreateReqVO reqVO = randomPojo(MedicineConsultationCreateReqVO.class);

        // 调用
        Long medicineConsultationId = medicineConsultationService.createMedicineConsultation(reqVO);
        // 断言
        assertNotNull(medicineConsultationId);
        // 校验记录的属性是否正确
        MedicineConsultationDO medicineConsultation = medicineConsultationMapper.selectById(medicineConsultationId);
        assertPojoEquals(reqVO, medicineConsultation);
    }

    @Test
    public void testUpdateMedicineConsultation_success() {
        // mock 数据
        MedicineConsultationDO dbMedicineConsultation = randomPojo(MedicineConsultationDO.class);
        medicineConsultationMapper.insert(dbMedicineConsultation);// @Sql: 先插入出一条存在的数据
        // 准备参数
        MedicineConsultationUpdateReqVO reqVO = randomPojo(MedicineConsultationUpdateReqVO.class, o -> {
            o.setId(dbMedicineConsultation.getId()); // 设置更新的 ID
        });

        // 调用
        medicineConsultationService.updateMedicineConsultation(reqVO);
        // 校验是否更新正确
        MedicineConsultationDO medicineConsultation = medicineConsultationMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, medicineConsultation);
    }

    @Test
    public void testUpdateMedicineConsultation_notExists() {
        // 准备参数
        MedicineConsultationUpdateReqVO reqVO = randomPojo(MedicineConsultationUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> medicineConsultationService.updateMedicineConsultation(reqVO), MEDICINE_CONSULTATION_NOT_EXISTS);
    }

    @Test
    public void testDeleteMedicineConsultation_success() {
        // mock 数据
        MedicineConsultationDO dbMedicineConsultation = randomPojo(MedicineConsultationDO.class);
        medicineConsultationMapper.insert(dbMedicineConsultation);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbMedicineConsultation.getId();

        // 调用
        medicineConsultationService.deleteMedicineConsultation(id);
       // 校验数据不存在了
       assertNull(medicineConsultationMapper.selectById(id));
    }

    @Test
    public void testDeleteMedicineConsultation_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> medicineConsultationService.deleteMedicineConsultation(id), MEDICINE_CONSULTATION_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetMedicineConsultationPage() {
       // mock 数据
       MedicineConsultationDO dbMedicineConsultation = randomPojo(MedicineConsultationDO.class, o -> { // 等会查询到
           o.setPatientId(null);
           o.setQuestion(null);
           o.setAnswer(null);
           o.setConsultObject(null);
           o.setSpecialPatient(null);
           o.setType(null);
           o.setDuration(null);
           o.setDoctorId(null);
           o.setAnswerBasis(null);
           o.setComment(null);
           o.setCreateTime(null);
           o.setMedicineId(null);
       });
       medicineConsultationMapper.insert(dbMedicineConsultation);
       // 测试 patientId 不匹配
       medicineConsultationMapper.insert(cloneIgnoreId(dbMedicineConsultation, o -> o.setPatientId(null)));
       // 测试 question 不匹配
       medicineConsultationMapper.insert(cloneIgnoreId(dbMedicineConsultation, o -> o.setQuestion(null)));
       // 测试 answer 不匹配
       medicineConsultationMapper.insert(cloneIgnoreId(dbMedicineConsultation, o -> o.setAnswer(null)));
       // 测试 consultObject 不匹配
       medicineConsultationMapper.insert(cloneIgnoreId(dbMedicineConsultation, o -> o.setConsultObject(null)));
       // 测试 specialPatient 不匹配
       medicineConsultationMapper.insert(cloneIgnoreId(dbMedicineConsultation, o -> o.setSpecialPatient(null)));
       // 测试 type 不匹配
       medicineConsultationMapper.insert(cloneIgnoreId(dbMedicineConsultation, o -> o.setType(null)));
       // 测试 duration 不匹配
       medicineConsultationMapper.insert(cloneIgnoreId(dbMedicineConsultation, o -> o.setDuration(null)));
       // 测试 doctorId 不匹配
       medicineConsultationMapper.insert(cloneIgnoreId(dbMedicineConsultation, o -> o.setDoctorId(null)));
       // 测试 answerBasis 不匹配
       medicineConsultationMapper.insert(cloneIgnoreId(dbMedicineConsultation, o -> o.setAnswerBasis(null)));
       // 测试 comment 不匹配
       medicineConsultationMapper.insert(cloneIgnoreId(dbMedicineConsultation, o -> o.setComment(null)));
       // 测试 createTime 不匹配
       medicineConsultationMapper.insert(cloneIgnoreId(dbMedicineConsultation, o -> o.setCreateTime(null)));
       // 测试 medicineId 不匹配
       medicineConsultationMapper.insert(cloneIgnoreId(dbMedicineConsultation, o -> o.setMedicineId(null)));
       // 准备参数
       MedicineConsultationPageReqVO reqVO = new MedicineConsultationPageReqVO();
       reqVO.setPatientId(null);
       reqVO.setQuestion(null);
       reqVO.setAnswer(null);
       reqVO.setConsultObject(null);
       reqVO.setSpecialPatient(null);
       reqVO.setType(null);
       reqVO.setDuration(null);
       reqVO.setDoctorId(null);
       reqVO.setAnswerBasis(null);
       reqVO.setComment(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setMedicineId(null);

       // 调用
       PageResult<MedicineConsultationDO> pageResult = medicineConsultationService.getMedicineConsultationPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbMedicineConsultation, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetMedicineConsultationList() {
       // mock 数据
       MedicineConsultationDO dbMedicineConsultation = randomPojo(MedicineConsultationDO.class, o -> { // 等会查询到
           o.setPatientId(null);
           o.setQuestion(null);
           o.setAnswer(null);
           o.setConsultObject(null);
           o.setSpecialPatient(null);
           o.setType(null);
           o.setDuration(null);
           o.setDoctorId(null);
           o.setAnswerBasis(null);
           o.setComment(null);
           o.setCreateTime(null);
           o.setMedicineId(null);
       });
       medicineConsultationMapper.insert(dbMedicineConsultation);
       // 测试 patientId 不匹配
       medicineConsultationMapper.insert(cloneIgnoreId(dbMedicineConsultation, o -> o.setPatientId(null)));
       // 测试 question 不匹配
       medicineConsultationMapper.insert(cloneIgnoreId(dbMedicineConsultation, o -> o.setQuestion(null)));
       // 测试 answer 不匹配
       medicineConsultationMapper.insert(cloneIgnoreId(dbMedicineConsultation, o -> o.setAnswer(null)));
       // 测试 consultObject 不匹配
       medicineConsultationMapper.insert(cloneIgnoreId(dbMedicineConsultation, o -> o.setConsultObject(null)));
       // 测试 specialPatient 不匹配
       medicineConsultationMapper.insert(cloneIgnoreId(dbMedicineConsultation, o -> o.setSpecialPatient(null)));
       // 测试 type 不匹配
       medicineConsultationMapper.insert(cloneIgnoreId(dbMedicineConsultation, o -> o.setType(null)));
       // 测试 duration 不匹配
       medicineConsultationMapper.insert(cloneIgnoreId(dbMedicineConsultation, o -> o.setDuration(null)));
       // 测试 doctorId 不匹配
       medicineConsultationMapper.insert(cloneIgnoreId(dbMedicineConsultation, o -> o.setDoctorId(null)));
       // 测试 answerBasis 不匹配
       medicineConsultationMapper.insert(cloneIgnoreId(dbMedicineConsultation, o -> o.setAnswerBasis(null)));
       // 测试 comment 不匹配
       medicineConsultationMapper.insert(cloneIgnoreId(dbMedicineConsultation, o -> o.setComment(null)));
       // 测试 createTime 不匹配
       medicineConsultationMapper.insert(cloneIgnoreId(dbMedicineConsultation, o -> o.setCreateTime(null)));
       // 测试 medicineId 不匹配
       medicineConsultationMapper.insert(cloneIgnoreId(dbMedicineConsultation, o -> o.setMedicineId(null)));
       // 准备参数
       MedicineConsultationExportReqVO reqVO = new MedicineConsultationExportReqVO();
       reqVO.setPatientId(null);
       reqVO.setQuestion(null);
       reqVO.setAnswer(null);
       reqVO.setConsultObject(null);
       reqVO.setSpecialPatient(null);
       reqVO.setType(null);
       reqVO.setDuration(null);
       reqVO.setDoctorId(null);
       reqVO.setAnswerBasis(null);
       reqVO.setComment(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setMedicineId(null);

       // 调用
       List<MedicineConsultationDO> list = medicineConsultationService.getMedicineConsultationList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbMedicineConsultation, list.get(0));
    }

}
