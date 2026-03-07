package com.sqlapk.medicine.module.patient.service.questions;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import com.sqlapk.medicine.framework.test.core.ut.BaseDbUnitTest;

import com.sqlapk.medicine.module.patient.controller.admin.questions.vo.*;
import com.sqlapk.medicine.module.patient.dal.dataobject.questions.QuestionsDO;
import com.sqlapk.medicine.module.patient.dal.mysql.questions.QuestionsMapper;
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
 * {@link QuestionsServiceImpl} 的单元测试类
 *
 * @author 管理员
 */
@Import(QuestionsServiceImpl.class)
public class QuestionsServiceImplTest extends BaseDbUnitTest {

    @Resource
    private QuestionsServiceImpl questionsService;

    @Resource
    private QuestionsMapper questionsMapper;

    @Test
    public void testCreateQuestions_success() {
        // 准备参数
        QuestionsCreateReqVO reqVO = randomPojo(QuestionsCreateReqVO.class);

        // 调用
        Long questionsId = questionsService.createQuestions(reqVO);
        // 断言
        assertNotNull(questionsId);
        // 校验记录的属性是否正确
        QuestionsDO questions = questionsMapper.selectById(questionsId);
        assertPojoEquals(reqVO, questions);
    }

    @Test
    public void testUpdateQuestions_success() {
        // mock 数据
        QuestionsDO dbQuestions = randomPojo(QuestionsDO.class);
        questionsMapper.insert(dbQuestions);// @Sql: 先插入出一条存在的数据
        // 准备参数
        QuestionsUpdateReqVO reqVO = randomPojo(QuestionsUpdateReqVO.class, o -> {
            o.setId(dbQuestions.getId()); // 设置更新的 ID
        });

        // 调用
        questionsService.updateQuestions(reqVO);
        // 校验是否更新正确
        QuestionsDO questions = questionsMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, questions);
    }

    @Test
    public void testUpdateQuestions_notExists() {
        // 准备参数
        QuestionsUpdateReqVO reqVO = randomPojo(QuestionsUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> questionsService.updateQuestions(reqVO), QUESTIONS_NOT_EXISTS);
    }

    @Test
    public void testDeleteQuestions_success() {
        // mock 数据
        QuestionsDO dbQuestions = randomPojo(QuestionsDO.class);
        questionsMapper.insert(dbQuestions);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbQuestions.getId();

        // 调用
        questionsService.deleteQuestions(id);
       // 校验数据不存在了
       assertNull(questionsMapper.selectById(id));
    }

    @Test
    public void testDeleteQuestions_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> questionsService.deleteQuestions(id), QUESTIONS_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetQuestionsPage() {
       // mock 数据
       QuestionsDO dbQuestions = randomPojo(QuestionsDO.class, o -> { // 等会查询到
           o.setContent(null);
           o.setSurveyId(null);
           o.setCreateTime(null);
       });
       questionsMapper.insert(dbQuestions);
       // 测试 content 不匹配
       questionsMapper.insert(cloneIgnoreId(dbQuestions, o -> o.setContent(null)));
       // 测试 surveyId 不匹配
       questionsMapper.insert(cloneIgnoreId(dbQuestions, o -> o.setSurveyId(null)));
       // 测试 createTime 不匹配
       questionsMapper.insert(cloneIgnoreId(dbQuestions, o -> o.setCreateTime(null)));
       // 准备参数
       QuestionsPageReqVO reqVO = new QuestionsPageReqVO();
       reqVO.setContent(null);
       reqVO.setSurveyId(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<QuestionsDO> pageResult = questionsService.getQuestionsPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbQuestions, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetQuestionsList() {
       // mock 数据
       QuestionsDO dbQuestions = randomPojo(QuestionsDO.class, o -> { // 等会查询到
           o.setContent(null);
           o.setSurveyId(null);
           o.setCreateTime(null);
       });
       questionsMapper.insert(dbQuestions);
       // 测试 content 不匹配
       questionsMapper.insert(cloneIgnoreId(dbQuestions, o -> o.setContent(null)));
       // 测试 surveyId 不匹配
       questionsMapper.insert(cloneIgnoreId(dbQuestions, o -> o.setSurveyId(null)));
       // 测试 createTime 不匹配
       questionsMapper.insert(cloneIgnoreId(dbQuestions, o -> o.setCreateTime(null)));
       // 准备参数
       QuestionsExportReqVO reqVO = new QuestionsExportReqVO();
       reqVO.setContent(null);
       reqVO.setSurveyId(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       List<QuestionsDO> list = questionsService.getQuestionsList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbQuestions, list.get(0));
    }

}
