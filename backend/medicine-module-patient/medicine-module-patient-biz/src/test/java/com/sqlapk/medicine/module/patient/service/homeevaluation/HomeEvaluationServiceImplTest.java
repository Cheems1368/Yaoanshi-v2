package com.sqlapk.medicine.module.patient.service.homeevaluation;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.test.core.ut.BaseDbUnitTest;
import com.sqlapk.medicine.module.patient.controller.admin.homeevaluation.vo.HomeEvaluationCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeevaluation.vo.HomeEvaluationExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeevaluation.vo.HomeEvaluationPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeevaluation.vo.HomeEvaluationUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.homeevaluation.HomeEvaluationDO;
import com.sqlapk.medicine.module.patient.dal.mysql.homeevaluation.HomeEvaluationMapper;
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
import static com.sqlapk.medicine.module.patient.enums.ErrorCodeConstants.HOME_EVALUATION_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link HomeEvaluationServiceImpl} 的单元测试类
 *
 * @author 管理员
 */
@Import(HomeEvaluationServiceImpl.class)
public class HomeEvaluationServiceImplTest extends BaseDbUnitTest {

    @Resource
    private HomeEvaluationServiceImpl homeEvaluationService;

    @Resource
    private HomeEvaluationMapper homeEvaluationMapper;

    @Test
    public void testCreateHomeEvaluation_success() {
        // 准备参数
        HomeEvaluationCreateReqVO reqVO = randomPojo(HomeEvaluationCreateReqVO.class);

        // 调用
        Long homeEvaluationId = homeEvaluationService.createHomeEvaluation(reqVO);
        // 断言
        assertNotNull(homeEvaluationId);
        // 校验记录的属性是否正确
        HomeEvaluationDO homeEvaluation = homeEvaluationMapper.selectById(homeEvaluationId);
        assertPojoEquals(reqVO, homeEvaluation);
    }

    @Test
    public void testUpdateHomeEvaluation_success() {
        // mock 数据
        HomeEvaluationDO dbHomeEvaluation = randomPojo(HomeEvaluationDO.class);
        homeEvaluationMapper.insert(dbHomeEvaluation);// @Sql: 先插入出一条存在的数据
        // 准备参数
        HomeEvaluationUpdateReqVO reqVO = randomPojo(HomeEvaluationUpdateReqVO.class, o -> {
            o.setId(dbHomeEvaluation.getId()); // 设置更新的 ID
        });

        // 调用
        homeEvaluationService.updateHomeEvaluation(reqVO);
        // 校验是否更新正确
        HomeEvaluationDO homeEvaluation = homeEvaluationMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, homeEvaluation);
    }

    @Test
    public void testUpdateHomeEvaluation_notExists() {
        // 准备参数
        HomeEvaluationUpdateReqVO reqVO = randomPojo(HomeEvaluationUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> homeEvaluationService.updateHomeEvaluation(reqVO), HOME_EVALUATION_NOT_EXISTS);
    }

    @Test
    public void testDeleteHomeEvaluation_success() {
        // mock 数据
        HomeEvaluationDO dbHomeEvaluation = randomPojo(HomeEvaluationDO.class);
        homeEvaluationMapper.insert(dbHomeEvaluation);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbHomeEvaluation.getId();

        // 调用
        homeEvaluationService.deleteHomeEvaluation(id);
       // 校验数据不存在了
       assertNull(homeEvaluationMapper.selectById(id));
    }

    @Test
    public void testDeleteHomeEvaluation_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> homeEvaluationService.deleteHomeEvaluation(id), HOME_EVALUATION_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetHomeEvaluationPage() {
       // mock 数据
       HomeEvaluationDO dbHomeEvaluation = randomPojo(HomeEvaluationDO.class, o -> { // 等会查询到
           o.setEvaluationDate(null);
           o.setHomeId(null);
           o.setSymptom(null);
           o.setIndicator(null);
           o.setTreatmentProblem(null);
           o.setCreateTime(null);
       });
       homeEvaluationMapper.insert(dbHomeEvaluation);
       // 测试 evaluationDate 不匹配
       homeEvaluationMapper.insert(cloneIgnoreId(dbHomeEvaluation, o -> o.setEvaluationDate(null)));
       // 测试 homeId 不匹配
       homeEvaluationMapper.insert(cloneIgnoreId(dbHomeEvaluation, o -> o.setHomeId(null)));
       // 测试 symptom 不匹配
       homeEvaluationMapper.insert(cloneIgnoreId(dbHomeEvaluation, o -> o.setSymptom(null)));
       // 测试 indicator 不匹配
       homeEvaluationMapper.insert(cloneIgnoreId(dbHomeEvaluation, o -> o.setIndicator(null)));
       // 测试 treatmentProblem 不匹配
       homeEvaluationMapper.insert(cloneIgnoreId(dbHomeEvaluation, o -> o.setTreatmentProblem(null)));
       // 测试 createTime 不匹配
       homeEvaluationMapper.insert(cloneIgnoreId(dbHomeEvaluation, o -> o.setCreateTime(null)));
       // 准备参数
       HomeEvaluationPageReqVO reqVO = new HomeEvaluationPageReqVO();
       reqVO.setEvaluationDate(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setHomeId(null);
       reqVO.setSymptom(null);
       reqVO.setIndicator(null);
       reqVO.setTreatmentProblem(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<HomeEvaluationDO> pageResult = homeEvaluationService.getHomeEvaluationPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbHomeEvaluation, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetHomeEvaluationList() {
       // mock 数据
       HomeEvaluationDO dbHomeEvaluation = randomPojo(HomeEvaluationDO.class, o -> { // 等会查询到
           o.setEvaluationDate(null);
           o.setHomeId(null);
           o.setSymptom(null);
           o.setIndicator(null);
           o.setTreatmentProblem(null);
           o.setCreateTime(null);
       });
       homeEvaluationMapper.insert(dbHomeEvaluation);
       // 测试 evaluationDate 不匹配
       homeEvaluationMapper.insert(cloneIgnoreId(dbHomeEvaluation, o -> o.setEvaluationDate(null)));
       // 测试 homeId 不匹配
       homeEvaluationMapper.insert(cloneIgnoreId(dbHomeEvaluation, o -> o.setHomeId(null)));
       // 测试 symptom 不匹配
       homeEvaluationMapper.insert(cloneIgnoreId(dbHomeEvaluation, o -> o.setSymptom(null)));
       // 测试 indicator 不匹配
       homeEvaluationMapper.insert(cloneIgnoreId(dbHomeEvaluation, o -> o.setIndicator(null)));
       // 测试 treatmentProblem 不匹配
       homeEvaluationMapper.insert(cloneIgnoreId(dbHomeEvaluation, o -> o.setTreatmentProblem(null)));
       // 测试 createTime 不匹配
       homeEvaluationMapper.insert(cloneIgnoreId(dbHomeEvaluation, o -> o.setCreateTime(null)));
       // 准备参数
       HomeEvaluationExportReqVO reqVO = new HomeEvaluationExportReqVO();
       reqVO.setEvaluationDate(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setHomeId(null);
       reqVO.setSymptom(null);
       reqVO.setIndicator(null);
       reqVO.setTreatmentProblem(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       List<HomeEvaluationDO> list = homeEvaluationService.getHomeEvaluationList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbHomeEvaluation, list.get(0));
    }

}
