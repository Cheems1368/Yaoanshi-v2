package com.sqlapk.medicine.module.patient.service.inpatientadvice;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.test.core.ut.BaseDbUnitTest;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientadvice.vo.InpatientAdviceCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientadvice.vo.InpatientAdviceExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientadvice.vo.InpatientAdvicePageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientadvice.vo.InpatientAdviceUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.inpatientadvice.InpatientAdviceDO;
import com.sqlapk.medicine.module.patient.dal.mysql.inpatientadvice.InpatientAdviceMapper;
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
import static com.sqlapk.medicine.module.patient.enums.ErrorCodeConstants.INPATIENT_ADVICE_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link InpatientAdviceServiceImpl} 的单元测试类
 *
 * @author 管理员
 */
@Import(InpatientAdviceServiceImpl.class)
public class InpatientAdviceServiceImplTest extends BaseDbUnitTest {

    @Resource
    private InpatientAdviceServiceImpl inpatientAdviceService;

    @Resource
    private InpatientAdviceMapper inpatientAdviceMapper;

    @Test
    public void testCreateInpatientAdvice_success() {
        // 准备参数
        InpatientAdviceCreateReqVO reqVO = randomPojo(InpatientAdviceCreateReqVO.class);

        // 调用
        Long inpatientAdviceId = inpatientAdviceService.createInpatientAdvice(reqVO);
        // 断言
        assertNotNull(inpatientAdviceId);
        // 校验记录的属性是否正确
        InpatientAdviceDO inpatientAdvice = inpatientAdviceMapper.selectById(inpatientAdviceId);
        assertPojoEquals(reqVO, inpatientAdvice);
    }

    @Test
    public void testUpdateInpatientAdvice_success() {
        // mock 数据
        InpatientAdviceDO dbInpatientAdvice = randomPojo(InpatientAdviceDO.class);
        inpatientAdviceMapper.insert(dbInpatientAdvice);// @Sql: 先插入出一条存在的数据
        // 准备参数
        InpatientAdviceUpdateReqVO reqVO = randomPojo(InpatientAdviceUpdateReqVO.class, o -> {
            o.setId(dbInpatientAdvice.getId()); // 设置更新的 ID
        });

        // 调用
        inpatientAdviceService.updateInpatientAdvice(reqVO);
        // 校验是否更新正确
        InpatientAdviceDO inpatientAdvice = inpatientAdviceMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, inpatientAdvice);
    }

    @Test
    public void testUpdateInpatientAdvice_notExists() {
        // 准备参数
        InpatientAdviceUpdateReqVO reqVO = randomPojo(InpatientAdviceUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> inpatientAdviceService.updateInpatientAdvice(reqVO), INPATIENT_ADVICE_NOT_EXISTS);
    }

    @Test
    public void testDeleteInpatientAdvice_success() {
        // mock 数据
        InpatientAdviceDO dbInpatientAdvice = randomPojo(InpatientAdviceDO.class);
        inpatientAdviceMapper.insert(dbInpatientAdvice);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbInpatientAdvice.getId();

        // 调用
        inpatientAdviceService.deleteInpatientAdvice(id);
       // 校验数据不存在了
       assertNull(inpatientAdviceMapper.selectById(id));
    }

    @Test
    public void testDeleteInpatientAdvice_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> inpatientAdviceService.deleteInpatientAdvice(id), INPATIENT_ADVICE_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetInpatientAdvicePage() {
       // mock 数据
       InpatientAdviceDO dbInpatientAdvice = randomPojo(InpatientAdviceDO.class, o -> { // 等会查询到
           o.setInpatientId(null);
           o.setAdviceDate(null);
           o.setAdviceContent(null);
           o.setIrrationalReason(null);
           o.setMedicationSuggestion(null);
           o.setResult(null);
           o.setCreateTime(null);
       });
       inpatientAdviceMapper.insert(dbInpatientAdvice);
       // 测试 inpatientId 不匹配
       inpatientAdviceMapper.insert(cloneIgnoreId(dbInpatientAdvice, o -> o.setInpatientId(null)));
       // 测试 adviceDate 不匹配
       inpatientAdviceMapper.insert(cloneIgnoreId(dbInpatientAdvice, o -> o.setAdviceDate(null)));
       // 测试 adviceContent 不匹配
       inpatientAdviceMapper.insert(cloneIgnoreId(dbInpatientAdvice, o -> o.setAdviceContent(null)));
       // 测试 irrationalReason 不匹配
       inpatientAdviceMapper.insert(cloneIgnoreId(dbInpatientAdvice, o -> o.setIrrationalReason(null)));
       // 测试 medicationSuggestion 不匹配
       inpatientAdviceMapper.insert(cloneIgnoreId(dbInpatientAdvice, o -> o.setMedicationSuggestion(null)));
       // 测试 result 不匹配
       inpatientAdviceMapper.insert(cloneIgnoreId(dbInpatientAdvice, o -> o.setResult(null)));
       // 测试 createTime 不匹配
       inpatientAdviceMapper.insert(cloneIgnoreId(dbInpatientAdvice, o -> o.setCreateTime(null)));
       // 准备参数
       InpatientAdvicePageReqVO reqVO = new InpatientAdvicePageReqVO();
       reqVO.setInpatientId(null);
       reqVO.setAdviceDate(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setAdviceContent(null);
       reqVO.setIrrationalReason(null);
       reqVO.setMedicationSuggestion(null);
       reqVO.setResult(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<InpatientAdviceDO> pageResult = inpatientAdviceService.getInpatientAdvicePage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbInpatientAdvice, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetInpatientAdviceList() {
       // mock 数据
       InpatientAdviceDO dbInpatientAdvice = randomPojo(InpatientAdviceDO.class, o -> { // 等会查询到
           o.setInpatientId(null);
           o.setAdviceDate(null);
           o.setAdviceContent(null);
           o.setIrrationalReason(null);
           o.setMedicationSuggestion(null);
           o.setResult(null);
           o.setCreateTime(null);
       });
       inpatientAdviceMapper.insert(dbInpatientAdvice);
       // 测试 inpatientId 不匹配
       inpatientAdviceMapper.insert(cloneIgnoreId(dbInpatientAdvice, o -> o.setInpatientId(null)));
       // 测试 adviceDate 不匹配
       inpatientAdviceMapper.insert(cloneIgnoreId(dbInpatientAdvice, o -> o.setAdviceDate(null)));
       // 测试 adviceContent 不匹配
       inpatientAdviceMapper.insert(cloneIgnoreId(dbInpatientAdvice, o -> o.setAdviceContent(null)));
       // 测试 irrationalReason 不匹配
       inpatientAdviceMapper.insert(cloneIgnoreId(dbInpatientAdvice, o -> o.setIrrationalReason(null)));
       // 测试 medicationSuggestion 不匹配
       inpatientAdviceMapper.insert(cloneIgnoreId(dbInpatientAdvice, o -> o.setMedicationSuggestion(null)));
       // 测试 result 不匹配
       inpatientAdviceMapper.insert(cloneIgnoreId(dbInpatientAdvice, o -> o.setResult(null)));
       // 测试 createTime 不匹配
       inpatientAdviceMapper.insert(cloneIgnoreId(dbInpatientAdvice, o -> o.setCreateTime(null)));
       // 准备参数
       InpatientAdviceExportReqVO reqVO = new InpatientAdviceExportReqVO();
       reqVO.setInpatientId(null);
       reqVO.setAdviceDate(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setAdviceContent(null);
       reqVO.setIrrationalReason(null);
       reqVO.setMedicationSuggestion(null);
       reqVO.setResult(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       List<InpatientAdviceDO> list = inpatientAdviceService.getInpatientAdviceList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbInpatientAdvice, list.get(0));
    }

}
