package com.sqlapk.medicine.module.patient.service.homeeducation;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.test.core.ut.BaseDbUnitTest;
import com.sqlapk.medicine.module.patient.controller.admin.homeeducation.vo.HomeEducationCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeeducation.vo.HomeEducationExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeeducation.vo.HomeEducationPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homeeducation.vo.HomeEducationUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.homeeducation.HomeEducationDO;
import com.sqlapk.medicine.module.patient.dal.mysql.homeeducation.HomeEducationMapper;
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
import static com.sqlapk.medicine.module.patient.enums.ErrorCodeConstants.HOME_EDUCATION_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link HomeEducationServiceImpl} 的单元测试类
 *
 * @author 管理员
 */
@Import(HomeEducationServiceImpl.class)
public class HomeEducationServiceImplTest extends BaseDbUnitTest {

    @Resource
    private HomeEducationServiceImpl homeEducationService;

    @Resource
    private HomeEducationMapper homeEducationMapper;

    @Test
    public void testCreateHomeEducation_success() {
        // 准备参数
        HomeEducationCreateReqVO reqVO = randomPojo(HomeEducationCreateReqVO.class);

        // 调用
        Long homeEducationId = homeEducationService.createHomeEducation(reqVO);
        // 断言
        assertNotNull(homeEducationId);
        // 校验记录的属性是否正确
        HomeEducationDO homeEducation = homeEducationMapper.selectById(homeEducationId);
        assertPojoEquals(reqVO, homeEducation);
    }

    @Test
    public void testUpdateHomeEducation_success() {
        // mock 数据
        HomeEducationDO dbHomeEducation = randomPojo(HomeEducationDO.class);
        homeEducationMapper.insert(dbHomeEducation);// @Sql: 先插入出一条存在的数据
        // 准备参数
        HomeEducationUpdateReqVO reqVO = randomPojo(HomeEducationUpdateReqVO.class, o -> {
            o.setId(dbHomeEducation.getId()); // 设置更新的 ID
        });

        // 调用
        homeEducationService.updateHomeEducation(reqVO);
        // 校验是否更新正确
        HomeEducationDO homeEducation = homeEducationMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, homeEducation);
    }

    @Test
    public void testUpdateHomeEducation_notExists() {
        // 准备参数
        HomeEducationUpdateReqVO reqVO = randomPojo(HomeEducationUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> homeEducationService.updateHomeEducation(reqVO), HOME_EDUCATION_NOT_EXISTS);
    }

    @Test
    public void testDeleteHomeEducation_success() {
        // mock 数据
        HomeEducationDO dbHomeEducation = randomPojo(HomeEducationDO.class);
        homeEducationMapper.insert(dbHomeEducation);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbHomeEducation.getId();

        // 调用
        homeEducationService.deleteHomeEducation(id);
       // 校验数据不存在了
       assertNull(homeEducationMapper.selectById(id));
    }

    @Test
    public void testDeleteHomeEducation_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> homeEducationService.deleteHomeEducation(id), HOME_EDUCATION_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetHomeEducationPage() {
       // mock 数据
       HomeEducationDO dbHomeEducation = randomPojo(HomeEducationDO.class, o -> { // 等会查询到
           o.setHomeId(null);
           o.setEducationDate(null);
           o.setDiagnosisAndMedication(null);
           o.setKeyPoints(null);
           o.setCreateTime(null);
       });
       homeEducationMapper.insert(dbHomeEducation);
       // 测试 homeId 不匹配
       homeEducationMapper.insert(cloneIgnoreId(dbHomeEducation, o -> o.setHomeId(null)));
       // 测试 educationDate 不匹配
       homeEducationMapper.insert(cloneIgnoreId(dbHomeEducation, o -> o.setEducationDate(null)));
       // 测试 diagnosisAndMedication 不匹配
       homeEducationMapper.insert(cloneIgnoreId(dbHomeEducation, o -> o.setDiagnosisAndMedication(null)));
       // 测试 keyPoints 不匹配
       homeEducationMapper.insert(cloneIgnoreId(dbHomeEducation, o -> o.setKeyPoints(null)));
       // 测试 createTime 不匹配
       homeEducationMapper.insert(cloneIgnoreId(dbHomeEducation, o -> o.setCreateTime(null)));
       // 准备参数
       HomeEducationPageReqVO reqVO = new HomeEducationPageReqVO();
       reqVO.setHomeId(null);
       reqVO.setEducationDate(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setDiagnosisAndMedication(null);
       reqVO.setKeyPoints(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<HomeEducationDO> pageResult = homeEducationService.getHomeEducationPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbHomeEducation, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetHomeEducationList() {
       // mock 数据
       HomeEducationDO dbHomeEducation = randomPojo(HomeEducationDO.class, o -> { // 等会查询到
           o.setHomeId(null);
           o.setEducationDate(null);
           o.setDiagnosisAndMedication(null);
           o.setKeyPoints(null);
           o.setCreateTime(null);
       });
       homeEducationMapper.insert(dbHomeEducation);
       // 测试 homeId 不匹配
       homeEducationMapper.insert(cloneIgnoreId(dbHomeEducation, o -> o.setHomeId(null)));
       // 测试 educationDate 不匹配
       homeEducationMapper.insert(cloneIgnoreId(dbHomeEducation, o -> o.setEducationDate(null)));
       // 测试 diagnosisAndMedication 不匹配
       homeEducationMapper.insert(cloneIgnoreId(dbHomeEducation, o -> o.setDiagnosisAndMedication(null)));
       // 测试 keyPoints 不匹配
       homeEducationMapper.insert(cloneIgnoreId(dbHomeEducation, o -> o.setKeyPoints(null)));
       // 测试 createTime 不匹配
       homeEducationMapper.insert(cloneIgnoreId(dbHomeEducation, o -> o.setCreateTime(null)));
       // 准备参数
       HomeEducationExportReqVO reqVO = new HomeEducationExportReqVO();
       reqVO.setHomeId(null);
       reqVO.setEducationDate(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setDiagnosisAndMedication(null);
       reqVO.setKeyPoints(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       List<HomeEducationDO> list = homeEducationService.getHomeEducationList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbHomeEducation, list.get(0));
    }

}
