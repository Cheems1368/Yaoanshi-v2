package com.sqlapk.medicine.module.patient.service.homemedicine;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.test.core.ut.BaseDbUnitTest;
import com.sqlapk.medicine.module.patient.controller.admin.homemedicine.vo.HomeMedicineCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homemedicine.vo.HomeMedicineExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homemedicine.vo.HomeMedicinePageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.homemedicine.vo.HomeMedicineUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.homemedicine.HomeMedicineDO;
import com.sqlapk.medicine.module.patient.dal.mysql.homemedicine.HomeMedicineMapper;
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
import static com.sqlapk.medicine.module.patient.enums.ErrorCodeConstants.HOME_MEDICINE_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link HomeMedicineServiceImpl} 的单元测试类
 *
 * @author 管理员
 */
@Import(HomeMedicineServiceImpl.class)
public class HomeMedicineServiceImplTest extends BaseDbUnitTest {

    @Resource
    private HomeMedicineServiceImpl homeMedicineService;

    @Resource
    private HomeMedicineMapper homeMedicineMapper;

    @Test
    public void testCreateHomeMedicine_success() {
        // 准备参数
        HomeMedicineCreateReqVO reqVO = randomPojo(HomeMedicineCreateReqVO.class);

        // 调用
        Long homeMedicineId = homeMedicineService.createHomeMedicine(reqVO);
        // 断言
        assertNotNull(homeMedicineId);
        // 校验记录的属性是否正确
        HomeMedicineDO homeMedicine = homeMedicineMapper.selectById(homeMedicineId);
        assertPojoEquals(reqVO, homeMedicine);
    }

    @Test
    public void testUpdateHomeMedicine_success() {
        // mock 数据
        HomeMedicineDO dbHomeMedicine = randomPojo(HomeMedicineDO.class);
        homeMedicineMapper.insert(dbHomeMedicine);// @Sql: 先插入出一条存在的数据
        // 准备参数
        HomeMedicineUpdateReqVO reqVO = randomPojo(HomeMedicineUpdateReqVO.class, o -> {
            o.setId(dbHomeMedicine.getId()); // 设置更新的 ID
        });

        // 调用
        homeMedicineService.updateHomeMedicine(reqVO);
        // 校验是否更新正确
        HomeMedicineDO homeMedicine = homeMedicineMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, homeMedicine);
    }

    @Test
    public void testUpdateHomeMedicine_notExists() {
        // 准备参数
        HomeMedicineUpdateReqVO reqVO = randomPojo(HomeMedicineUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> homeMedicineService.updateHomeMedicine(reqVO), HOME_MEDICINE_NOT_EXISTS);
    }

    @Test
    public void testDeleteHomeMedicine_success() {
        // mock 数据
        HomeMedicineDO dbHomeMedicine = randomPojo(HomeMedicineDO.class);
        homeMedicineMapper.insert(dbHomeMedicine);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbHomeMedicine.getId();

        // 调用
        homeMedicineService.deleteHomeMedicine(id);
       // 校验数据不存在了
       assertNull(homeMedicineMapper.selectById(id));
    }

    @Test
    public void testDeleteHomeMedicine_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> homeMedicineService.deleteHomeMedicine(id), HOME_MEDICINE_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetHomeMedicinePage() {
       // mock 数据
       HomeMedicineDO dbHomeMedicine = randomPojo(HomeMedicineDO.class, o -> { // 等会查询到
           o.setHomeId(null);
           o.setMedicineName(null);
           o.setDoctorId(null);
           o.setDoctorName(null);
           o.setDosage(null);
           o.setIndications(null);
           o.setMedicineUsage(null);
           o.setStartTime(null);
           o.setEndTime(null);
           o.setComment(null);
           o.setCreateTime(null);
       });
       homeMedicineMapper.insert(dbHomeMedicine);
       // 测试 homeId 不匹配
       homeMedicineMapper.insert(cloneIgnoreId(dbHomeMedicine, o -> o.setHomeId(null)));
       // 测试 medicineName 不匹配
       homeMedicineMapper.insert(cloneIgnoreId(dbHomeMedicine, o -> o.setMedicineName(null)));
       // 测试 doctorId 不匹配
       homeMedicineMapper.insert(cloneIgnoreId(dbHomeMedicine, o -> o.setDoctorId(null)));
       // 测试 doctorName 不匹配
       homeMedicineMapper.insert(cloneIgnoreId(dbHomeMedicine, o -> o.setDoctorName(null)));
       // 测试 dosage 不匹配
       homeMedicineMapper.insert(cloneIgnoreId(dbHomeMedicine, o -> o.setDosage(null)));
       // 测试 indications 不匹配
       homeMedicineMapper.insert(cloneIgnoreId(dbHomeMedicine, o -> o.setIndications(null)));
       // 测试 medicineUsage 不匹配
       homeMedicineMapper.insert(cloneIgnoreId(dbHomeMedicine, o -> o.setMedicineUsage(null)));
       // 测试 startTime 不匹配
       homeMedicineMapper.insert(cloneIgnoreId(dbHomeMedicine, o -> o.setStartTime(null)));
       // 测试 endTime 不匹配
       homeMedicineMapper.insert(cloneIgnoreId(dbHomeMedicine, o -> o.setEndTime(null)));
       // 测试 comment 不匹配
       homeMedicineMapper.insert(cloneIgnoreId(dbHomeMedicine, o -> o.setComment(null)));
       // 测试 createTime 不匹配
       homeMedicineMapper.insert(cloneIgnoreId(dbHomeMedicine, o -> o.setCreateTime(null)));
       // 准备参数
       HomeMedicinePageReqVO reqVO = new HomeMedicinePageReqVO();
       reqVO.setHomeId(null);
       reqVO.setMedicineName(null);
       reqVO.setDoctorId(null);
       reqVO.setDoctorName(null);
       reqVO.setDosage(null);
       reqVO.setIndications(null);
       reqVO.setMedicineUsage(null);
       reqVO.setStartTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setEndTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setComment(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<HomeMedicineDO> pageResult = homeMedicineService.getHomeMedicinePage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbHomeMedicine, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetHomeMedicineList() {
       // mock 数据
       HomeMedicineDO dbHomeMedicine = randomPojo(HomeMedicineDO.class, o -> { // 等会查询到
           o.setHomeId(null);
           o.setMedicineName(null);
           o.setDoctorId(null);
           o.setDoctorName(null);
           o.setDosage(null);
           o.setIndications(null);
           o.setMedicineUsage(null);
           o.setStartTime(null);
           o.setEndTime(null);
           o.setComment(null);
           o.setCreateTime(null);
       });
       homeMedicineMapper.insert(dbHomeMedicine);
       // 测试 homeId 不匹配
       homeMedicineMapper.insert(cloneIgnoreId(dbHomeMedicine, o -> o.setHomeId(null)));
       // 测试 medicineName 不匹配
       homeMedicineMapper.insert(cloneIgnoreId(dbHomeMedicine, o -> o.setMedicineName(null)));
       // 测试 doctorId 不匹配
       homeMedicineMapper.insert(cloneIgnoreId(dbHomeMedicine, o -> o.setDoctorId(null)));
       // 测试 doctorName 不匹配
       homeMedicineMapper.insert(cloneIgnoreId(dbHomeMedicine, o -> o.setDoctorName(null)));
       // 测试 dosage 不匹配
       homeMedicineMapper.insert(cloneIgnoreId(dbHomeMedicine, o -> o.setDosage(null)));
       // 测试 indications 不匹配
       homeMedicineMapper.insert(cloneIgnoreId(dbHomeMedicine, o -> o.setIndications(null)));
       // 测试 medicineUsage 不匹配
       homeMedicineMapper.insert(cloneIgnoreId(dbHomeMedicine, o -> o.setMedicineUsage(null)));
       // 测试 startTime 不匹配
       homeMedicineMapper.insert(cloneIgnoreId(dbHomeMedicine, o -> o.setStartTime(null)));
       // 测试 endTime 不匹配
       homeMedicineMapper.insert(cloneIgnoreId(dbHomeMedicine, o -> o.setEndTime(null)));
       // 测试 comment 不匹配
       homeMedicineMapper.insert(cloneIgnoreId(dbHomeMedicine, o -> o.setComment(null)));
       // 测试 createTime 不匹配
       homeMedicineMapper.insert(cloneIgnoreId(dbHomeMedicine, o -> o.setCreateTime(null)));
       // 准备参数
       HomeMedicineExportReqVO reqVO = new HomeMedicineExportReqVO();
       reqVO.setHomeId(null);
       reqVO.setMedicineName(null);
       reqVO.setDoctorId(null);
       reqVO.setDoctorName(null);
       reqVO.setDosage(null);
       reqVO.setIndications(null);
       reqVO.setMedicineUsage(null);
       reqVO.setStartTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setEndTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setComment(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       List<HomeMedicineDO> list = homeMedicineService.getHomeMedicineList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbHomeMedicine, list.get(0));
    }

}
