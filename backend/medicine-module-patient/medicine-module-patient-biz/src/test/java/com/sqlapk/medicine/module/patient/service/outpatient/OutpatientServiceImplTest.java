package com.sqlapk.medicine.module.patient.service.outpatient;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.test.core.ut.BaseDbUnitTest;
import com.sqlapk.medicine.module.patient.controller.admin.outpatient.vo.OutpatientCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatient.vo.OutpatientExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatient.vo.OutpatientPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatient.vo.OutpatientUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.outpatient.OutpatientDO;
import com.sqlapk.medicine.module.patient.dal.mysql.outpatient.OutpatientMapper;
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
import static com.sqlapk.medicine.module.patient.enums.ErrorCodeConstants.OUTPATIENT_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link OutpatientServiceImpl} 的单元测试类
 *
 * @author 管理员
 */
@Import(OutpatientServiceImpl.class)
public class OutpatientServiceImplTest extends BaseDbUnitTest {

    @Resource
    private OutpatientServiceImpl outpatientService;

    @Resource
    private OutpatientMapper outpatientMapper;

    @Test
    public void testCreateOutpatient_success() {
        // 准备参数
        OutpatientCreateReqVO reqVO = randomPojo(OutpatientCreateReqVO.class);

        // 调用
        Long outpatientId = outpatientService.createOutpatient(reqVO);
        // 断言
        assertNotNull(outpatientId);
        // 校验记录的属性是否正确
        OutpatientDO outpatient = outpatientMapper.selectById(outpatientId);
        assertPojoEquals(reqVO, outpatient);
    }

    @Test
    public void testUpdateOutpatient_success() {
        // mock 数据
        OutpatientDO dbOutpatient = randomPojo(OutpatientDO.class);
        outpatientMapper.insert(dbOutpatient);// @Sql: 先插入出一条存在的数据
        // 准备参数
        OutpatientUpdateReqVO reqVO = randomPojo(OutpatientUpdateReqVO.class, o -> {
            o.setId(dbOutpatient.getId()); // 设置更新的 ID
        });

        // 调用
        outpatientService.updateOutpatient(reqVO);
        // 校验是否更新正确
        OutpatientDO outpatient = outpatientMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, outpatient);
    }

    @Test
    public void testUpdateOutpatient_notExists() {
        // 准备参数
        OutpatientUpdateReqVO reqVO = randomPojo(OutpatientUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> outpatientService.updateOutpatient(reqVO), OUTPATIENT_NOT_EXISTS);
    }

    @Test
    public void testDeleteOutpatient_success() {
        // mock 数据
        OutpatientDO dbOutpatient = randomPojo(OutpatientDO.class);
        outpatientMapper.insert(dbOutpatient);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbOutpatient.getId();

        // 调用
        outpatientService.deleteOutpatient(id);
       // 校验数据不存在了
       assertNull(outpatientMapper.selectById(id));
    }

    @Test
    public void testDeleteOutpatient_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> outpatientService.deleteOutpatient(id), OUTPATIENT_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetOutpatientPage() {
       // mock 数据
       OutpatientDO dbOutpatient = randomPojo(OutpatientDO.class, o -> { // 等会查询到
           o.setDoctorId(null);
           o.setPatientId(null);
           o.setAllergyHistory(null);
           o.setMedicalHistory(null);
           o.setFamilyHistory(null);
           o.setPersonalHistory(null);
           o.setComment(null);
           o.setCreateTime(null);
       });
       outpatientMapper.insert(dbOutpatient);
       // 测试 doctorId 不匹配
       outpatientMapper.insert(cloneIgnoreId(dbOutpatient, o -> o.setDoctorId(null)));
       // 测试 patientId 不匹配
       outpatientMapper.insert(cloneIgnoreId(dbOutpatient, o -> o.setPatientId(null)));
       // 测试 allergyHistory 不匹配
       outpatientMapper.insert(cloneIgnoreId(dbOutpatient, o -> o.setAllergyHistory(null)));
       // 测试 medicalHistory 不匹配
       outpatientMapper.insert(cloneIgnoreId(dbOutpatient, o -> o.setMedicalHistory(null)));
       // 测试 familyHistory 不匹配
       outpatientMapper.insert(cloneIgnoreId(dbOutpatient, o -> o.setFamilyHistory(null)));
       // 测试 personalHistory 不匹配
       outpatientMapper.insert(cloneIgnoreId(dbOutpatient, o -> o.setPersonalHistory(null)));
       // 测试 comment 不匹配
       outpatientMapper.insert(cloneIgnoreId(dbOutpatient, o -> o.setComment(null)));
       // 测试 createTime 不匹配
       outpatientMapper.insert(cloneIgnoreId(dbOutpatient, o -> o.setCreateTime(null)));
       // 准备参数
       OutpatientPageReqVO reqVO = new OutpatientPageReqVO();
       reqVO.setDoctorId(null);
       reqVO.setPatientId(null);
       reqVO.setAllergyHistory(null);
       reqVO.setMedicalHistory(null);
       reqVO.setFamilyHistory(null);
       reqVO.setPersonalHistory(null);
       reqVO.setComment(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<OutpatientDO> pageResult = outpatientService.getOutpatientPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbOutpatient, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetOutpatientList() {
       // mock 数据
       OutpatientDO dbOutpatient = randomPojo(OutpatientDO.class, o -> { // 等会查询到
           o.setDoctorId(null);
           o.setPatientId(null);
           o.setAllergyHistory(null);
           o.setMedicalHistory(null);
           o.setFamilyHistory(null);
           o.setPersonalHistory(null);
           o.setComment(null);
           o.setCreateTime(null);
       });
       outpatientMapper.insert(dbOutpatient);
       // 测试 doctorId 不匹配
       outpatientMapper.insert(cloneIgnoreId(dbOutpatient, o -> o.setDoctorId(null)));
       // 测试 patientId 不匹配
       outpatientMapper.insert(cloneIgnoreId(dbOutpatient, o -> o.setPatientId(null)));
       // 测试 allergyHistory 不匹配
       outpatientMapper.insert(cloneIgnoreId(dbOutpatient, o -> o.setAllergyHistory(null)));
       // 测试 medicalHistory 不匹配
       outpatientMapper.insert(cloneIgnoreId(dbOutpatient, o -> o.setMedicalHistory(null)));
       // 测试 familyHistory 不匹配
       outpatientMapper.insert(cloneIgnoreId(dbOutpatient, o -> o.setFamilyHistory(null)));
       // 测试 personalHistory 不匹配
       outpatientMapper.insert(cloneIgnoreId(dbOutpatient, o -> o.setPersonalHistory(null)));
       // 测试 comment 不匹配
       outpatientMapper.insert(cloneIgnoreId(dbOutpatient, o -> o.setComment(null)));
       // 测试 createTime 不匹配
       outpatientMapper.insert(cloneIgnoreId(dbOutpatient, o -> o.setCreateTime(null)));
       // 准备参数
       OutpatientExportReqVO reqVO = new OutpatientExportReqVO();
       reqVO.setDoctorId(null);
       reqVO.setPatientId(null);
       reqVO.setAllergyHistory(null);
       reqVO.setMedicalHistory(null);
       reqVO.setFamilyHistory(null);
       reqVO.setPersonalHistory(null);
       reqVO.setComment(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       List<OutpatientDO> list = outpatientService.getOutpatientList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbOutpatient, list.get(0));
    }

}
