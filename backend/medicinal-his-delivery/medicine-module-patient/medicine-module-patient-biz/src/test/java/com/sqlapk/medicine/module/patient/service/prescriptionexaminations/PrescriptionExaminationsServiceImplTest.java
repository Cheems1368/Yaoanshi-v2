package com.sqlapk.medicine.module.patient.service.prescriptionexaminations;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.test.core.ut.BaseDbUnitTest;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptionexaminations.vo.PrescriptionExaminationsCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptionexaminations.vo.PrescriptionExaminationsExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptionexaminations.vo.PrescriptionExaminationsPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptionexaminations.vo.PrescriptionExaminationsUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.prescriptionexaminations.PrescriptionExaminationsDO;
import com.sqlapk.medicine.module.patient.dal.mysql.prescriptionexaminations.PrescriptionExaminationsMapper;
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
import static com.sqlapk.medicine.module.patient.enums.ErrorCodeConstants.PRESCRIPTION_EXAMINATIONS_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link PrescriptionExaminationsServiceImpl} 的单元测试类
 *
 * @author 管理员
 */
@Import(PrescriptionExaminationsServiceImpl.class)
public class PrescriptionExaminationsServiceImplTest extends BaseDbUnitTest {

    @Resource
    private PrescriptionExaminationsServiceImpl prescriptionExaminationsService;

    @Resource
    private PrescriptionExaminationsMapper prescriptionExaminationsMapper;

    @Test
    public void testCreatePrescriptionExaminations_success() {
        // 准备参数
        PrescriptionExaminationsCreateReqVO reqVO = randomPojo(PrescriptionExaminationsCreateReqVO.class);

        // 调用
        Long prescriptionExaminationsId = prescriptionExaminationsService.createPrescriptionExaminations(reqVO);
        // 断言
        assertNotNull(prescriptionExaminationsId);
        // 校验记录的属性是否正确
        PrescriptionExaminationsDO prescriptionExaminations = prescriptionExaminationsMapper.selectById(prescriptionExaminationsId);
        assertPojoEquals(reqVO, prescriptionExaminations);
    }

    @Test
    public void testUpdatePrescriptionExaminations_success() {
        // mock 数据
        PrescriptionExaminationsDO dbPrescriptionExaminations = randomPojo(PrescriptionExaminationsDO.class);
        prescriptionExaminationsMapper.insert(dbPrescriptionExaminations);// @Sql: 先插入出一条存在的数据
        // 准备参数
        PrescriptionExaminationsUpdateReqVO reqVO = randomPojo(PrescriptionExaminationsUpdateReqVO.class, o -> {
            o.setId(dbPrescriptionExaminations.getId()); // 设置更新的 ID
        });

        // 调用
        prescriptionExaminationsService.updatePrescriptionExaminations(reqVO);
        // 校验是否更新正确
        PrescriptionExaminationsDO prescriptionExaminations = prescriptionExaminationsMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, prescriptionExaminations);
    }

    @Test
    public void testUpdatePrescriptionExaminations_notExists() {
        // 准备参数
        PrescriptionExaminationsUpdateReqVO reqVO = randomPojo(PrescriptionExaminationsUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> prescriptionExaminationsService.updatePrescriptionExaminations(reqVO), PRESCRIPTION_EXAMINATIONS_NOT_EXISTS);
    }

    @Test
    public void testDeletePrescriptionExaminations_success() {
        // mock 数据
        PrescriptionExaminationsDO dbPrescriptionExaminations = randomPojo(PrescriptionExaminationsDO.class);
        prescriptionExaminationsMapper.insert(dbPrescriptionExaminations);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbPrescriptionExaminations.getId();

        // 调用
        prescriptionExaminationsService.deletePrescriptionExaminations(id);
       // 校验数据不存在了
       assertNull(prescriptionExaminationsMapper.selectById(id));
    }

    @Test
    public void testDeletePrescriptionExaminations_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> prescriptionExaminationsService.deletePrescriptionExaminations(id), PRESCRIPTION_EXAMINATIONS_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetPrescriptionExaminationsPage() {
       // mock 数据
       PrescriptionExaminationsDO dbPrescriptionExaminations = randomPojo(PrescriptionExaminationsDO.class, o -> { // 等会查询到
           o.setPrescriptionId(null);
           o.setExaminationId(null);
           o.setResult(null);
           o.setCreateTime(null);
       });
       prescriptionExaminationsMapper.insert(dbPrescriptionExaminations);
       // 测试 prescriptionId 不匹配
       prescriptionExaminationsMapper.insert(cloneIgnoreId(dbPrescriptionExaminations, o -> o.setPrescriptionId(null)));
       // 测试 examinationId 不匹配
       prescriptionExaminationsMapper.insert(cloneIgnoreId(dbPrescriptionExaminations, o -> o.setExaminationId(null)));
       // 测试 result 不匹配
       prescriptionExaminationsMapper.insert(cloneIgnoreId(dbPrescriptionExaminations, o -> o.setResult(null)));
       // 测试 createTime 不匹配
       prescriptionExaminationsMapper.insert(cloneIgnoreId(dbPrescriptionExaminations, o -> o.setCreateTime(null)));
       // 准备参数
       PrescriptionExaminationsPageReqVO reqVO = new PrescriptionExaminationsPageReqVO();
       reqVO.setPrescriptionId(null);
       reqVO.setExaminationId(null);
       reqVO.setResult(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<PrescriptionExaminationsDO> pageResult = prescriptionExaminationsService.getPrescriptionExaminationsPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbPrescriptionExaminations, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetPrescriptionExaminationsList() {
       // mock 数据
       PrescriptionExaminationsDO dbPrescriptionExaminations = randomPojo(PrescriptionExaminationsDO.class, o -> { // 等会查询到
           o.setPrescriptionId(null);
           o.setExaminationId(null);
           o.setResult(null);
           o.setCreateTime(null);
       });
       prescriptionExaminationsMapper.insert(dbPrescriptionExaminations);
       // 测试 prescriptionId 不匹配
       prescriptionExaminationsMapper.insert(cloneIgnoreId(dbPrescriptionExaminations, o -> o.setPrescriptionId(null)));
       // 测试 examinationId 不匹配
       prescriptionExaminationsMapper.insert(cloneIgnoreId(dbPrescriptionExaminations, o -> o.setExaminationId(null)));
       // 测试 result 不匹配
       prescriptionExaminationsMapper.insert(cloneIgnoreId(dbPrescriptionExaminations, o -> o.setResult(null)));
       // 测试 createTime 不匹配
       prescriptionExaminationsMapper.insert(cloneIgnoreId(dbPrescriptionExaminations, o -> o.setCreateTime(null)));
       // 准备参数
       PrescriptionExaminationsExportReqVO reqVO = new PrescriptionExaminationsExportReqVO();
       reqVO.setPrescriptionId(null);
       reqVO.setExaminationId(null);
       reqVO.setResult(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       List<PrescriptionExaminationsDO> list = prescriptionExaminationsService.getPrescriptionExaminationsList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbPrescriptionExaminations, list.get(0));
    }

}
