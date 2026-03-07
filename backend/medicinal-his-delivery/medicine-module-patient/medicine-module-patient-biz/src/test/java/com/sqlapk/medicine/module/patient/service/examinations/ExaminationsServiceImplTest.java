package com.sqlapk.medicine.module.patient.service.examinations;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.test.core.ut.BaseDbUnitTest;
import com.sqlapk.medicine.module.patient.controller.admin.examinations.vo.ExaminationsCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.examinations.vo.ExaminationsExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.examinations.vo.ExaminationsPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.examinations.vo.ExaminationsUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.examinations.ExaminationsDO;
import com.sqlapk.medicine.module.patient.dal.mysql.examinations.ExaminationsMapper;
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
import static com.sqlapk.medicine.module.patient.enums.ErrorCodeConstants.EXAMINATIONS_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link ExaminationsServiceImpl} 的单元测试类
 *
 * @author 管理员
 */
@Import(ExaminationsServiceImpl.class)
public class ExaminationsServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ExaminationsServiceImpl examinationsService;

    @Resource
    private ExaminationsMapper examinationsMapper;

    @Test
    public void testCreateExaminations_success() {
        // 准备参数
        ExaminationsCreateReqVO reqVO = randomPojo(ExaminationsCreateReqVO.class);

        // 调用
        Long examinationsId = examinationsService.createExaminations(reqVO);
        // 断言
        assertNotNull(examinationsId);
        // 校验记录的属性是否正确
        ExaminationsDO examinations = examinationsMapper.selectById(examinationsId);
        assertPojoEquals(reqVO, examinations);
    }

    @Test
    public void testUpdateExaminations_success() {
        // mock 数据
        ExaminationsDO dbExaminations = randomPojo(ExaminationsDO.class);
        examinationsMapper.insert(dbExaminations);// @Sql: 先插入出一条存在的数据
        // 准备参数
        ExaminationsUpdateReqVO reqVO = randomPojo(ExaminationsUpdateReqVO.class, o -> {
            o.setId(dbExaminations.getId()); // 设置更新的 ID
        });

        // 调用
        examinationsService.updateExaminations(reqVO);
        // 校验是否更新正确
        ExaminationsDO examinations = examinationsMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, examinations);
    }

    @Test
    public void testUpdateExaminations_notExists() {
        // 准备参数
        ExaminationsUpdateReqVO reqVO = randomPojo(ExaminationsUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> examinationsService.updateExaminations(reqVO), EXAMINATIONS_NOT_EXISTS);
    }

    @Test
    public void testDeleteExaminations_success() {
        // mock 数据
        ExaminationsDO dbExaminations = randomPojo(ExaminationsDO.class);
        examinationsMapper.insert(dbExaminations);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbExaminations.getId();

        // 调用
        examinationsService.deleteExaminations(id);
       // 校验数据不存在了
       assertNull(examinationsMapper.selectById(id));
    }

    @Test
    public void testDeleteExaminations_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> examinationsService.deleteExaminations(id), EXAMINATIONS_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetExaminationsPage() {
       // mock 数据
       ExaminationsDO dbExaminations = randomPojo(ExaminationsDO.class, o -> { // 等会查询到
           o.setName(null);
           o.setPrice(null);
           o.setDescription(null);
           o.setCreateTime(null);
       });
       examinationsMapper.insert(dbExaminations);
       // 测试 name 不匹配
       examinationsMapper.insert(cloneIgnoreId(dbExaminations, o -> o.setName(null)));
       // 测试 price 不匹配
       examinationsMapper.insert(cloneIgnoreId(dbExaminations, o -> o.setPrice(null)));
       // 测试 description 不匹配
       examinationsMapper.insert(cloneIgnoreId(dbExaminations, o -> o.setDescription(null)));
       // 测试 createTime 不匹配
       examinationsMapper.insert(cloneIgnoreId(dbExaminations, o -> o.setCreateTime(null)));
       // 准备参数
       ExaminationsPageReqVO reqVO = new ExaminationsPageReqVO();
       reqVO.setName(null);
       reqVO.setPrice(null);
       reqVO.setDescription(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<ExaminationsDO> pageResult = examinationsService.getExaminationsPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbExaminations, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetExaminationsList() {
       // mock 数据
       ExaminationsDO dbExaminations = randomPojo(ExaminationsDO.class, o -> { // 等会查询到
           o.setName(null);
           o.setPrice(null);
           o.setDescription(null);
           o.setCreateTime(null);
       });
       examinationsMapper.insert(dbExaminations);
       // 测试 name 不匹配
       examinationsMapper.insert(cloneIgnoreId(dbExaminations, o -> o.setName(null)));
       // 测试 price 不匹配
       examinationsMapper.insert(cloneIgnoreId(dbExaminations, o -> o.setPrice(null)));
       // 测试 description 不匹配
       examinationsMapper.insert(cloneIgnoreId(dbExaminations, o -> o.setDescription(null)));
       // 测试 createTime 不匹配
       examinationsMapper.insert(cloneIgnoreId(dbExaminations, o -> o.setCreateTime(null)));
       // 准备参数
       ExaminationsExportReqVO reqVO = new ExaminationsExportReqVO();
       reqVO.setName(null);
       reqVO.setPrice(null);
       reqVO.setDescription(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       List<ExaminationsDO> list = examinationsService.getExaminationsList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbExaminations, list.get(0));
    }

}
