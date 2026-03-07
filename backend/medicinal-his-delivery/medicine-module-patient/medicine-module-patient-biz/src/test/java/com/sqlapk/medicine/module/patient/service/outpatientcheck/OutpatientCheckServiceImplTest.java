package com.sqlapk.medicine.module.patient.service.outpatientcheck;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.test.core.ut.BaseDbUnitTest;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientcheck.vo.OutpatientCheckCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientcheck.vo.OutpatientCheckExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientcheck.vo.OutpatientCheckPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientcheck.vo.OutpatientCheckUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.outpatientcheck.OutpatientCheckDO;
import com.sqlapk.medicine.module.patient.dal.mysql.outpatientcheck.OutpatientCheckMapper;
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
import static com.sqlapk.medicine.module.patient.enums.ErrorCodeConstants.OUTPATIENT_CHECK_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link OutpatientCheckServiceImpl} 的单元测试类
 *
 * @author 管理员
 */
@Import(OutpatientCheckServiceImpl.class)
public class OutpatientCheckServiceImplTest extends BaseDbUnitTest {

    @Resource
    private OutpatientCheckServiceImpl outpatientCheckService;

    @Resource
    private OutpatientCheckMapper outpatientCheckMapper;

    @Test
    public void testCreateOutpatientCheck_success() {
        // 准备参数
        OutpatientCheckCreateReqVO reqVO = randomPojo(OutpatientCheckCreateReqVO.class);

        // 调用
        Long outpatientCheckId = outpatientCheckService.createOutpatientCheck(reqVO);
        // 断言
        assertNotNull(outpatientCheckId);
        // 校验记录的属性是否正确
        OutpatientCheckDO outpatientCheck = outpatientCheckMapper.selectById(outpatientCheckId);
        assertPojoEquals(reqVO, outpatientCheck);
    }

    @Test
    public void testUpdateOutpatientCheck_success() {
        // mock 数据
        OutpatientCheckDO dbOutpatientCheck = randomPojo(OutpatientCheckDO.class);
        outpatientCheckMapper.insert(dbOutpatientCheck);// @Sql: 先插入出一条存在的数据
        // 准备参数
        OutpatientCheckUpdateReqVO reqVO = randomPojo(OutpatientCheckUpdateReqVO.class, o -> {
            o.setId(dbOutpatientCheck.getId()); // 设置更新的 ID
        });

        // 调用
        outpatientCheckService.updateOutpatientCheck(reqVO);
        // 校验是否更新正确
        OutpatientCheckDO outpatientCheck = outpatientCheckMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, outpatientCheck);
    }

    @Test
    public void testUpdateOutpatientCheck_notExists() {
        // 准备参数
        OutpatientCheckUpdateReqVO reqVO = randomPojo(OutpatientCheckUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> outpatientCheckService.updateOutpatientCheck(reqVO), OUTPATIENT_CHECK_NOT_EXISTS);
    }

    @Test
    public void testDeleteOutpatientCheck_success() {
        // mock 数据
        OutpatientCheckDO dbOutpatientCheck = randomPojo(OutpatientCheckDO.class);
        outpatientCheckMapper.insert(dbOutpatientCheck);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbOutpatientCheck.getId();

        // 调用
        outpatientCheckService.deleteOutpatientCheck(id);
       // 校验数据不存在了
       assertNull(outpatientCheckMapper.selectById(id));
    }

    @Test
    public void testDeleteOutpatientCheck_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> outpatientCheckService.deleteOutpatientCheck(id), OUTPATIENT_CHECK_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetOutpatientCheckPage() {
       // mock 数据
       OutpatientCheckDO dbOutpatientCheck = randomPojo(OutpatientCheckDO.class, o -> { // 等会查询到
           o.setOutpatientId(null);
           o.setMedicineName(null);
           o.setDoctorId(null);
           o.setPrescriptionId(null);
           o.setIssueDesc(null);
           o.setIssueCode(null);
           o.setInterveneResult(null);
           o.setCreateTime(null);
       });
       outpatientCheckMapper.insert(dbOutpatientCheck);
       // 测试 outpatientId 不匹配
       outpatientCheckMapper.insert(cloneIgnoreId(dbOutpatientCheck, o -> o.setOutpatientId(null)));
       // 测试 medicineName 不匹配
       outpatientCheckMapper.insert(cloneIgnoreId(dbOutpatientCheck, o -> o.setMedicineName(null)));
       // 测试 doctorId 不匹配
       outpatientCheckMapper.insert(cloneIgnoreId(dbOutpatientCheck, o -> o.setDoctorId(null)));
       // 测试 prescriptionId 不匹配
       outpatientCheckMapper.insert(cloneIgnoreId(dbOutpatientCheck, o -> o.setPrescriptionId(null)));
       // 测试 issueDesc 不匹配
       outpatientCheckMapper.insert(cloneIgnoreId(dbOutpatientCheck, o -> o.setIssueDesc(null)));
       // 测试 issueCode 不匹配
       outpatientCheckMapper.insert(cloneIgnoreId(dbOutpatientCheck, o -> o.setIssueCode(null)));
       // 测试 interveneResult 不匹配
       outpatientCheckMapper.insert(cloneIgnoreId(dbOutpatientCheck, o -> o.setInterveneResult(null)));
       // 测试 createTime 不匹配
       outpatientCheckMapper.insert(cloneIgnoreId(dbOutpatientCheck, o -> o.setCreateTime(null)));
       // 准备参数
       OutpatientCheckPageReqVO reqVO = new OutpatientCheckPageReqVO();
       reqVO.setOutpatientId(null);
       reqVO.setMedicineName(null);
       reqVO.setDoctorId(null);
       reqVO.setPrescriptionId(null);
       reqVO.setIssueDesc(null);
       reqVO.setIssueCode(null);
       reqVO.setInterveneResult(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<OutpatientCheckDO> pageResult = outpatientCheckService.getOutpatientCheckPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbOutpatientCheck, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetOutpatientCheckList() {
       // mock 数据
       OutpatientCheckDO dbOutpatientCheck = randomPojo(OutpatientCheckDO.class, o -> { // 等会查询到
           o.setOutpatientId(null);
           o.setMedicineName(null);
           o.setDoctorId(null);
           o.setPrescriptionId(null);
           o.setIssueDesc(null);
           o.setIssueCode(null);
           o.setInterveneResult(null);
           o.setCreateTime(null);
       });
       outpatientCheckMapper.insert(dbOutpatientCheck);
       // 测试 outpatientId 不匹配
       outpatientCheckMapper.insert(cloneIgnoreId(dbOutpatientCheck, o -> o.setOutpatientId(null)));
       // 测试 medicineName 不匹配
       outpatientCheckMapper.insert(cloneIgnoreId(dbOutpatientCheck, o -> o.setMedicineName(null)));
       // 测试 doctorId 不匹配
       outpatientCheckMapper.insert(cloneIgnoreId(dbOutpatientCheck, o -> o.setDoctorId(null)));
       // 测试 prescriptionId 不匹配
       outpatientCheckMapper.insert(cloneIgnoreId(dbOutpatientCheck, o -> o.setPrescriptionId(null)));
       // 测试 issueDesc 不匹配
       outpatientCheckMapper.insert(cloneIgnoreId(dbOutpatientCheck, o -> o.setIssueDesc(null)));
       // 测试 issueCode 不匹配
       outpatientCheckMapper.insert(cloneIgnoreId(dbOutpatientCheck, o -> o.setIssueCode(null)));
       // 测试 interveneResult 不匹配
       outpatientCheckMapper.insert(cloneIgnoreId(dbOutpatientCheck, o -> o.setInterveneResult(null)));
       // 测试 createTime 不匹配
       outpatientCheckMapper.insert(cloneIgnoreId(dbOutpatientCheck, o -> o.setCreateTime(null)));
       // 准备参数
       OutpatientCheckExportReqVO reqVO = new OutpatientCheckExportReqVO();
       reqVO.setOutpatientId(null);
       reqVO.setMedicineName(null);
       reqVO.setDoctorId(null);
       reqVO.setPrescriptionId(null);
       reqVO.setIssueDesc(null);
       reqVO.setIssueCode(null);
       reqVO.setInterveneResult(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       List<OutpatientCheckDO> list = outpatientCheckService.getOutpatientCheckList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbOutpatientCheck, list.get(0));
    }

}
