package com.sqlapk.medicine.module.patient.service.home;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.test.core.ut.BaseDbUnitTest;
import com.sqlapk.medicine.module.patient.controller.admin.home.vo.HomeCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.home.vo.HomeExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.home.vo.HomePageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.home.vo.HomeUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.home.HomeDO;
import com.sqlapk.medicine.module.patient.dal.mysql.home.HomeMapper;
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
import static com.sqlapk.medicine.module.patient.enums.ErrorCodeConstants.HOME_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link HomeServiceImpl} 的单元测试类
 *
 * @author 管理员
 */
@Import(HomeServiceImpl.class)
public class HomeServiceImplTest extends BaseDbUnitTest {

    @Resource
    private HomeServiceImpl homeService;

    @Resource
    private HomeMapper homeMapper;

    @Test
    public void testCreateHome_success() {
        // 准备参数
        HomeCreateReqVO reqVO = randomPojo(HomeCreateReqVO.class);

        // 调用
        Long homeId = homeService.createHome(reqVO);
        // 断言
        assertNotNull(homeId);
        // 校验记录的属性是否正确
        HomeDO home = homeMapper.selectById(homeId);
        assertPojoEquals(reqVO, home);
    }

    @Test
    public void testUpdateHome_success() {
        // mock 数据
        HomeDO dbHome = randomPojo(HomeDO.class);
        homeMapper.insert(dbHome);// @Sql: 先插入出一条存在的数据
        // 准备参数
        HomeUpdateReqVO reqVO = randomPojo(HomeUpdateReqVO.class, o -> {
            o.setId(dbHome.getId()); // 设置更新的 ID
        });

        // 调用
        homeService.updateHome(reqVO);
        // 校验是否更新正确
        HomeDO home = homeMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, home);
    }

    @Test
    public void testUpdateHome_notExists() {
        // 准备参数
        HomeUpdateReqVO reqVO = randomPojo(HomeUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> homeService.updateHome(reqVO), HOME_NOT_EXISTS);
    }

    @Test
    public void testDeleteHome_success() {
        // mock 数据
        HomeDO dbHome = randomPojo(HomeDO.class);
        homeMapper.insert(dbHome);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbHome.getId();

        // 调用
        homeService.deleteHome(id);
       // 校验数据不存在了
       assertNull(homeMapper.selectById(id));
    }

    @Test
    public void testDeleteHome_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> homeService.deleteHome(id), HOME_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetHomePage() {
       // mock 数据
       HomeDO dbHome = randomPojo(HomeDO.class, o -> { // 等会查询到
           o.setDoctorId(null);
           o.setPatientId(null);
           o.setName(null);
           o.setInsurance(null);
           o.setGender(null);
           o.setBirthday(null);
           o.setAddress(null);
           o.setPhone(null);
           o.setInterviewLocation(null);
           o.setInterviewTime(null);
           o.setComplicatedDisease(null);
           o.setInterviewPurpose(null);
           o.setCreateTime(null);
       });
       homeMapper.insert(dbHome);
       // 测试 doctorId 不匹配
       homeMapper.insert(cloneIgnoreId(dbHome, o -> o.setDoctorId(null)));
       // 测试 patientId 不匹配
       homeMapper.insert(cloneIgnoreId(dbHome, o -> o.setPatientId(null)));
       // 测试 name 不匹配
       homeMapper.insert(cloneIgnoreId(dbHome, o -> o.setName(null)));
       // 测试 insurance 不匹配
       homeMapper.insert(cloneIgnoreId(dbHome, o -> o.setInsurance(null)));
       // 测试 gender 不匹配
       homeMapper.insert(cloneIgnoreId(dbHome, o -> o.setGender(null)));
       // 测试 birthday 不匹配
       homeMapper.insert(cloneIgnoreId(dbHome, o -> o.setBirthday(null)));
       // 测试 address 不匹配
       homeMapper.insert(cloneIgnoreId(dbHome, o -> o.setAddress(null)));
       // 测试 phone 不匹配
       homeMapper.insert(cloneIgnoreId(dbHome, o -> o.setPhone(null)));
       // 测试 interviewLocation 不匹配
       homeMapper.insert(cloneIgnoreId(dbHome, o -> o.setInterviewLocation(null)));
       // 测试 interviewTime 不匹配
       homeMapper.insert(cloneIgnoreId(dbHome, o -> o.setInterviewTime(null)));
       // 测试 complicatedDisease 不匹配
       homeMapper.insert(cloneIgnoreId(dbHome, o -> o.setComplicatedDisease(null)));
       // 测试 interviewPurpose 不匹配
       homeMapper.insert(cloneIgnoreId(dbHome, o -> o.setInterviewPurpose(null)));
       // 测试 createTime 不匹配
       homeMapper.insert(cloneIgnoreId(dbHome, o -> o.setCreateTime(null)));
       // 准备参数
       HomePageReqVO reqVO = new HomePageReqVO();
       reqVO.setDoctorId(null);
       reqVO.setPatientId(null);
       reqVO.setName(null);
       reqVO.setInsurance(null);
       reqVO.setGender(null);
       reqVO.setBirthday(null);
       reqVO.setAddress(null);
       reqVO.setPhone(null);
       reqVO.setInterviewLocation(null);
       reqVO.setInterviewTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setComplicatedDisease(null);
       reqVO.setInterviewPurpose(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<HomeDO> pageResult = homeService.getHomePage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbHome, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetHomeList() {
       // mock 数据
       HomeDO dbHome = randomPojo(HomeDO.class, o -> { // 等会查询到
           o.setDoctorId(null);
           o.setPatientId(null);
           o.setName(null);
           o.setInsurance(null);
           o.setGender(null);
           o.setBirthday(null);
           o.setAddress(null);
           o.setPhone(null);
           o.setInterviewLocation(null);
           o.setInterviewTime(null);
           o.setComplicatedDisease(null);
           o.setInterviewPurpose(null);
           o.setCreateTime(null);
       });
       homeMapper.insert(dbHome);
       // 测试 doctorId 不匹配
       homeMapper.insert(cloneIgnoreId(dbHome, o -> o.setDoctorId(null)));
       // 测试 patientId 不匹配
       homeMapper.insert(cloneIgnoreId(dbHome, o -> o.setPatientId(null)));
       // 测试 name 不匹配
       homeMapper.insert(cloneIgnoreId(dbHome, o -> o.setName(null)));
       // 测试 insurance 不匹配
       homeMapper.insert(cloneIgnoreId(dbHome, o -> o.setInsurance(null)));
       // 测试 gender 不匹配
       homeMapper.insert(cloneIgnoreId(dbHome, o -> o.setGender(null)));
       // 测试 birthday 不匹配
       homeMapper.insert(cloneIgnoreId(dbHome, o -> o.setBirthday(null)));
       // 测试 address 不匹配
       homeMapper.insert(cloneIgnoreId(dbHome, o -> o.setAddress(null)));
       // 测试 phone 不匹配
       homeMapper.insert(cloneIgnoreId(dbHome, o -> o.setPhone(null)));
       // 测试 interviewLocation 不匹配
       homeMapper.insert(cloneIgnoreId(dbHome, o -> o.setInterviewLocation(null)));
       // 测试 interviewTime 不匹配
       homeMapper.insert(cloneIgnoreId(dbHome, o -> o.setInterviewTime(null)));
       // 测试 complicatedDisease 不匹配
       homeMapper.insert(cloneIgnoreId(dbHome, o -> o.setComplicatedDisease(null)));
       // 测试 interviewPurpose 不匹配
       homeMapper.insert(cloneIgnoreId(dbHome, o -> o.setInterviewPurpose(null)));
       // 测试 createTime 不匹配
       homeMapper.insert(cloneIgnoreId(dbHome, o -> o.setCreateTime(null)));
       // 准备参数
       HomeExportReqVO reqVO = new HomeExportReqVO();
       reqVO.setDoctorId(null);
       reqVO.setPatientId(null);
       reqVO.setName(null);
       reqVO.setInsurance(null);
       reqVO.setGender(null);
       reqVO.setBirthday(null);
       reqVO.setAddress(null);
       reqVO.setPhone(null);
       reqVO.setInterviewLocation(null);
       reqVO.setInterviewTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setComplicatedDisease(null);
       reqVO.setInterviewPurpose(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       List<HomeDO> list = homeService.getHomeList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbHome, list.get(0));
    }

}
