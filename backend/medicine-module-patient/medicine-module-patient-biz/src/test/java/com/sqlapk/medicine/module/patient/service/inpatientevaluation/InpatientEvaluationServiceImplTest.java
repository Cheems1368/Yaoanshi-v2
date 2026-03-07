package com.sqlapk.medicine.module.patient.service.inpatientevaluation;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.test.core.ut.BaseDbUnitTest;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientevaluation.vo.InpatientEvaluationCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientevaluation.vo.InpatientEvaluationExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientevaluation.vo.InpatientEvaluationPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientevaluation.vo.InpatientEvaluationUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.inpatientevaluation.InpatientEvaluationDO;
import com.sqlapk.medicine.module.patient.dal.mysql.inpatientevaluation.InpatientEvaluationMapper;
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
import static com.sqlapk.medicine.module.patient.enums.ErrorCodeConstants.INPATIENT_EVALUATION_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link InpatientEvaluationServiceImpl} 的单元测试类
 *
 * @author 管理员
 */
@Import(InpatientEvaluationServiceImpl.class)
public class InpatientEvaluationServiceImplTest extends BaseDbUnitTest {

    @Resource
    private InpatientEvaluationServiceImpl inpatientEvaluationService;

    @Resource
    private InpatientEvaluationMapper inpatientEvaluationMapper;

    @Test
    public void testCreateInpatientEvaluation_success() {
        // 准备参数
        InpatientEvaluationCreateReqVO reqVO = randomPojo(InpatientEvaluationCreateReqVO.class);

        // 调用
        Long inpatientEvaluationId = inpatientEvaluationService.createInpatientEvaluation(reqVO);
        // 断言
        assertNotNull(inpatientEvaluationId);
        // 校验记录的属性是否正确
        InpatientEvaluationDO inpatientEvaluation = inpatientEvaluationMapper.selectById(inpatientEvaluationId);
        assertPojoEquals(reqVO, inpatientEvaluation);
    }

    @Test
    public void testUpdateInpatientEvaluation_success() {
        // mock 数据
        InpatientEvaluationDO dbInpatientEvaluation = randomPojo(InpatientEvaluationDO.class);
        inpatientEvaluationMapper.insert(dbInpatientEvaluation);// @Sql: 先插入出一条存在的数据
        // 准备参数
        InpatientEvaluationUpdateReqVO reqVO = randomPojo(InpatientEvaluationUpdateReqVO.class, o -> {
            o.setId(dbInpatientEvaluation.getId()); // 设置更新的 ID
        });

        // 调用
        inpatientEvaluationService.updateInpatientEvaluation(reqVO);
        // 校验是否更新正确
        InpatientEvaluationDO inpatientEvaluation = inpatientEvaluationMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, inpatientEvaluation);
    }

    @Test
    public void testUpdateInpatientEvaluation_notExists() {
        // 准备参数
        InpatientEvaluationUpdateReqVO reqVO = randomPojo(InpatientEvaluationUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> inpatientEvaluationService.updateInpatientEvaluation(reqVO), INPATIENT_EVALUATION_NOT_EXISTS);
    }

    @Test
    public void testDeleteInpatientEvaluation_success() {
        // mock 数据
        InpatientEvaluationDO dbInpatientEvaluation = randomPojo(InpatientEvaluationDO.class);
        inpatientEvaluationMapper.insert(dbInpatientEvaluation);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbInpatientEvaluation.getId();

        // 调用
        inpatientEvaluationService.deleteInpatientEvaluation(id);
       // 校验数据不存在了
       assertNull(inpatientEvaluationMapper.selectById(id));
    }

    @Test
    public void testDeleteInpatientEvaluation_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> inpatientEvaluationService.deleteInpatientEvaluation(id), INPATIENT_EVALUATION_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetInpatientEvaluationPage() {
       // mock 数据
       InpatientEvaluationDO dbInpatientEvaluation = randomPojo(InpatientEvaluationDO.class, o -> { // 等会查询到
           o.setInpatientId(null);
           o.setEvaluationDate(null);
           o.setTargetDrug(null);
           o.setSymptom(null);
           o.setResult(null);
           o.setMedicationSuggestion(null);
           o.setCreateTime(null);
       });
       inpatientEvaluationMapper.insert(dbInpatientEvaluation);
       // 测试 inpatientId 不匹配
       inpatientEvaluationMapper.insert(cloneIgnoreId(dbInpatientEvaluation, o -> o.setInpatientId(null)));
       // 测试 evaluationDate 不匹配
       inpatientEvaluationMapper.insert(cloneIgnoreId(dbInpatientEvaluation, o -> o.setEvaluationDate(null)));
       // 测试 targetDrug 不匹配
       inpatientEvaluationMapper.insert(cloneIgnoreId(dbInpatientEvaluation, o -> o.setTargetDrug(null)));
       // 测试 symptom 不匹配
       inpatientEvaluationMapper.insert(cloneIgnoreId(dbInpatientEvaluation, o -> o.setSymptom(null)));
       // 测试 result 不匹配
       inpatientEvaluationMapper.insert(cloneIgnoreId(dbInpatientEvaluation, o -> o.setResult(null)));
       // 测试 medicationSuggestion 不匹配
       inpatientEvaluationMapper.insert(cloneIgnoreId(dbInpatientEvaluation, o -> o.setMedicationSuggestion(null)));
       // 测试 createTime 不匹配
       inpatientEvaluationMapper.insert(cloneIgnoreId(dbInpatientEvaluation, o -> o.setCreateTime(null)));
       // 准备参数
       InpatientEvaluationPageReqVO reqVO = new InpatientEvaluationPageReqVO();
       reqVO.setInpatientId(null);
       reqVO.setEvaluationDate(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setTargetDrug(null);
       reqVO.setSymptom(null);
       reqVO.setResult(null);
       reqVO.setMedicationSuggestion(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<InpatientEvaluationDO> pageResult = inpatientEvaluationService.getInpatientEvaluationPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbInpatientEvaluation, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetInpatientEvaluationList() {
       // mock 数据
       InpatientEvaluationDO dbInpatientEvaluation = randomPojo(InpatientEvaluationDO.class, o -> { // 等会查询到
           o.setInpatientId(null);
           o.setEvaluationDate(null);
           o.setTargetDrug(null);
           o.setSymptom(null);
           o.setResult(null);
           o.setMedicationSuggestion(null);
           o.setCreateTime(null);
       });
       inpatientEvaluationMapper.insert(dbInpatientEvaluation);
       // 测试 inpatientId 不匹配
       inpatientEvaluationMapper.insert(cloneIgnoreId(dbInpatientEvaluation, o -> o.setInpatientId(null)));
       // 测试 evaluationDate 不匹配
       inpatientEvaluationMapper.insert(cloneIgnoreId(dbInpatientEvaluation, o -> o.setEvaluationDate(null)));
       // 测试 targetDrug 不匹配
       inpatientEvaluationMapper.insert(cloneIgnoreId(dbInpatientEvaluation, o -> o.setTargetDrug(null)));
       // 测试 symptom 不匹配
       inpatientEvaluationMapper.insert(cloneIgnoreId(dbInpatientEvaluation, o -> o.setSymptom(null)));
       // 测试 result 不匹配
       inpatientEvaluationMapper.insert(cloneIgnoreId(dbInpatientEvaluation, o -> o.setResult(null)));
       // 测试 medicationSuggestion 不匹配
       inpatientEvaluationMapper.insert(cloneIgnoreId(dbInpatientEvaluation, o -> o.setMedicationSuggestion(null)));
       // 测试 createTime 不匹配
       inpatientEvaluationMapper.insert(cloneIgnoreId(dbInpatientEvaluation, o -> o.setCreateTime(null)));
       // 准备参数
       InpatientEvaluationExportReqVO reqVO = new InpatientEvaluationExportReqVO();
       reqVO.setInpatientId(null);
       reqVO.setEvaluationDate(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setTargetDrug(null);
       reqVO.setSymptom(null);
       reqVO.setResult(null);
       reqVO.setMedicationSuggestion(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       List<InpatientEvaluationDO> list = inpatientEvaluationService.getInpatientEvaluationList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbInpatientEvaluation, list.get(0));
    }

}
