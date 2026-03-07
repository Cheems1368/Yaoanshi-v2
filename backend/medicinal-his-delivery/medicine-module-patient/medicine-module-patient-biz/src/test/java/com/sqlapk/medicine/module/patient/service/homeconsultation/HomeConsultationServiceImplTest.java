package com.sqlapk.medicine.module.patient.service.homeconsultation;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.test.core.ut.BaseDbUnitTest;
import com.sqlapk.medicine.module.patient.controller.admin.homeconsultation.vo.HomeConsultationCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeconsultation.vo.HomeConsultationExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeconsultation.vo.HomeConsultationPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeconsultation.vo.HomeConsultationUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.homeconsultation.HomeConsultationDO;
import com.sqlapk.medicine.module.patient.dal.mysql.homeconsultation.HomeConsultationMapper;
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
import static com.sqlapk.medicine.module.patient.enums.ErrorCodeConstants.HOME_CONSULTATION_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link HomeConsultationServiceImpl} 的单元测试类
 *
 * @author 管理员
 */
@Import(HomeConsultationServiceImpl.class)
public class HomeConsultationServiceImplTest extends BaseDbUnitTest {

    @Resource
    private HomeConsultationServiceImpl homeConsultationService;

    @Resource
    private HomeConsultationMapper homeConsultationMapper;

    @Test
    public void testCreateHomeConsultation_success() {
        // 准备参数
        HomeConsultationCreateReqVO reqVO = randomPojo(HomeConsultationCreateReqVO.class);

        // 调用
        Long homeConsultationId = homeConsultationService.createHomeConsultation(reqVO);
        // 断言
        assertNotNull(homeConsultationId);
        // 校验记录的属性是否正确
        HomeConsultationDO homeConsultation = homeConsultationMapper.selectById(homeConsultationId);
        assertPojoEquals(reqVO, homeConsultation);
    }

    @Test
    public void testUpdateHomeConsultation_success() {
        // mock 数据
        HomeConsultationDO dbHomeConsultation = randomPojo(HomeConsultationDO.class);
        homeConsultationMapper.insert(dbHomeConsultation);// @Sql: 先插入出一条存在的数据
        // 准备参数
        HomeConsultationUpdateReqVO reqVO = randomPojo(HomeConsultationUpdateReqVO.class, o -> {
            o.setId(dbHomeConsultation.getId()); // 设置更新的 ID
        });

        // 调用
        homeConsultationService.updateHomeConsultation(reqVO);
        // 校验是否更新正确
        HomeConsultationDO homeConsultation = homeConsultationMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, homeConsultation);
    }

    @Test
    public void testUpdateHomeConsultation_notExists() {
        // 准备参数
        HomeConsultationUpdateReqVO reqVO = randomPojo(HomeConsultationUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> homeConsultationService.updateHomeConsultation(reqVO), HOME_CONSULTATION_NOT_EXISTS);
    }

    @Test
    public void testDeleteHomeConsultation_success() {
        // mock 数据
        HomeConsultationDO dbHomeConsultation = randomPojo(HomeConsultationDO.class);
        homeConsultationMapper.insert(dbHomeConsultation);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbHomeConsultation.getId();

        // 调用
        homeConsultationService.deleteHomeConsultation(id);
       // 校验数据不存在了
       assertNull(homeConsultationMapper.selectById(id));
    }

    @Test
    public void testDeleteHomeConsultation_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> homeConsultationService.deleteHomeConsultation(id), HOME_CONSULTATION_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetHomeConsultationPage() {
       // mock 数据
       HomeConsultationDO dbHomeConsultation = randomPojo(HomeConsultationDO.class, o -> { // 等会查询到
           o.setHomeId(null);
           o.setConsultationDate(null);
           o.setConsultationPoints(null);
           o.setReplyContent(null);
           o.setCreateTime(null);
       });
       homeConsultationMapper.insert(dbHomeConsultation);
       // 测试 homeId 不匹配
       homeConsultationMapper.insert(cloneIgnoreId(dbHomeConsultation, o -> o.setHomeId(null)));
       // 测试 consultationDate 不匹配
       homeConsultationMapper.insert(cloneIgnoreId(dbHomeConsultation, o -> o.setConsultationDate(null)));
       // 测试 consultationPoints 不匹配
       homeConsultationMapper.insert(cloneIgnoreId(dbHomeConsultation, o -> o.setConsultationPoints(null)));
       // 测试 replyContent 不匹配
       homeConsultationMapper.insert(cloneIgnoreId(dbHomeConsultation, o -> o.setReplyContent(null)));
       // 测试 createTime 不匹配
       homeConsultationMapper.insert(cloneIgnoreId(dbHomeConsultation, o -> o.setCreateTime(null)));
       // 准备参数
       HomeConsultationPageReqVO reqVO = new HomeConsultationPageReqVO();
       reqVO.setHomeId(null);
       reqVO.setConsultationDate(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setConsultationPoints(null);
       reqVO.setReplyContent(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<HomeConsultationDO> pageResult = homeConsultationService.getHomeConsultationPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbHomeConsultation, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetHomeConsultationList() {
       // mock 数据
       HomeConsultationDO dbHomeConsultation = randomPojo(HomeConsultationDO.class, o -> { // 等会查询到
           o.setHomeId(null);
           o.setConsultationDate(null);
           o.setConsultationPoints(null);
           o.setReplyContent(null);
           o.setCreateTime(null);
       });
       homeConsultationMapper.insert(dbHomeConsultation);
       // 测试 homeId 不匹配
       homeConsultationMapper.insert(cloneIgnoreId(dbHomeConsultation, o -> o.setHomeId(null)));
       // 测试 consultationDate 不匹配
       homeConsultationMapper.insert(cloneIgnoreId(dbHomeConsultation, o -> o.setConsultationDate(null)));
       // 测试 consultationPoints 不匹配
       homeConsultationMapper.insert(cloneIgnoreId(dbHomeConsultation, o -> o.setConsultationPoints(null)));
       // 测试 replyContent 不匹配
       homeConsultationMapper.insert(cloneIgnoreId(dbHomeConsultation, o -> o.setReplyContent(null)));
       // 测试 createTime 不匹配
       homeConsultationMapper.insert(cloneIgnoreId(dbHomeConsultation, o -> o.setCreateTime(null)));
       // 准备参数
       HomeConsultationExportReqVO reqVO = new HomeConsultationExportReqVO();
       reqVO.setHomeId(null);
       reqVO.setConsultationDate(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setConsultationPoints(null);
       reqVO.setReplyContent(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       List<HomeConsultationDO> list = homeConsultationService.getHomeConsultationList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbHomeConsultation, list.get(0));
    }

}
