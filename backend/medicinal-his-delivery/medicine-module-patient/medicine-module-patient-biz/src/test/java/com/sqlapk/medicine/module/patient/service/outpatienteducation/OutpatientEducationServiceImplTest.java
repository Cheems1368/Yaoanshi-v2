package com.sqlapk.medicine.module.patient.service.outpatienteducation;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.test.core.ut.BaseDbUnitTest;
import com.sqlapk.medicine.module.patient.controller.admin.outpatienteducation.vo.OutpatientEducationCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatienteducation.vo.OutpatientEducationExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatienteducation.vo.OutpatientEducationPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatienteducation.vo.OutpatientEducationUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.outpatienteducation.OutpatientEducationDO;
import com.sqlapk.medicine.module.patient.dal.mysql.outpatienteducation.OutpatientEducationMapper;
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
import static com.sqlapk.medicine.module.patient.enums.ErrorCodeConstants.OUTPATIENT_EDUCATION_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link OutpatientEducationServiceImpl} 的单元测试类
 *
 * @author 管理员
 */
@Import(OutpatientEducationServiceImpl.class)
public class OutpatientEducationServiceImplTest extends BaseDbUnitTest {

    @Resource
    private OutpatientEducationServiceImpl outpatientEducationService;

    @Resource
    private OutpatientEducationMapper outpatientEducationMapper;

    @Test
    public void testCreateOutpatientEducation_success() {
        // 准备参数
        OutpatientEducationCreateReqVO reqVO = randomPojo(OutpatientEducationCreateReqVO.class);

        // 调用
        Long outpatientEducationId = outpatientEducationService.createOutpatientEducation(reqVO);
        // 断言
        assertNotNull(outpatientEducationId);
        // 校验记录的属性是否正确
        OutpatientEducationDO outpatientEducation = outpatientEducationMapper.selectById(outpatientEducationId);
        assertPojoEquals(reqVO, outpatientEducation);
    }

    @Test
    public void testUpdateOutpatientEducation_success() {
        // mock 数据
        OutpatientEducationDO dbOutpatientEducation = randomPojo(OutpatientEducationDO.class);
        outpatientEducationMapper.insert(dbOutpatientEducation);// @Sql: 先插入出一条存在的数据
        // 准备参数
        OutpatientEducationUpdateReqVO reqVO = randomPojo(OutpatientEducationUpdateReqVO.class, o -> {
            o.setId(dbOutpatientEducation.getId()); // 设置更新的 ID
        });

        // 调用
        outpatientEducationService.updateOutpatientEducation(reqVO);
        // 校验是否更新正确
        OutpatientEducationDO outpatientEducation = outpatientEducationMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, outpatientEducation);
    }

    @Test
    public void testUpdateOutpatientEducation_notExists() {
        // 准备参数
        OutpatientEducationUpdateReqVO reqVO = randomPojo(OutpatientEducationUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> outpatientEducationService.updateOutpatientEducation(reqVO), OUTPATIENT_EDUCATION_NOT_EXISTS);
    }

    @Test
    public void testDeleteOutpatientEducation_success() {
        // mock 数据
        OutpatientEducationDO dbOutpatientEducation = randomPojo(OutpatientEducationDO.class);
        outpatientEducationMapper.insert(dbOutpatientEducation);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbOutpatientEducation.getId();

        // 调用
        outpatientEducationService.deleteOutpatientEducation(id);
       // 校验数据不存在了
       assertNull(outpatientEducationMapper.selectById(id));
    }

    @Test
    public void testDeleteOutpatientEducation_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> outpatientEducationService.deleteOutpatientEducation(id), OUTPATIENT_EDUCATION_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetOutpatientEducationPage() {
       // mock 数据
       OutpatientEducationDO dbOutpatientEducation = randomPojo(OutpatientEducationDO.class, o -> { // 等会查询到
           o.setOutpatientId(null);
           o.setEducationId(null);
           o.setCreateTime(null);
       });
       outpatientEducationMapper.insert(dbOutpatientEducation);
       // 测试 outpatientId 不匹配
       outpatientEducationMapper.insert(cloneIgnoreId(dbOutpatientEducation, o -> o.setOutpatientId(null)));
       // 测试 educationId 不匹配
       outpatientEducationMapper.insert(cloneIgnoreId(dbOutpatientEducation, o -> o.setEducationId(null)));
       // 测试 createTime 不匹配
       outpatientEducationMapper.insert(cloneIgnoreId(dbOutpatientEducation, o -> o.setCreateTime(null)));
       // 准备参数
       OutpatientEducationPageReqVO reqVO = new OutpatientEducationPageReqVO();
       reqVO.setOutpatientId(null);
       reqVO.setEducationId(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<OutpatientEducationDO> pageResult = outpatientEducationService.getOutpatientEducationPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbOutpatientEducation, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetOutpatientEducationList() {
       // mock 数据
       OutpatientEducationDO dbOutpatientEducation = randomPojo(OutpatientEducationDO.class, o -> { // 等会查询到
           o.setOutpatientId(null);
           o.setEducationId(null);
           o.setCreateTime(null);
       });
       outpatientEducationMapper.insert(dbOutpatientEducation);
       // 测试 outpatientId 不匹配
       outpatientEducationMapper.insert(cloneIgnoreId(dbOutpatientEducation, o -> o.setOutpatientId(null)));
       // 测试 educationId 不匹配
       outpatientEducationMapper.insert(cloneIgnoreId(dbOutpatientEducation, o -> o.setEducationId(null)));
       // 测试 createTime 不匹配
       outpatientEducationMapper.insert(cloneIgnoreId(dbOutpatientEducation, o -> o.setCreateTime(null)));
       // 准备参数
       OutpatientEducationExportReqVO reqVO = new OutpatientEducationExportReqVO();
       reqVO.setOutpatientId(null);
       reqVO.setEducationId(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       List<OutpatientEducationDO> list = outpatientEducationService.getOutpatientEducationList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbOutpatientEducation, list.get(0));
    }

}
