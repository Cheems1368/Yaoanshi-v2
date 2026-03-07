package com.sqlapk.medicine.module.patient.service.inpatientround;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.test.core.ut.BaseDbUnitTest;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientround.vo.InpatientRoundCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientround.vo.InpatientRoundExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientround.vo.InpatientRoundPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.inpatientround.vo.InpatientRoundUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.inpatientround.InpatientRoundDO;
import com.sqlapk.medicine.module.patient.dal.mysql.inpatientround.InpatientRoundMapper;
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
import static com.sqlapk.medicine.module.patient.enums.ErrorCodeConstants.INPATIENT_ROUND_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link InpatientRoundServiceImpl} 的单元测试类
 *
 * @author 管理员
 */
@Import(InpatientRoundServiceImpl.class)
public class InpatientRoundServiceImplTest extends BaseDbUnitTest {

    @Resource
    private InpatientRoundServiceImpl inpatientRoundService;

    @Resource
    private InpatientRoundMapper inpatientRoundMapper;

    @Test
    public void testCreateInpatientRound_success() {
        // 准备参数
        InpatientRoundCreateReqVO reqVO = randomPojo(InpatientRoundCreateReqVO.class);

        // 调用
        Long inpatientRoundId = inpatientRoundService.createInpatientRound(reqVO);
        // 断言
        assertNotNull(inpatientRoundId);
        // 校验记录的属性是否正确
        InpatientRoundDO inpatientRound = inpatientRoundMapper.selectById(inpatientRoundId);
        assertPojoEquals(reqVO, inpatientRound);
    }

    @Test
    public void testUpdateInpatientRound_success() {
        // mock 数据
        InpatientRoundDO dbInpatientRound = randomPojo(InpatientRoundDO.class);
        inpatientRoundMapper.insert(dbInpatientRound);// @Sql: 先插入出一条存在的数据
        // 准备参数
        InpatientRoundUpdateReqVO reqVO = randomPojo(InpatientRoundUpdateReqVO.class, o -> {
            o.setId(dbInpatientRound.getId()); // 设置更新的 ID
        });

        // 调用
        inpatientRoundService.updateInpatientRound(reqVO);
        // 校验是否更新正确
        InpatientRoundDO inpatientRound = inpatientRoundMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, inpatientRound);
    }

    @Test
    public void testUpdateInpatientRound_notExists() {
        // 准备参数
        InpatientRoundUpdateReqVO reqVO = randomPojo(InpatientRoundUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> inpatientRoundService.updateInpatientRound(reqVO), INPATIENT_ROUND_NOT_EXISTS);
    }

    @Test
    public void testDeleteInpatientRound_success() {
        // mock 数据
        InpatientRoundDO dbInpatientRound = randomPojo(InpatientRoundDO.class);
        inpatientRoundMapper.insert(dbInpatientRound);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbInpatientRound.getId();

        // 调用
        inpatientRoundService.deleteInpatientRound(id);
       // 校验数据不存在了
       assertNull(inpatientRoundMapper.selectById(id));
    }

    @Test
    public void testDeleteInpatientRound_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> inpatientRoundService.deleteInpatientRound(id), INPATIENT_ROUND_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetInpatientRoundPage() {
       // mock 数据
       InpatientRoundDO dbInpatientRound = randomPojo(InpatientRoundDO.class, o -> { // 等会查询到
           o.setInpatientId(null);
           o.setRoundDate(null);
           o.setRoundContent(null);
           o.setCreateTime(null);
       });
       inpatientRoundMapper.insert(dbInpatientRound);
       // 测试 inpatientId 不匹配
       inpatientRoundMapper.insert(cloneIgnoreId(dbInpatientRound, o -> o.setInpatientId(null)));
       // 测试 roundDate 不匹配
       inpatientRoundMapper.insert(cloneIgnoreId(dbInpatientRound, o -> o.setRoundDate(null)));
       // 测试 roundContent 不匹配
       inpatientRoundMapper.insert(cloneIgnoreId(dbInpatientRound, o -> o.setRoundContent(null)));
       // 测试 createTime 不匹配
       inpatientRoundMapper.insert(cloneIgnoreId(dbInpatientRound, o -> o.setCreateTime(null)));
       // 准备参数
       InpatientRoundPageReqVO reqVO = new InpatientRoundPageReqVO();
       reqVO.setInpatientId(null);
       reqVO.setRoundDate(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setRoundContent(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<InpatientRoundDO> pageResult = inpatientRoundService.getInpatientRoundPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbInpatientRound, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetInpatientRoundList() {
       // mock 数据
       InpatientRoundDO dbInpatientRound = randomPojo(InpatientRoundDO.class, o -> { // 等会查询到
           o.setInpatientId(null);
           o.setRoundDate(null);
           o.setRoundContent(null);
           o.setCreateTime(null);
       });
       inpatientRoundMapper.insert(dbInpatientRound);
       // 测试 inpatientId 不匹配
       inpatientRoundMapper.insert(cloneIgnoreId(dbInpatientRound, o -> o.setInpatientId(null)));
       // 测试 roundDate 不匹配
       inpatientRoundMapper.insert(cloneIgnoreId(dbInpatientRound, o -> o.setRoundDate(null)));
       // 测试 roundContent 不匹配
       inpatientRoundMapper.insert(cloneIgnoreId(dbInpatientRound, o -> o.setRoundContent(null)));
       // 测试 createTime 不匹配
       inpatientRoundMapper.insert(cloneIgnoreId(dbInpatientRound, o -> o.setCreateTime(null)));
       // 准备参数
       InpatientRoundExportReqVO reqVO = new InpatientRoundExportReqVO();
       reqVO.setInpatientId(null);
       reqVO.setRoundDate(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setRoundContent(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       List<InpatientRoundDO> list = inpatientRoundService.getInpatientRoundList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbInpatientRound, list.get(0));
    }

}
