package com.sqlapk.medicine.module.patient.service.prescriptions;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.test.core.ut.BaseDbUnitTest;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptions.vo.PrescriptionsCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptions.vo.PrescriptionsExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptions.vo.PrescriptionsPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptions.vo.PrescriptionsUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.prescriptions.PrescriptionsDO;
import com.sqlapk.medicine.module.patient.dal.mysql.prescriptions.PrescriptionsMapper;
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
import static com.sqlapk.medicine.module.patient.enums.ErrorCodeConstants.PRESCRIPTIONS_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link PrescriptionsServiceImpl} 的单元测试类
 *
 * @author 管理员
 */
@Import(PrescriptionsServiceImpl.class)
public class PrescriptionsServiceImplTest extends BaseDbUnitTest {

    @Resource
    private PrescriptionsServiceImpl prescriptionsService;

    @Resource
    private PrescriptionsMapper prescriptionsMapper;

    @Test
    public void testCreatePrescriptions_success() {
        // 准备参数
        PrescriptionsCreateReqVO reqVO = randomPojo(PrescriptionsCreateReqVO.class);

        // 调用
        Long prescriptionsId = prescriptionsService.createPrescriptions(reqVO);
        // 断言
        assertNotNull(prescriptionsId);
        // 校验记录的属性是否正确
        PrescriptionsDO prescriptions = prescriptionsMapper.selectById(prescriptionsId);
        assertPojoEquals(reqVO, prescriptions);
    }

    @Test
    public void testUpdatePrescriptions_success() {
        // mock 数据
        PrescriptionsDO dbPrescriptions = randomPojo(PrescriptionsDO.class);
        prescriptionsMapper.insert(dbPrescriptions);// @Sql: 先插入出一条存在的数据
        // 准备参数
        PrescriptionsUpdateReqVO reqVO = randomPojo(PrescriptionsUpdateReqVO.class, o -> {
            o.setId(dbPrescriptions.getId()); // 设置更新的 ID
        });

        // 调用
        prescriptionsService.updatePrescriptions(reqVO);
        // 校验是否更新正确
        PrescriptionsDO prescriptions = prescriptionsMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, prescriptions);
    }

    @Test
    public void testUpdatePrescriptions_notExists() {
        // 准备参数
        PrescriptionsUpdateReqVO reqVO = randomPojo(PrescriptionsUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> prescriptionsService.updatePrescriptions(reqVO), PRESCRIPTIONS_NOT_EXISTS);
    }

    @Test
    public void testDeletePrescriptions_success() {
        // mock 数据
        PrescriptionsDO dbPrescriptions = randomPojo(PrescriptionsDO.class);
        prescriptionsMapper.insert(dbPrescriptions);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbPrescriptions.getId();

        // 调用
        prescriptionsService.deletePrescriptions(id);
       // 校验数据不存在了
       assertNull(prescriptionsMapper.selectById(id));
    }

    @Test
    public void testDeletePrescriptions_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> prescriptionsService.deletePrescriptions(id), PRESCRIPTIONS_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetPrescriptionsPage() {
       // mock 数据
       PrescriptionsDO dbPrescriptions = randomPojo(PrescriptionsDO.class, o -> { // 等会查询到
           o.setPatientId(null);
           o.setDoctorId(null);
           o.setCreateTime(null);
           o.setType(null);
       });
       prescriptionsMapper.insert(dbPrescriptions);
       // 测试 patientId 不匹配
       prescriptionsMapper.insert(cloneIgnoreId(dbPrescriptions, o -> o.setPatientId(null)));
       // 测试 doctorId 不匹配
       prescriptionsMapper.insert(cloneIgnoreId(dbPrescriptions, o -> o.setDoctorId(null)));
       // 测试 createTime 不匹配
       prescriptionsMapper.insert(cloneIgnoreId(dbPrescriptions, o -> o.setCreateTime(null)));
       // 测试 type 不匹配
       prescriptionsMapper.insert(cloneIgnoreId(dbPrescriptions, o -> o.setType(null)));
       // 准备参数
       PrescriptionsPageReqVO reqVO = new PrescriptionsPageReqVO();
       reqVO.setPatientId(null);
       reqVO.setDoctorId(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setType(null);

       // 调用
       PageResult<PrescriptionsDO> pageResult = prescriptionsService.getPrescriptionsPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbPrescriptions, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetPrescriptionsList() {
       // mock 数据
       PrescriptionsDO dbPrescriptions = randomPojo(PrescriptionsDO.class, o -> { // 等会查询到
           o.setPatientId(null);
           o.setDoctorId(null);
           o.setCreateTime(null);
           o.setType(null);
       });
       prescriptionsMapper.insert(dbPrescriptions);
       // 测试 patientId 不匹配
       prescriptionsMapper.insert(cloneIgnoreId(dbPrescriptions, o -> o.setPatientId(null)));
       // 测试 doctorId 不匹配
       prescriptionsMapper.insert(cloneIgnoreId(dbPrescriptions, o -> o.setDoctorId(null)));
       // 测试 createTime 不匹配
       prescriptionsMapper.insert(cloneIgnoreId(dbPrescriptions, o -> o.setCreateTime(null)));
       // 测试 type 不匹配
       prescriptionsMapper.insert(cloneIgnoreId(dbPrescriptions, o -> o.setType(null)));
       // 准备参数
       PrescriptionsExportReqVO reqVO = new PrescriptionsExportReqVO();
       reqVO.setPatientId(null);
       reqVO.setDoctorId(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setType(null);

       // 调用
       List<PrescriptionsDO> list = prescriptionsService.getPrescriptionsList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbPrescriptions, list.get(0));
    }

}
