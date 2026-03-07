package com.sqlapk.medicine.module.patient.service.medicinereconciliationdetail;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.test.core.ut.BaseDbUnitTest;
import com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliationdetail.vo.MedicineReconciliationDetailCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliationdetail.vo.MedicineReconciliationDetailExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliationdetail.vo.MedicineReconciliationDetailPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.medicinereconciliationdetail.vo.MedicineReconciliationDetailUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.medicinereconciliationdetail.MedicineReconciliationDetailDO;
import com.sqlapk.medicine.module.patient.dal.mysql.medicinereconciliationdetail.MedicineReconciliationDetailMapper;
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
import static com.sqlapk.medicine.module.patient.enums.ErrorCodeConstants.MEDICINE_RECONCILIATION_DETAIL_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link MedicineReconciliationDetailServiceImpl} 的单元测试类
 *
 * @author 管理员
 */
@Import(MedicineReconciliationDetailServiceImpl.class)
public class MedicineReconciliationDetailServiceImplTest extends BaseDbUnitTest {

    @Resource
    private MedicineReconciliationDetailServiceImpl medicineReconciliationDetailService;

    @Resource
    private MedicineReconciliationDetailMapper medicineReconciliationDetailMapper;

    @Test
    public void testCreateMedicineReconciliationDetail_success() {
        // 准备参数
        MedicineReconciliationDetailCreateReqVO reqVO = randomPojo(MedicineReconciliationDetailCreateReqVO.class);

        // 调用
        Long medicineReconciliationDetailId = medicineReconciliationDetailService.createMedicineReconciliationDetail(reqVO);
        // 断言
        assertNotNull(medicineReconciliationDetailId);
        // 校验记录的属性是否正确
        MedicineReconciliationDetailDO medicineReconciliationDetail = medicineReconciliationDetailMapper.selectById(medicineReconciliationDetailId);
        assertPojoEquals(reqVO, medicineReconciliationDetail);
    }

    @Test
    public void testUpdateMedicineReconciliationDetail_success() {
        // mock 数据
        MedicineReconciliationDetailDO dbMedicineReconciliationDetail = randomPojo(MedicineReconciliationDetailDO.class);
        medicineReconciliationDetailMapper.insert(dbMedicineReconciliationDetail);// @Sql: 先插入出一条存在的数据
        // 准备参数
        MedicineReconciliationDetailUpdateReqVO reqVO = randomPojo(MedicineReconciliationDetailUpdateReqVO.class, o -> {
            o.setId(dbMedicineReconciliationDetail.getId()); // 设置更新的 ID
        });

        // 调用
        medicineReconciliationDetailService.updateMedicineReconciliationDetail(reqVO);
        // 校验是否更新正确
        MedicineReconciliationDetailDO medicineReconciliationDetail = medicineReconciliationDetailMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, medicineReconciliationDetail);
    }

    @Test
    public void testUpdateMedicineReconciliationDetail_notExists() {
        // 准备参数
        MedicineReconciliationDetailUpdateReqVO reqVO = randomPojo(MedicineReconciliationDetailUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> medicineReconciliationDetailService.updateMedicineReconciliationDetail(reqVO), MEDICINE_RECONCILIATION_DETAIL_NOT_EXISTS);
    }

    @Test
    public void testDeleteMedicineReconciliationDetail_success() {
        // mock 数据
        MedicineReconciliationDetailDO dbMedicineReconciliationDetail = randomPojo(MedicineReconciliationDetailDO.class);
        medicineReconciliationDetailMapper.insert(dbMedicineReconciliationDetail);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbMedicineReconciliationDetail.getId();

        // 调用
        medicineReconciliationDetailService.deleteMedicineReconciliationDetail(id);
       // 校验数据不存在了
       assertNull(medicineReconciliationDetailMapper.selectById(id));
    }

    @Test
    public void testDeleteMedicineReconciliationDetail_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> medicineReconciliationDetailService.deleteMedicineReconciliationDetail(id), MEDICINE_RECONCILIATION_DETAIL_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetMedicineReconciliationDetailPage() {
       // mock 数据
       MedicineReconciliationDetailDO dbMedicineReconciliationDetail = randomPojo(MedicineReconciliationDetailDO.class, o -> { // 等会查询到
           o.setReconciliationId(null);
           o.setMedicineName(null);
           o.setDosage(null);
           o.setUsageReason(null);
           o.setStartTime(null);
           o.setEndTime(null);
           o.setComment(null);
           o.setCreateTime(null);
       });
       medicineReconciliationDetailMapper.insert(dbMedicineReconciliationDetail);
       // 测试 reconciliationId 不匹配
       medicineReconciliationDetailMapper.insert(cloneIgnoreId(dbMedicineReconciliationDetail, o -> o.setReconciliationId(null)));
       // 测试 medicineName 不匹配
       medicineReconciliationDetailMapper.insert(cloneIgnoreId(dbMedicineReconciliationDetail, o -> o.setMedicineName(null)));
       // 测试 dosage 不匹配
       medicineReconciliationDetailMapper.insert(cloneIgnoreId(dbMedicineReconciliationDetail, o -> o.setDosage(null)));
       // 测试 usageReason 不匹配
       medicineReconciliationDetailMapper.insert(cloneIgnoreId(dbMedicineReconciliationDetail, o -> o.setUsageReason(null)));
       // 测试 startTime 不匹配
       medicineReconciliationDetailMapper.insert(cloneIgnoreId(dbMedicineReconciliationDetail, o -> o.setStartTime(null)));
       // 测试 endTime 不匹配
       medicineReconciliationDetailMapper.insert(cloneIgnoreId(dbMedicineReconciliationDetail, o -> o.setEndTime(null)));
       // 测试 comment 不匹配
       medicineReconciliationDetailMapper.insert(cloneIgnoreId(dbMedicineReconciliationDetail, o -> o.setComment(null)));
       // 测试 createTime 不匹配
       medicineReconciliationDetailMapper.insert(cloneIgnoreId(dbMedicineReconciliationDetail, o -> o.setCreateTime(null)));
       // 准备参数
       MedicineReconciliationDetailPageReqVO reqVO = new MedicineReconciliationDetailPageReqVO();
       reqVO.setReconciliationId(null);
       reqVO.setMedicineName(null);
       reqVO.setDosage(null);
       reqVO.setUsageReason(null);
       reqVO.setStartTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setEndTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setComment(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<MedicineReconciliationDetailDO> pageResult = medicineReconciliationDetailService.getMedicineReconciliationDetailPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbMedicineReconciliationDetail, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetMedicineReconciliationDetailList() {
       // mock 数据
       MedicineReconciliationDetailDO dbMedicineReconciliationDetail = randomPojo(MedicineReconciliationDetailDO.class, o -> { // 等会查询到
           o.setReconciliationId(null);
           o.setMedicineName(null);
           o.setDosage(null);
           o.setUsageReason(null);
           o.setStartTime(null);
           o.setEndTime(null);
           o.setComment(null);
           o.setCreateTime(null);
       });
       medicineReconciliationDetailMapper.insert(dbMedicineReconciliationDetail);
       // 测试 reconciliationId 不匹配
       medicineReconciliationDetailMapper.insert(cloneIgnoreId(dbMedicineReconciliationDetail, o -> o.setReconciliationId(null)));
       // 测试 medicineName 不匹配
       medicineReconciliationDetailMapper.insert(cloneIgnoreId(dbMedicineReconciliationDetail, o -> o.setMedicineName(null)));
       // 测试 dosage 不匹配
       medicineReconciliationDetailMapper.insert(cloneIgnoreId(dbMedicineReconciliationDetail, o -> o.setDosage(null)));
       // 测试 usageReason 不匹配
       medicineReconciliationDetailMapper.insert(cloneIgnoreId(dbMedicineReconciliationDetail, o -> o.setUsageReason(null)));
       // 测试 startTime 不匹配
       medicineReconciliationDetailMapper.insert(cloneIgnoreId(dbMedicineReconciliationDetail, o -> o.setStartTime(null)));
       // 测试 endTime 不匹配
       medicineReconciliationDetailMapper.insert(cloneIgnoreId(dbMedicineReconciliationDetail, o -> o.setEndTime(null)));
       // 测试 comment 不匹配
       medicineReconciliationDetailMapper.insert(cloneIgnoreId(dbMedicineReconciliationDetail, o -> o.setComment(null)));
       // 测试 createTime 不匹配
       medicineReconciliationDetailMapper.insert(cloneIgnoreId(dbMedicineReconciliationDetail, o -> o.setCreateTime(null)));
       // 准备参数
       MedicineReconciliationDetailExportReqVO reqVO = new MedicineReconciliationDetailExportReqVO();
       reqVO.setReconciliationId(null);
       reqVO.setMedicineName(null);
       reqVO.setDosage(null);
       reqVO.setUsageReason(null);
       reqVO.setStartTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setEndTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setComment(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       List<MedicineReconciliationDetailDO> list = medicineReconciliationDetailService.getMedicineReconciliationDetailList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbMedicineReconciliationDetail, list.get(0));
    }

}
