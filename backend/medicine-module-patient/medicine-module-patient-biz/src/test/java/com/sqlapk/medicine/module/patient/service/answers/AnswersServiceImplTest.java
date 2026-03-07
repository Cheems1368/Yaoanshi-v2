package com.sqlapk.medicine.module.patient.service.answers;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import com.sqlapk.medicine.framework.test.core.ut.BaseDbUnitTest;

import com.sqlapk.medicine.module.patient.controller.admin.answers.vo.*;
import com.sqlapk.medicine.module.patient.dal.dataobject.answers.AnswersDO;
import com.sqlapk.medicine.module.patient.dal.mysql.answers.AnswersMapper;
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
 * {@link AnswersServiceImpl} 的单元测试类
 *
 * @author 管理员
 */
@Import(AnswersServiceImpl.class)
public class AnswersServiceImplTest extends BaseDbUnitTest {

    @Resource
    private AnswersServiceImpl answersService;

    @Resource
    private AnswersMapper answersMapper;

    @Test
    public void testCreateAnswers_success() {
        // 准备参数
        AnswersCreateReqVO reqVO = randomPojo(AnswersCreateReqVO.class);

        // 调用
        Long answersId = answersService.createAnswers(reqVO);
        // 断言
        assertNotNull(answersId);
        // 校验记录的属性是否正确
        AnswersDO answers = answersMapper.selectById(answersId);
        assertPojoEquals(reqVO, answers);
    }

    @Test
    public void testUpdateAnswers_success() {
        // mock 数据
        AnswersDO dbAnswers = randomPojo(AnswersDO.class);
        answersMapper.insert(dbAnswers);// @Sql: 先插入出一条存在的数据
        // 准备参数
        AnswersUpdateReqVO reqVO = randomPojo(AnswersUpdateReqVO.class, o -> {
            o.setId(dbAnswers.getId()); // 设置更新的 ID
        });

        // 调用
        answersService.updateAnswers(reqVO);
        // 校验是否更新正确
        AnswersDO answers = answersMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, answers);
    }

    @Test
    public void testUpdateAnswers_notExists() {
        // 准备参数
        AnswersUpdateReqVO reqVO = randomPojo(AnswersUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> answersService.updateAnswers(reqVO), ANSWERS_NOT_EXISTS);
    }

    @Test
    public void testDeleteAnswers_success() {
        // mock 数据
        AnswersDO dbAnswers = randomPojo(AnswersDO.class);
        answersMapper.insert(dbAnswers);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbAnswers.getId();

        // 调用
        answersService.deleteAnswers(id);
       // 校验数据不存在了
       assertNull(answersMapper.selectById(id));
    }

    @Test
    public void testDeleteAnswers_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> answersService.deleteAnswers(id), ANSWERS_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetAnswersPage() {
       // mock 数据
       AnswersDO dbAnswers = randomPojo(AnswersDO.class, o -> { // 等会查询到
           o.setUserId(null);
           o.setQuestionId(null);
           o.setOptionId(null);
           o.setValue(null);
           o.setCreateTime(null);
       });
       answersMapper.insert(dbAnswers);
       // 测试 userId 不匹配
       answersMapper.insert(cloneIgnoreId(dbAnswers, o -> o.setUserId(null)));
       // 测试 questionId 不匹配
       answersMapper.insert(cloneIgnoreId(dbAnswers, o -> o.setQuestionId(null)));
       // 测试 optionId 不匹配
       answersMapper.insert(cloneIgnoreId(dbAnswers, o -> o.setOptionId(null)));
       // 测试 value 不匹配
       answersMapper.insert(cloneIgnoreId(dbAnswers, o -> o.setValue(null)));
       // 测试 createTime 不匹配
       answersMapper.insert(cloneIgnoreId(dbAnswers, o -> o.setCreateTime(null)));
       // 准备参数
       AnswersPageReqVO reqVO = new AnswersPageReqVO();
       reqVO.setUserId(null);
       reqVO.setQuestionId(null);
       reqVO.setOptionId(null);
       reqVO.setValue(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<AnswersDO> pageResult = answersService.getAnswersPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbAnswers, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetAnswersList() {
       // mock 数据
       AnswersDO dbAnswers = randomPojo(AnswersDO.class, o -> { // 等会查询到
           o.setUserId(null);
           o.setQuestionId(null);
           o.setOptionId(null);
           o.setValue(null);
           o.setCreateTime(null);
       });
       answersMapper.insert(dbAnswers);
       // 测试 userId 不匹配
       answersMapper.insert(cloneIgnoreId(dbAnswers, o -> o.setUserId(null)));
       // 测试 questionId 不匹配
       answersMapper.insert(cloneIgnoreId(dbAnswers, o -> o.setQuestionId(null)));
       // 测试 optionId 不匹配
       answersMapper.insert(cloneIgnoreId(dbAnswers, o -> o.setOptionId(null)));
       // 测试 value 不匹配
       answersMapper.insert(cloneIgnoreId(dbAnswers, o -> o.setValue(null)));
       // 测试 createTime 不匹配
       answersMapper.insert(cloneIgnoreId(dbAnswers, o -> o.setCreateTime(null)));
       // 准备参数
       AnswersExportReqVO reqVO = new AnswersExportReqVO();
       reqVO.setUserId(null);
       reqVO.setQuestionId(null);
       reqVO.setOptionId(null);
       reqVO.setValue(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       List<AnswersDO> list = answersService.getAnswersList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbAnswers, list.get(0));
    }

}
