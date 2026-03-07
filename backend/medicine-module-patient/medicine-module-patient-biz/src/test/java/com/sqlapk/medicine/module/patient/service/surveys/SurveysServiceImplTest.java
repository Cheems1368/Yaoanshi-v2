package com.sqlapk.medicine.module.patient.service.surveys;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import com.sqlapk.medicine.framework.test.core.ut.BaseDbUnitTest;

import com.sqlapk.medicine.module.patient.controller.admin.surveys.vo.*;
import com.sqlapk.medicine.module.patient.dal.dataobject.surveys.SurveysDO;
import com.sqlapk.medicine.module.patient.dal.mysql.surveys.SurveysMapper;
import com.sqlapk.medicine.framework.common.pojo.PageResult;

import javax.annotation.Resource;
import org.springframework.context.annotation.Import;
import java.util.*;
import java.time.LocalDateTime;

import static cn.hutool.core.util.RandomUtil.*;
import static com.sqlapk.medicine.module.patient.enums.ErrorCodeConstants.*;
import static com.sqlapk.medicine.framework.test.core.util.AssertUtils.*;
import static com.sqlapk.medicine.framework.test.core.util.RandomUtils.*;
import static com.sqlapk.medicine.framework.common.util.date.LocalDateTimeUtils.*;
import static com.sqlapk.medicine.framework.common.util.object.ObjectUtils.*;
import static com.sqlapk.medicine.framework.common.util.date.DateUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link SurveysServiceImpl} 的单元测试类
 *
 * @author 管理员
 */
@Import(SurveysServiceImpl.class)
public class SurveysServiceImplTest extends BaseDbUnitTest {

    @Resource
    private SurveysServiceImpl surveysService;

    @Resource
    private SurveysMapper surveysMapper;

    @Test
    public void testCreateSurveys_success() {
        // 准备参数
        SurveysCreateReqVO reqVO = randomPojo(SurveysCreateReqVO.class);

        // 调用
        Long surveysId = surveysService.createSurveys(reqVO);
        // 断言
        assertNotNull(surveysId);
        // 校验记录的属性是否正确
        SurveysDO surveys = surveysMapper.selectById(surveysId);
        assertPojoEquals(reqVO, surveys);
    }

    @Test
    public void testUpdateSurveys_success() {
        // mock 数据
        SurveysDO dbSurveys = randomPojo(SurveysDO.class);
        surveysMapper.insert(dbSurveys);// @Sql: 先插入出一条存在的数据
        // 准备参数
        SurveysUpdateReqVO reqVO = randomPojo(SurveysUpdateReqVO.class, o -> {
            o.setId(dbSurveys.getId()); // 设置更新的 ID
        });

        // 调用
        surveysService.updateSurveys(reqVO);
        // 校验是否更新正确
        SurveysDO surveys = surveysMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, surveys);
    }

    @Test
    public void testUpdateSurveys_notExists() {
        // 准备参数
        SurveysUpdateReqVO reqVO = randomPojo(SurveysUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> surveysService.updateSurveys(reqVO), SURVEYS_NOT_EXISTS);
    }

    @Test
    public void testDeleteSurveys_success() {
        // mock 数据
        SurveysDO dbSurveys = randomPojo(SurveysDO.class);
        surveysMapper.insert(dbSurveys);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbSurveys.getId();

        // 调用
        surveysService.deleteSurveys(id);
       // 校验数据不存在了
       assertNull(surveysMapper.selectById(id));
    }

    @Test
    public void testDeleteSurveys_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> surveysService.deleteSurveys(id), SURVEYS_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetSurveysPage() {
       // mock 数据
       SurveysDO dbSurveys = randomPojo(SurveysDO.class, o -> { // 等会查询到
           o.setTitle(null);
           o.setDescription(null);
           o.setCreateTime(null);
       });
       surveysMapper.insert(dbSurveys);
       // 测试 title 不匹配
       surveysMapper.insert(cloneIgnoreId(dbSurveys, o -> o.setTitle(null)));
       // 测试 description 不匹配
       surveysMapper.insert(cloneIgnoreId(dbSurveys, o -> o.setDescription(null)));
       // 测试 createTime 不匹配
       surveysMapper.insert(cloneIgnoreId(dbSurveys, o -> o.setCreateTime(null)));
       // 准备参数
       SurveysPageReqVO reqVO = new SurveysPageReqVO();
       reqVO.setTitle(null);
       reqVO.setDescription(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<SurveysDO> pageResult = surveysService.getSurveysPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbSurveys, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetSurveysList() {
       // mock 数据
       SurveysDO dbSurveys = randomPojo(SurveysDO.class, o -> { // 等会查询到
           o.setTitle(null);
           o.setDescription(null);
           o.setCreateTime(null);
       });
       surveysMapper.insert(dbSurveys);
       // 测试 title 不匹配
       surveysMapper.insert(cloneIgnoreId(dbSurveys, o -> o.setTitle(null)));
       // 测试 description 不匹配
       surveysMapper.insert(cloneIgnoreId(dbSurveys, o -> o.setDescription(null)));
       // 测试 createTime 不匹配
       surveysMapper.insert(cloneIgnoreId(dbSurveys, o -> o.setCreateTime(null)));
       // 准备参数
       SurveysExportReqVO reqVO = new SurveysExportReqVO();
       reqVO.setTitle(null);
       reqVO.setDescription(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       List<SurveysDO> list = surveysService.getSurveysList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbSurveys, list.get(0));
    }

}
