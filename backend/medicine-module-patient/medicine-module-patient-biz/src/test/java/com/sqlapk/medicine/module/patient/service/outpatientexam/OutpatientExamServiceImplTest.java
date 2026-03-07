package com.sqlapk.medicine.module.patient.service.outpatientexam;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.test.core.ut.BaseDbUnitTest;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientexam.vo.OutpatientExamCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientexam.vo.OutpatientExamExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientexam.vo.OutpatientExamPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientexam.vo.OutpatientExamUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.outpatientexam.OutpatientExamDO;
import com.sqlapk.medicine.module.patient.dal.mysql.outpatientexam.OutpatientExamMapper;
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
import static com.sqlapk.medicine.module.patient.enums.ErrorCodeConstants.OUTPATIENT_EXAM_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link OutpatientExamServiceImpl} 的单元测试类
 *
 * @author 管理员
 */
@Import(OutpatientExamServiceImpl.class)
public class OutpatientExamServiceImplTest extends BaseDbUnitTest {

    @Resource
    private OutpatientExamServiceImpl outpatientExamService;

    @Resource
    private OutpatientExamMapper outpatientExamMapper;

    @Test
    public void testCreateOutpatientExam_success() {
        // 准备参数
        OutpatientExamCreateReqVO reqVO = randomPojo(OutpatientExamCreateReqVO.class);

        // 调用
        Long outpatientExamId = outpatientExamService.createOutpatientExam(reqVO);
        // 断言
        assertNotNull(outpatientExamId);
        // 校验记录的属性是否正确
        OutpatientExamDO outpatientExam = outpatientExamMapper.selectById(outpatientExamId);
        assertPojoEquals(reqVO, outpatientExam);
    }

    @Test
    public void testUpdateOutpatientExam_success() {
        // mock 数据
        OutpatientExamDO dbOutpatientExam = randomPojo(OutpatientExamDO.class);
        outpatientExamMapper.insert(dbOutpatientExam);// @Sql: 先插入出一条存在的数据
        // 准备参数
        OutpatientExamUpdateReqVO reqVO = randomPojo(OutpatientExamUpdateReqVO.class, o -> {
            o.setId(dbOutpatientExam.getId()); // 设置更新的 ID
        });

        // 调用
        outpatientExamService.updateOutpatientExam(reqVO);
        // 校验是否更新正确
        OutpatientExamDO outpatientExam = outpatientExamMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, outpatientExam);
    }

    @Test
    public void testUpdateOutpatientExam_notExists() {
        // 准备参数
        OutpatientExamUpdateReqVO reqVO = randomPojo(OutpatientExamUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> outpatientExamService.updateOutpatientExam(reqVO), OUTPATIENT_EXAM_NOT_EXISTS);
    }

    @Test
    public void testDeleteOutpatientExam_success() {
        // mock 数据
        OutpatientExamDO dbOutpatientExam = randomPojo(OutpatientExamDO.class);
        outpatientExamMapper.insert(dbOutpatientExam);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbOutpatientExam.getId();

        // 调用
        outpatientExamService.deleteOutpatientExam(id);
       // 校验数据不存在了
       assertNull(outpatientExamMapper.selectById(id));
    }

    @Test
    public void testDeleteOutpatientExam_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> outpatientExamService.deleteOutpatientExam(id), OUTPATIENT_EXAM_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetOutpatientExamPage() {
       // mock 数据
       OutpatientExamDO dbOutpatientExam = randomPojo(OutpatientExamDO.class, o -> { // 等会查询到
           o.setOutpatientId(null);
           o.setExaminationsId(null);
           o.setCreateTime(null);
       });
       outpatientExamMapper.insert(dbOutpatientExam);
       // 测试 outpatientId 不匹配
       outpatientExamMapper.insert(cloneIgnoreId(dbOutpatientExam, o -> o.setOutpatientId(null)));
       // 测试 examinationsId 不匹配
       outpatientExamMapper.insert(cloneIgnoreId(dbOutpatientExam, o -> o.setExaminationsId(null)));
       // 测试 createTime 不匹配
       outpatientExamMapper.insert(cloneIgnoreId(dbOutpatientExam, o -> o.setCreateTime(null)));
       // 准备参数
       OutpatientExamPageReqVO reqVO = new OutpatientExamPageReqVO();
       reqVO.setOutpatientId(null);
       reqVO.setExaminationsId(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<OutpatientExamDO> pageResult = outpatientExamService.getOutpatientExamPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbOutpatientExam, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetOutpatientExamList() {
       // mock 数据
       OutpatientExamDO dbOutpatientExam = randomPojo(OutpatientExamDO.class, o -> { // 等会查询到
           o.setOutpatientId(null);
           o.setExaminationsId(null);
           o.setCreateTime(null);
       });
       outpatientExamMapper.insert(dbOutpatientExam);
       // 测试 outpatientId 不匹配
       outpatientExamMapper.insert(cloneIgnoreId(dbOutpatientExam, o -> o.setOutpatientId(null)));
       // 测试 examinationsId 不匹配
       outpatientExamMapper.insert(cloneIgnoreId(dbOutpatientExam, o -> o.setExaminationsId(null)));
       // 测试 createTime 不匹配
       outpatientExamMapper.insert(cloneIgnoreId(dbOutpatientExam, o -> o.setCreateTime(null)));
       // 准备参数
       OutpatientExamExportReqVO reqVO = new OutpatientExamExportReqVO();
       reqVO.setOutpatientId(null);
       reqVO.setExaminationsId(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       List<OutpatientExamDO> list = outpatientExamService.getOutpatientExamList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbOutpatientExam, list.get(0));
    }

}
