package com.sqlapk.medicine.module.patient.service.inpatientconsultation;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.test.core.ut.BaseDbUnitTest;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientconsultation.vo.InpatientConsultationCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientconsultation.vo.InpatientConsultationExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientconsultation.vo.InpatientConsultationPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientconsultation.vo.InpatientConsultationUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.inpatientconsultation.InpatientConsultationDO;
import com.sqlapk.medicine.module.patient.dal.mysql.inpatientconsultation.InpatientConsultationMapper;
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
import static com.sqlapk.medicine.module.patient.enums.ErrorCodeConstants.INPATIENT_CONSULTATION_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link InpatientConsultationServiceImpl} 的单元测试类
 *
 * @author 管理员
 */
@Import(InpatientConsultationServiceImpl.class)
public class InpatientConsultationServiceImplTest extends BaseDbUnitTest {

    @Resource
    private InpatientConsultationServiceImpl inpatientConsultationService;

    @Resource
    private InpatientConsultationMapper inpatientConsultationMapper;

    @Test
    public void testCreateInpatientConsultation_success() {
        // 准备参数
        InpatientConsultationCreateReqVO reqVO = randomPojo(InpatientConsultationCreateReqVO.class);

        // 调用
        Long inpatientConsultationId = inpatientConsultationService.createInpatientConsultation(reqVO);
        // 断言
        assertNotNull(inpatientConsultationId);
        // 校验记录的属性是否正确
        InpatientConsultationDO inpatientConsultation = inpatientConsultationMapper.selectById(inpatientConsultationId);
        assertPojoEquals(reqVO, inpatientConsultation);
    }

    @Test
    public void testUpdateInpatientConsultation_success() {
        // mock 数据
        InpatientConsultationDO dbInpatientConsultation = randomPojo(InpatientConsultationDO.class);
        inpatientConsultationMapper.insert(dbInpatientConsultation);// @Sql: 先插入出一条存在的数据
        // 准备参数
        InpatientConsultationUpdateReqVO reqVO = randomPojo(InpatientConsultationUpdateReqVO.class, o -> {
            o.setId(dbInpatientConsultation.getId()); // 设置更新的 ID
        });

        // 调用
        inpatientConsultationService.updateInpatientConsultation(reqVO);
        // 校验是否更新正确
        InpatientConsultationDO inpatientConsultation = inpatientConsultationMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, inpatientConsultation);
    }

    @Test
    public void testUpdateInpatientConsultation_notExists() {
        // 准备参数
        InpatientConsultationUpdateReqVO reqVO = randomPojo(InpatientConsultationUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> inpatientConsultationService.updateInpatientConsultation(reqVO), INPATIENT_CONSULTATION_NOT_EXISTS);
    }

    @Test
    public void testDeleteInpatientConsultation_success() {
        // mock 数据
        InpatientConsultationDO dbInpatientConsultation = randomPojo(InpatientConsultationDO.class);
        inpatientConsultationMapper.insert(dbInpatientConsultation);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbInpatientConsultation.getId();

        // 调用
        inpatientConsultationService.deleteInpatientConsultation(id);
       // 校验数据不存在了
       assertNull(inpatientConsultationMapper.selectById(id));
    }

    @Test
    public void testDeleteInpatientConsultation_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> inpatientConsultationService.deleteInpatientConsultation(id), INPATIENT_CONSULTATION_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetInpatientConsultationPage() {
       // mock 数据
       InpatientConsultationDO dbInpatientConsultation = randomPojo(InpatientConsultationDO.class, o -> { // 等会查询到
           o.setInpatientId(null);
           o.setConsultationDate(null);
           o.setConsultationPoints(null);
           o.setReplyContent(null);
           o.setCreateTime(null);
       });
       inpatientConsultationMapper.insert(dbInpatientConsultation);
       // 测试 inpatientId 不匹配
       inpatientConsultationMapper.insert(cloneIgnoreId(dbInpatientConsultation, o -> o.setInpatientId(null)));
       // 测试 consultationDate 不匹配
       inpatientConsultationMapper.insert(cloneIgnoreId(dbInpatientConsultation, o -> o.setConsultationDate(null)));
       // 测试 consultationPoints 不匹配
       inpatientConsultationMapper.insert(cloneIgnoreId(dbInpatientConsultation, o -> o.setConsultationPoints(null)));
       // 测试 replyContent 不匹配
       inpatientConsultationMapper.insert(cloneIgnoreId(dbInpatientConsultation, o -> o.setReplyContent(null)));
       // 测试 createTime 不匹配
       inpatientConsultationMapper.insert(cloneIgnoreId(dbInpatientConsultation, o -> o.setCreateTime(null)));
       // 准备参数
       InpatientConsultationPageReqVO reqVO = new InpatientConsultationPageReqVO();
       reqVO.setInpatientId(null);
       reqVO.setConsultationDate(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setConsultationPoints(null);
       reqVO.setReplyContent(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<InpatientConsultationDO> pageResult = inpatientConsultationService.getInpatientConsultationPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbInpatientConsultation, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetInpatientConsultationList() {
       // mock 数据
       InpatientConsultationDO dbInpatientConsultation = randomPojo(InpatientConsultationDO.class, o -> { // 等会查询到
           o.setInpatientId(null);
           o.setConsultationDate(null);
           o.setConsultationPoints(null);
           o.setReplyContent(null);
           o.setCreateTime(null);
       });
       inpatientConsultationMapper.insert(dbInpatientConsultation);
       // 测试 inpatientId 不匹配
       inpatientConsultationMapper.insert(cloneIgnoreId(dbInpatientConsultation, o -> o.setInpatientId(null)));
       // 测试 consultationDate 不匹配
       inpatientConsultationMapper.insert(cloneIgnoreId(dbInpatientConsultation, o -> o.setConsultationDate(null)));
       // 测试 consultationPoints 不匹配
       inpatientConsultationMapper.insert(cloneIgnoreId(dbInpatientConsultation, o -> o.setConsultationPoints(null)));
       // 测试 replyContent 不匹配
       inpatientConsultationMapper.insert(cloneIgnoreId(dbInpatientConsultation, o -> o.setReplyContent(null)));
       // 测试 createTime 不匹配
       inpatientConsultationMapper.insert(cloneIgnoreId(dbInpatientConsultation, o -> o.setCreateTime(null)));
       // 准备参数
       InpatientConsultationExportReqVO reqVO = new InpatientConsultationExportReqVO();
       reqVO.setInpatientId(null);
       reqVO.setConsultationDate(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setConsultationPoints(null);
       reqVO.setReplyContent(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       List<InpatientConsultationDO> list = inpatientConsultationService.getInpatientConsultationList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbInpatientConsultation, list.get(0));
    }

}
