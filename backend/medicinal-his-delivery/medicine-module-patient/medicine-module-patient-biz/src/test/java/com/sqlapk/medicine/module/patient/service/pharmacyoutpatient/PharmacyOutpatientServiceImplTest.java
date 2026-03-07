package com.sqlapk.medicine.module.patient.service.pharmacyoutpatient;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.test.core.ut.BaseDbUnitTest;
import com.sqlapk.medicine.module.patient.controller.admin.pharmacyoutpatient.vo.PharmacyOutpatientCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.pharmacyoutpatient.vo.PharmacyOutpatientExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.pharmacyoutpatient.vo.PharmacyOutpatientPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.pharmacyoutpatient.vo.PharmacyOutpatientUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.pharmacyoutpatient.PharmacyOutpatientDO;
import com.sqlapk.medicine.module.patient.dal.mysql.pharmacyoutpatient.PharmacyOutpatientMapper;
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
import static com.sqlapk.medicine.module.patient.enums.ErrorCodeConstants.PHARMACY_OUTPATIENT_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link PharmacyOutpatientServiceImpl} 的单元测试类
 *
 * @author 管理员
 */
@Import(PharmacyOutpatientServiceImpl.class)
public class PharmacyOutpatientServiceImplTest extends BaseDbUnitTest {

    @Resource
    private PharmacyOutpatientServiceImpl pharmacyOutpatientService;

    @Resource
    private PharmacyOutpatientMapper pharmacyOutpatientMapper;

    @Test
    public void testCreatePharmacyOutpatient_success() {
        // 准备参数
        PharmacyOutpatientCreateReqVO reqVO = randomPojo(PharmacyOutpatientCreateReqVO.class);

        // 调用
        Long pharmacyOutpatientId = pharmacyOutpatientService.createPharmacyOutpatient(reqVO);
        // 断言
        assertNotNull(pharmacyOutpatientId);
        // 校验记录的属性是否正确
        PharmacyOutpatientDO pharmacyOutpatient = pharmacyOutpatientMapper.selectById(pharmacyOutpatientId);
        assertPojoEquals(reqVO, pharmacyOutpatient);
    }

    @Test
    public void testUpdatePharmacyOutpatient_success() {
        // mock 数据
        PharmacyOutpatientDO dbPharmacyOutpatient = randomPojo(PharmacyOutpatientDO.class);
        pharmacyOutpatientMapper.insert(dbPharmacyOutpatient);// @Sql: 先插入出一条存在的数据
        // 准备参数
        PharmacyOutpatientUpdateReqVO reqVO = randomPojo(PharmacyOutpatientUpdateReqVO.class, o -> {
            o.setId(dbPharmacyOutpatient.getId()); // 设置更新的 ID
        });

        // 调用
        pharmacyOutpatientService.updatePharmacyOutpatient(reqVO);
        // 校验是否更新正确
        PharmacyOutpatientDO pharmacyOutpatient = pharmacyOutpatientMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, pharmacyOutpatient);
    }

    @Test
    public void testUpdatePharmacyOutpatient_notExists() {
        // 准备参数
        PharmacyOutpatientUpdateReqVO reqVO = randomPojo(PharmacyOutpatientUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> pharmacyOutpatientService.updatePharmacyOutpatient(reqVO), PHARMACY_OUTPATIENT_NOT_EXISTS);
    }

    @Test
    public void testDeletePharmacyOutpatient_success() {
        // mock 数据
        PharmacyOutpatientDO dbPharmacyOutpatient = randomPojo(PharmacyOutpatientDO.class);
        pharmacyOutpatientMapper.insert(dbPharmacyOutpatient);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbPharmacyOutpatient.getId();

        // 调用
        pharmacyOutpatientService.deletePharmacyOutpatient(id);
       // 校验数据不存在了
       assertNull(pharmacyOutpatientMapper.selectById(id));
    }

    @Test
    public void testDeletePharmacyOutpatient_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> pharmacyOutpatientService.deletePharmacyOutpatient(id), PHARMACY_OUTPATIENT_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetPharmacyOutpatientPage() {
       // mock 数据
       PharmacyOutpatientDO dbPharmacyOutpatient = randomPojo(PharmacyOutpatientDO.class, o -> { // 等会查询到
           o.setPrescriptionId(null);
           o.setMedicineName(null);
           o.setMedicineUsage(null);
           o.setStartUsageTime(null);
           o.setStopUsageTime(null);
           o.setStopReason(null);
           o.setMrpType(null);
           o.setWeight(null);
           o.setMrpDesc(null);
           o.setCreateTime(null);
       });
       pharmacyOutpatientMapper.insert(dbPharmacyOutpatient);
       // 测试 prescriptionId 不匹配
       pharmacyOutpatientMapper.insert(cloneIgnoreId(dbPharmacyOutpatient, o -> o.setPrescriptionId(null)));
       // 测试 medicineName 不匹配
       pharmacyOutpatientMapper.insert(cloneIgnoreId(dbPharmacyOutpatient, o -> o.setMedicineName(null)));
       // 测试 medicineUsage 不匹配
       pharmacyOutpatientMapper.insert(cloneIgnoreId(dbPharmacyOutpatient, o -> o.setMedicineUsage(null)));
       // 测试 startUsageTime 不匹配
       pharmacyOutpatientMapper.insert(cloneIgnoreId(dbPharmacyOutpatient, o -> o.setStartUsageTime(null)));
       // 测试 stopUsageTime 不匹配
       pharmacyOutpatientMapper.insert(cloneIgnoreId(dbPharmacyOutpatient, o -> o.setStopUsageTime(null)));
       // 测试 stopReason 不匹配
       pharmacyOutpatientMapper.insert(cloneIgnoreId(dbPharmacyOutpatient, o -> o.setStopReason(null)));
       // 测试 mrpType 不匹配
       pharmacyOutpatientMapper.insert(cloneIgnoreId(dbPharmacyOutpatient, o -> o.setMrpType(null)));
       // 测试 weight 不匹配
       pharmacyOutpatientMapper.insert(cloneIgnoreId(dbPharmacyOutpatient, o -> o.setWeight(null)));
       // 测试 mrpDesc 不匹配
       pharmacyOutpatientMapper.insert(cloneIgnoreId(dbPharmacyOutpatient, o -> o.setMrpDesc(null)));
       // 测试 createTime 不匹配
       pharmacyOutpatientMapper.insert(cloneIgnoreId(dbPharmacyOutpatient, o -> o.setCreateTime(null)));
       // 准备参数
       PharmacyOutpatientPageReqVO reqVO = new PharmacyOutpatientPageReqVO();
       reqVO.setPrescriptionId(null);
       reqVO.setMedicineName(null);
       reqVO.setMedicineUsage(null);
       reqVO.setStartUsageTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setStopUsageTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setStopReason(null);
       reqVO.setMrpType(null);
       reqVO.setWeight(null);
       reqVO.setMrpDesc(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<PharmacyOutpatientDO> pageResult = pharmacyOutpatientService.getPharmacyOutpatientPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbPharmacyOutpatient, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetPharmacyOutpatientList() {
       // mock 数据
       PharmacyOutpatientDO dbPharmacyOutpatient = randomPojo(PharmacyOutpatientDO.class, o -> { // 等会查询到
           o.setPrescriptionId(null);
           o.setMedicineName(null);
           o.setMedicineUsage(null);
           o.setStartUsageTime(null);
           o.setStopUsageTime(null);
           o.setStopReason(null);
           o.setMrpType(null);
           o.setWeight(null);
           o.setMrpDesc(null);
           o.setCreateTime(null);
       });
       pharmacyOutpatientMapper.insert(dbPharmacyOutpatient);
       // 测试 prescriptionId 不匹配
       pharmacyOutpatientMapper.insert(cloneIgnoreId(dbPharmacyOutpatient, o -> o.setPrescriptionId(null)));
       // 测试 medicineName 不匹配
       pharmacyOutpatientMapper.insert(cloneIgnoreId(dbPharmacyOutpatient, o -> o.setMedicineName(null)));
       // 测试 medicineUsage 不匹配
       pharmacyOutpatientMapper.insert(cloneIgnoreId(dbPharmacyOutpatient, o -> o.setMedicineUsage(null)));
       // 测试 startUsageTime 不匹配
       pharmacyOutpatientMapper.insert(cloneIgnoreId(dbPharmacyOutpatient, o -> o.setStartUsageTime(null)));
       // 测试 stopUsageTime 不匹配
       pharmacyOutpatientMapper.insert(cloneIgnoreId(dbPharmacyOutpatient, o -> o.setStopUsageTime(null)));
       // 测试 stopReason 不匹配
       pharmacyOutpatientMapper.insert(cloneIgnoreId(dbPharmacyOutpatient, o -> o.setStopReason(null)));
       // 测试 mrpType 不匹配
       pharmacyOutpatientMapper.insert(cloneIgnoreId(dbPharmacyOutpatient, o -> o.setMrpType(null)));
       // 测试 weight 不匹配
       pharmacyOutpatientMapper.insert(cloneIgnoreId(dbPharmacyOutpatient, o -> o.setWeight(null)));
       // 测试 mrpDesc 不匹配
       pharmacyOutpatientMapper.insert(cloneIgnoreId(dbPharmacyOutpatient, o -> o.setMrpDesc(null)));
       // 测试 createTime 不匹配
       pharmacyOutpatientMapper.insert(cloneIgnoreId(dbPharmacyOutpatient, o -> o.setCreateTime(null)));
       // 准备参数
       PharmacyOutpatientExportReqVO reqVO = new PharmacyOutpatientExportReqVO();
       reqVO.setPrescriptionId(null);
       reqVO.setMedicineName(null);
       reqVO.setMedicineUsage(null);
       reqVO.setStartUsageTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setStopUsageTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setStopReason(null);
       reqVO.setMrpType(null);
       reqVO.setWeight(null);
       reqVO.setMrpDesc(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       List<PharmacyOutpatientDO> list = pharmacyOutpatientService.getPharmacyOutpatientList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbPharmacyOutpatient, list.get(0));
    }

}
