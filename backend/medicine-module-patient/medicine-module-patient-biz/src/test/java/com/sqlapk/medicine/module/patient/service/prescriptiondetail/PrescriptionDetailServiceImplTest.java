package com.sqlapk.medicine.module.patient.service.prescriptiondetail;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.test.core.ut.BaseDbUnitTest;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptiondetail.vo.PrescriptionDetailCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptiondetail.vo.PrescriptionDetailExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptiondetail.vo.PrescriptionDetailPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.prescriptiondetail.vo.PrescriptionDetailUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.prescriptiondetail.PrescriptionDetailDO;
import com.sqlapk.medicine.module.patient.dal.mysql.prescriptiondetail.PrescriptionDetailMapper;
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
import static com.sqlapk.medicine.module.patient.enums.ErrorCodeConstants.PRESCRIPTION_DETAIL_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link PrescriptionDetailServiceImpl} 的单元测试类
 *
 * @author 管理员
 */
@Import(PrescriptionDetailServiceImpl.class)
public class PrescriptionDetailServiceImplTest extends BaseDbUnitTest {

    @Resource
    private PrescriptionDetailServiceImpl prescriptionDetailService;

    @Resource
    private PrescriptionDetailMapper prescriptionDetailMapper;

    @Test
    public void testCreatePrescriptionDetail_success() {
        // 准备参数
        PrescriptionDetailCreateReqVO reqVO = randomPojo(PrescriptionDetailCreateReqVO.class);

        // 调用
        Long prescriptionDetailId = prescriptionDetailService.createPrescriptionDetail(reqVO);
        // 断言
        assertNotNull(prescriptionDetailId);
        // 校验记录的属性是否正确
        PrescriptionDetailDO prescriptionDetail = prescriptionDetailMapper.selectById(prescriptionDetailId);
        assertPojoEquals(reqVO, prescriptionDetail);
    }

    @Test
    public void testUpdatePrescriptionDetail_success() {
        // mock 数据
        PrescriptionDetailDO dbPrescriptionDetail = randomPojo(PrescriptionDetailDO.class);
        prescriptionDetailMapper.insert(dbPrescriptionDetail);// @Sql: 先插入出一条存在的数据
        // 准备参数
        PrescriptionDetailUpdateReqVO reqVO = randomPojo(PrescriptionDetailUpdateReqVO.class, o -> {
            o.setId(dbPrescriptionDetail.getId()); // 设置更新的 ID
        });

        // 调用
        prescriptionDetailService.updatePrescriptionDetail(reqVO);
        // 校验是否更新正确
        PrescriptionDetailDO prescriptionDetail = prescriptionDetailMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, prescriptionDetail);
    }

    @Test
    public void testUpdatePrescriptionDetail_notExists() {
        // 准备参数
        PrescriptionDetailUpdateReqVO reqVO = randomPojo(PrescriptionDetailUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> prescriptionDetailService.updatePrescriptionDetail(reqVO), PRESCRIPTION_DETAIL_NOT_EXISTS);
    }

    @Test
    public void testDeletePrescriptionDetail_success() {
        // mock 数据
        PrescriptionDetailDO dbPrescriptionDetail = randomPojo(PrescriptionDetailDO.class);
        prescriptionDetailMapper.insert(dbPrescriptionDetail);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbPrescriptionDetail.getId();

        // 调用
        prescriptionDetailService.deletePrescriptionDetail(id);
       // 校验数据不存在了
       assertNull(prescriptionDetailMapper.selectById(id));
    }

    @Test
    public void testDeletePrescriptionDetail_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> prescriptionDetailService.deletePrescriptionDetail(id), PRESCRIPTION_DETAIL_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetPrescriptionDetailPage() {
       // mock 数据
       PrescriptionDetailDO dbPrescriptionDetail = randomPojo(PrescriptionDetailDO.class, o -> { // 等会查询到
           o.setPrescriptionId(null);
           o.setMedicineId(null);
           o.setDosage(null);
           o.setUsageInfo(null);
           o.setFrequency(null);
           o.setQuality(null);
           o.setUnit(null);
           o.setCreateTime(null);
       });
       prescriptionDetailMapper.insert(dbPrescriptionDetail);
       // 测试 prescriptionId 不匹配
       prescriptionDetailMapper.insert(cloneIgnoreId(dbPrescriptionDetail, o -> o.setPrescriptionId(null)));
       // 测试 medicineId 不匹配
       prescriptionDetailMapper.insert(cloneIgnoreId(dbPrescriptionDetail, o -> o.setMedicineId(null)));
       // 测试 dosage 不匹配
       prescriptionDetailMapper.insert(cloneIgnoreId(dbPrescriptionDetail, o -> o.setDosage(null)));
       // 测试 usage 不匹配
       prescriptionDetailMapper.insert(cloneIgnoreId(dbPrescriptionDetail, o -> o.setUsageInfo(null)));
       // 测试 frequency 不匹配
       prescriptionDetailMapper.insert(cloneIgnoreId(dbPrescriptionDetail, o -> o.setFrequency(null)));
       // 测试 quality 不匹配
       prescriptionDetailMapper.insert(cloneIgnoreId(dbPrescriptionDetail, o -> o.setQuality(null)));
       // 测试 unit 不匹配
       prescriptionDetailMapper.insert(cloneIgnoreId(dbPrescriptionDetail, o -> o.setUnit(null)));
       // 测试 createTime 不匹配
       prescriptionDetailMapper.insert(cloneIgnoreId(dbPrescriptionDetail, o -> o.setCreateTime(null)));
       // 准备参数
       PrescriptionDetailPageReqVO reqVO = new PrescriptionDetailPageReqVO();
       reqVO.setPrescriptionId(null);
       reqVO.setMedicineId(null);
       reqVO.setDosage(null);
       reqVO.setUsageInfo(null);
       reqVO.setFrequency(null);
       reqVO.setQuality(null);
       reqVO.setUnit(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<PrescriptionDetailDO> pageResult = prescriptionDetailService.getPrescriptionDetailPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbPrescriptionDetail, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetPrescriptionDetailList() {
       // mock 数据
       PrescriptionDetailDO dbPrescriptionDetail = randomPojo(PrescriptionDetailDO.class, o -> { // 等会查询到
           o.setPrescriptionId(null);
           o.setMedicineId(null);
           o.setDosage(null);
           o.setUsageInfo(null);
           o.setFrequency(null);
           o.setQuality(null);
           o.setUnit(null);
           o.setCreateTime(null);
       });
       prescriptionDetailMapper.insert(dbPrescriptionDetail);
       // 测试 prescriptionId 不匹配
       prescriptionDetailMapper.insert(cloneIgnoreId(dbPrescriptionDetail, o -> o.setPrescriptionId(null)));
       // 测试 medicineId 不匹配
       prescriptionDetailMapper.insert(cloneIgnoreId(dbPrescriptionDetail, o -> o.setMedicineId(null)));
       // 测试 dosage 不匹配
       prescriptionDetailMapper.insert(cloneIgnoreId(dbPrescriptionDetail, o -> o.setDosage(null)));
       // 测试 usage 不匹配
       prescriptionDetailMapper.insert(cloneIgnoreId(dbPrescriptionDetail, o -> o.setUsageInfo(null)));
       // 测试 frequency 不匹配
       prescriptionDetailMapper.insert(cloneIgnoreId(dbPrescriptionDetail, o -> o.setFrequency(null)));
       // 测试 quality 不匹配
       prescriptionDetailMapper.insert(cloneIgnoreId(dbPrescriptionDetail, o -> o.setQuality(null)));
       // 测试 unit 不匹配
       prescriptionDetailMapper.insert(cloneIgnoreId(dbPrescriptionDetail, o -> o.setUnit(null)));
       // 测试 createTime 不匹配
       prescriptionDetailMapper.insert(cloneIgnoreId(dbPrescriptionDetail, o -> o.setCreateTime(null)));
       // 准备参数
       PrescriptionDetailExportReqVO reqVO = new PrescriptionDetailExportReqVO();
       reqVO.setPrescriptionId(null);
       reqVO.setMedicineId(null);
       reqVO.setDosage(null);
       reqVO.setUsageInfo(null);
       reqVO.setFrequency(null);
       reqVO.setQuality(null);
       reqVO.setUnit(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       List<PrescriptionDetailDO> list = prescriptionDetailService.getPrescriptionDetailList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbPrescriptionDetail, list.get(0));
    }

}
