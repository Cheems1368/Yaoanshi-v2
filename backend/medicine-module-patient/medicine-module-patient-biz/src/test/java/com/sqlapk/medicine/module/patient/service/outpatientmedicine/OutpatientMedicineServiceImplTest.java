package com.sqlapk.medicine.module.patient.service.outpatientmedicine;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.test.core.ut.BaseDbUnitTest;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientmedicine.vo.OutpatientMedicineCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientmedicine.vo.OutpatientMedicineExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientmedicine.vo.OutpatientMedicinePageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.outpatientmedicine.vo.OutpatientMedicineUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.outpatientmedicine.OutpatientMedicineDO;
import com.sqlapk.medicine.module.patient.dal.mysql.outpatientmedicine.OutpatientMedicineMapper;
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
import static com.sqlapk.medicine.module.patient.enums.ErrorCodeConstants.OUTPATIENT_MEDICINE_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link OutpatientMedicineServiceImpl} 的单元测试类
 *
 * @author 管理员
 */
@Import(OutpatientMedicineServiceImpl.class)
public class OutpatientMedicineServiceImplTest extends BaseDbUnitTest {

    @Resource
    private OutpatientMedicineServiceImpl outpatientMedicineService;

    @Resource
    private OutpatientMedicineMapper outpatientMedicineMapper;

    @Test
    public void testCreateOutpatientMedicine_success() {
        // 准备参数
        OutpatientMedicineCreateReqVO reqVO = randomPojo(OutpatientMedicineCreateReqVO.class);

        // 调用
        Long outpatientMedicineId = outpatientMedicineService.createOutpatientMedicine(reqVO);
        // 断言
        assertNotNull(outpatientMedicineId);
        // 校验记录的属性是否正确
        OutpatientMedicineDO outpatientMedicine = outpatientMedicineMapper.selectById(outpatientMedicineId);
        assertPojoEquals(reqVO, outpatientMedicine);
    }

    @Test
    public void testUpdateOutpatientMedicine_success() {
        // mock 数据
        OutpatientMedicineDO dbOutpatientMedicine = randomPojo(OutpatientMedicineDO.class);
        outpatientMedicineMapper.insert(dbOutpatientMedicine);// @Sql: 先插入出一条存在的数据
        // 准备参数
        OutpatientMedicineUpdateReqVO reqVO = randomPojo(OutpatientMedicineUpdateReqVO.class, o -> {
            o.setId(dbOutpatientMedicine.getId()); // 设置更新的 ID
        });

        // 调用
        outpatientMedicineService.updateOutpatientMedicine(reqVO);
        // 校验是否更新正确
        OutpatientMedicineDO outpatientMedicine = outpatientMedicineMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, outpatientMedicine);
    }

    @Test
    public void testUpdateOutpatientMedicine_notExists() {
        // 准备参数
        OutpatientMedicineUpdateReqVO reqVO = randomPojo(OutpatientMedicineUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> outpatientMedicineService.updateOutpatientMedicine(reqVO), OUTPATIENT_MEDICINE_NOT_EXISTS);
    }

    @Test
    public void testDeleteOutpatientMedicine_success() {
        // mock 数据
        OutpatientMedicineDO dbOutpatientMedicine = randomPojo(OutpatientMedicineDO.class);
        outpatientMedicineMapper.insert(dbOutpatientMedicine);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbOutpatientMedicine.getId();

        // 调用
        outpatientMedicineService.deleteOutpatientMedicine(id);
       // 校验数据不存在了
       assertNull(outpatientMedicineMapper.selectById(id));
    }

    @Test
    public void testDeleteOutpatientMedicine_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> outpatientMedicineService.deleteOutpatientMedicine(id), OUTPATIENT_MEDICINE_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetOutpatientMedicinePage() {
       // mock 数据
       OutpatientMedicineDO dbOutpatientMedicine = randomPojo(OutpatientMedicineDO.class, o -> { // 等会查询到
           o.setCreateTime(null);
           o.setOutpatientId(null);
           o.setMeicineId(null);
       });
       outpatientMedicineMapper.insert(dbOutpatientMedicine);
       // 测试 createTime 不匹配
       outpatientMedicineMapper.insert(cloneIgnoreId(dbOutpatientMedicine, o -> o.setCreateTime(null)));
       // 测试 outpatientId 不匹配
       outpatientMedicineMapper.insert(cloneIgnoreId(dbOutpatientMedicine, o -> o.setOutpatientId(null)));
       // 测试 meicineId 不匹配
       outpatientMedicineMapper.insert(cloneIgnoreId(dbOutpatientMedicine, o -> o.setMeicineId(null)));
       // 准备参数
       OutpatientMedicinePageReqVO reqVO = new OutpatientMedicinePageReqVO();
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setOutpatientId(null);
       reqVO.setMeicineId(null);

       // 调用
       PageResult<OutpatientMedicineDO> pageResult = outpatientMedicineService.getOutpatientMedicinePage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbOutpatientMedicine, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetOutpatientMedicineList() {
       // mock 数据
       OutpatientMedicineDO dbOutpatientMedicine = randomPojo(OutpatientMedicineDO.class, o -> { // 等会查询到
           o.setCreateTime(null);
           o.setOutpatientId(null);
           o.setMeicineId(null);
       });
       outpatientMedicineMapper.insert(dbOutpatientMedicine);
       // 测试 createTime 不匹配
       outpatientMedicineMapper.insert(cloneIgnoreId(dbOutpatientMedicine, o -> o.setCreateTime(null)));
       // 测试 outpatientId 不匹配
       outpatientMedicineMapper.insert(cloneIgnoreId(dbOutpatientMedicine, o -> o.setOutpatientId(null)));
       // 测试 meicineId 不匹配
       outpatientMedicineMapper.insert(cloneIgnoreId(dbOutpatientMedicine, o -> o.setMeicineId(null)));
       // 准备参数
       OutpatientMedicineExportReqVO reqVO = new OutpatientMedicineExportReqVO();
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setOutpatientId(null);
       reqVO.setMeicineId(null);

       // 调用
       List<OutpatientMedicineDO> list = outpatientMedicineService.getOutpatientMedicineList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbOutpatientMedicine, list.get(0));
    }

}
