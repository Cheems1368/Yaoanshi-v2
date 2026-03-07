package com.sqlapk.medicine.module.patient.service.inpatienteducation;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.test.core.ut.BaseDbUnitTest;
import com.sqlapk.medicine.module.patient.controller.admin.inpatienteducation.vo.InpatientEducationCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatienteducation.vo.InpatientEducationExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatienteducation.vo.InpatientEducationPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatienteducation.vo.InpatientEducationUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.inpatienteducation.InpatientEducationDO;
import com.sqlapk.medicine.module.patient.dal.mysql.inpatienteducation.InpatientEducationMapper;
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
import static com.sqlapk.medicine.module.patient.enums.ErrorCodeConstants.INPATIENT_EDUCATION_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link InpatientEducationServiceImpl} 的单元测试类
 *
 * @author 管理员
 */
@Import(InpatientEducationServiceImpl.class)
public class InpatientEducationServiceImplTest extends BaseDbUnitTest {

    @Resource
    private InpatientEducationServiceImpl inpatientEducationService;

    @Resource
    private InpatientEducationMapper inpatientEducationMapper;

    @Test
    public void testCreateInpatientEducation_success() {
        // 准备参数
        InpatientEducationCreateReqVO reqVO = randomPojo(InpatientEducationCreateReqVO.class);

        // 调用
        Long inpatientEducationId = inpatientEducationService.createInpatientEducation(reqVO);
        // 断言
        assertNotNull(inpatientEducationId);
        // 校验记录的属性是否正确
        InpatientEducationDO inpatientEducation = inpatientEducationMapper.selectById(inpatientEducationId);
        assertPojoEquals(reqVO, inpatientEducation);
    }

    @Test
    public void testUpdateInpatientEducation_success() {
        // mock 数据
        InpatientEducationDO dbInpatientEducation = randomPojo(InpatientEducationDO.class);
        inpatientEducationMapper.insert(dbInpatientEducation);// @Sql: 先插入出一条存在的数据
        // 准备参数
        InpatientEducationUpdateReqVO reqVO = randomPojo(InpatientEducationUpdateReqVO.class, o -> {
            o.setId(dbInpatientEducation.getId()); // 设置更新的 ID
        });

        // 调用
        inpatientEducationService.updateInpatientEducation(reqVO);
        // 校验是否更新正确
        InpatientEducationDO inpatientEducation = inpatientEducationMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, inpatientEducation);
    }

    @Test
    public void testUpdateInpatientEducation_notExists() {
        // 准备参数
        InpatientEducationUpdateReqVO reqVO = randomPojo(InpatientEducationUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> inpatientEducationService.updateInpatientEducation(reqVO), INPATIENT_EDUCATION_NOT_EXISTS);
    }

    @Test
    public void testDeleteInpatientEducation_success() {
        // mock 数据
        InpatientEducationDO dbInpatientEducation = randomPojo(InpatientEducationDO.class);
        inpatientEducationMapper.insert(dbInpatientEducation);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbInpatientEducation.getId();

        // 调用
        inpatientEducationService.deleteInpatientEducation(id);
       // 校验数据不存在了
       assertNull(inpatientEducationMapper.selectById(id));
    }

    @Test
    public void testDeleteInpatientEducation_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> inpatientEducationService.deleteInpatientEducation(id), INPATIENT_EDUCATION_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetInpatientEducationPage() {
       // mock 数据
       InpatientEducationDO dbInpatientEducation = randomPojo(InpatientEducationDO.class, o -> { // 等会查询到
           o.setInpatientId(null);
           o.setEducationDate(null);
           o.setDiagnosisAndMedication(null);
           o.setKeyPoints(null);
           o.setCreateTime(null);
       });
       inpatientEducationMapper.insert(dbInpatientEducation);
       // 测试 inpatientId 不匹配
       inpatientEducationMapper.insert(cloneIgnoreId(dbInpatientEducation, o -> o.setInpatientId(null)));
       // 测试 educationDate 不匹配
       inpatientEducationMapper.insert(cloneIgnoreId(dbInpatientEducation, o -> o.setEducationDate(null)));
       // 测试 diagnosisAndMedication 不匹配
       inpatientEducationMapper.insert(cloneIgnoreId(dbInpatientEducation, o -> o.setDiagnosisAndMedication(null)));
       // 测试 keyPoints 不匹配
       inpatientEducationMapper.insert(cloneIgnoreId(dbInpatientEducation, o -> o.setKeyPoints(null)));
       // 测试 createTime 不匹配
       inpatientEducationMapper.insert(cloneIgnoreId(dbInpatientEducation, o -> o.setCreateTime(null)));
       // 准备参数
       InpatientEducationPageReqVO reqVO = new InpatientEducationPageReqVO();
       reqVO.setInpatientId(null);
       reqVO.setEducationDate(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setDiagnosisAndMedication(null);
       reqVO.setKeyPoints(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<InpatientEducationDO> pageResult = inpatientEducationService.getInpatientEducationPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbInpatientEducation, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetInpatientEducationList() {
       // mock 数据
       InpatientEducationDO dbInpatientEducation = randomPojo(InpatientEducationDO.class, o -> { // 等会查询到
           o.setInpatientId(null);
           o.setEducationDate(null);
           o.setDiagnosisAndMedication(null);
           o.setKeyPoints(null);
           o.setCreateTime(null);
       });
       inpatientEducationMapper.insert(dbInpatientEducation);
       // 测试 inpatientId 不匹配
       inpatientEducationMapper.insert(cloneIgnoreId(dbInpatientEducation, o -> o.setInpatientId(null)));
       // 测试 educationDate 不匹配
       inpatientEducationMapper.insert(cloneIgnoreId(dbInpatientEducation, o -> o.setEducationDate(null)));
       // 测试 diagnosisAndMedication 不匹配
       inpatientEducationMapper.insert(cloneIgnoreId(dbInpatientEducation, o -> o.setDiagnosisAndMedication(null)));
       // 测试 keyPoints 不匹配
       inpatientEducationMapper.insert(cloneIgnoreId(dbInpatientEducation, o -> o.setKeyPoints(null)));
       // 测试 createTime 不匹配
       inpatientEducationMapper.insert(cloneIgnoreId(dbInpatientEducation, o -> o.setCreateTime(null)));
       // 准备参数
       InpatientEducationExportReqVO reqVO = new InpatientEducationExportReqVO();
       reqVO.setInpatientId(null);
       reqVO.setEducationDate(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setDiagnosisAndMedication(null);
       reqVO.setKeyPoints(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       List<InpatientEducationDO> list = inpatientEducationService.getInpatientEducationList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbInpatientEducation, list.get(0));
    }

}
