package com.sqlapk.medicine.module.patient.service.options;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import com.sqlapk.medicine.framework.test.core.ut.BaseDbUnitTest;

import com.sqlapk.medicine.module.patient.controller.admin.options.vo.*;
import com.sqlapk.medicine.module.patient.dal.dataobject.options.OptionsDO;
import com.sqlapk.medicine.module.patient.dal.mysql.options.OptionsMapper;
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
 * {@link OptionsServiceImpl} 的单元测试类
 *
 * @author 管理员
 */
@Import(OptionsServiceImpl.class)
public class OptionsServiceImplTest extends BaseDbUnitTest {

    @Resource
    private OptionsServiceImpl optionsService;

    @Resource
    private OptionsMapper optionsMapper;

    @Test
    public void testCreateOptions_success() {
        // 准备参数
        OptionsCreateReqVO reqVO = randomPojo(OptionsCreateReqVO.class);

        // 调用
        Long optionsId = optionsService.createOptions(reqVO);
        // 断言
        assertNotNull(optionsId);
        // 校验记录的属性是否正确
        OptionsDO options = optionsMapper.selectById(optionsId);
        assertPojoEquals(reqVO, options);
    }

    @Test
    public void testUpdateOptions_success() {
        // mock 数据
        OptionsDO dbOptions = randomPojo(OptionsDO.class);
        optionsMapper.insert(dbOptions);// @Sql: 先插入出一条存在的数据
        // 准备参数
        OptionsUpdateReqVO reqVO = randomPojo(OptionsUpdateReqVO.class, o -> {
            o.setId(dbOptions.getId()); // 设置更新的 ID
        });

        // 调用
        optionsService.updateOptions(reqVO);
        // 校验是否更新正确
        OptionsDO options = optionsMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, options);
    }

    @Test
    public void testUpdateOptions_notExists() {
        // 准备参数
        OptionsUpdateReqVO reqVO = randomPojo(OptionsUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> optionsService.updateOptions(reqVO), OPTIONS_NOT_EXISTS);
    }

    @Test
    public void testDeleteOptions_success() {
        // mock 数据
        OptionsDO dbOptions = randomPojo(OptionsDO.class);
        optionsMapper.insert(dbOptions);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbOptions.getId();

        // 调用
        optionsService.deleteOptions(id);
       // 校验数据不存在了
       assertNull(optionsMapper.selectById(id));
    }

    @Test
    public void testDeleteOptions_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> optionsService.deleteOptions(id), OPTIONS_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetOptionsPage() {
       // mock 数据
       OptionsDO dbOptions = randomPojo(OptionsDO.class, o -> { // 等会查询到
           o.setContent(null);
           o.setValue(null);
           o.setScore(null);
           o.setQuestionId(null);
           o.setCreateTime(null);
       });
       optionsMapper.insert(dbOptions);
       // 测试 content 不匹配
       optionsMapper.insert(cloneIgnoreId(dbOptions, o -> o.setContent(null)));
       // 测试 value 不匹配
       optionsMapper.insert(cloneIgnoreId(dbOptions, o -> o.setValue(null)));
       // 测试 score 不匹配
       optionsMapper.insert(cloneIgnoreId(dbOptions, o -> o.setScore(null)));
       // 测试 questionId 不匹配
       optionsMapper.insert(cloneIgnoreId(dbOptions, o -> o.setQuestionId(null)));
       // 测试 createTime 不匹配
       optionsMapper.insert(cloneIgnoreId(dbOptions, o -> o.setCreateTime(null)));
       // 准备参数
       OptionsPageReqVO reqVO = new OptionsPageReqVO();
       reqVO.setContent(null);
       reqVO.setValue(null);
       reqVO.setScore(null);
       reqVO.setQuestionId(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<OptionsDO> pageResult = optionsService.getOptionsPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbOptions, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetOptionsList() {
       // mock 数据
       OptionsDO dbOptions = randomPojo(OptionsDO.class, o -> { // 等会查询到
           o.setContent(null);
           o.setValue(null);
           o.setScore(null);
           o.setQuestionId(null);
           o.setCreateTime(null);
       });
       optionsMapper.insert(dbOptions);
       // 测试 content 不匹配
       optionsMapper.insert(cloneIgnoreId(dbOptions, o -> o.setContent(null)));
       // 测试 value 不匹配
       optionsMapper.insert(cloneIgnoreId(dbOptions, o -> o.setValue(null)));
       // 测试 score 不匹配
       optionsMapper.insert(cloneIgnoreId(dbOptions, o -> o.setScore(null)));
       // 测试 questionId 不匹配
       optionsMapper.insert(cloneIgnoreId(dbOptions, o -> o.setQuestionId(null)));
       // 测试 createTime 不匹配
       optionsMapper.insert(cloneIgnoreId(dbOptions, o -> o.setCreateTime(null)));
       // 准备参数
       OptionsExportReqVO reqVO = new OptionsExportReqVO();
       reqVO.setContent(null);
       reqVO.setValue(null);
       reqVO.setScore(null);
       reqVO.setQuestionId(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       List<OptionsDO> list = optionsService.getOptionsList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbOptions, list.get(0));
    }

}
