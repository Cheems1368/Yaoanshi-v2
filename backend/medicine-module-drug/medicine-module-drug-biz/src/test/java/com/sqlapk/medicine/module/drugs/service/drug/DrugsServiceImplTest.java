//package com.sqlapk.medicine.module.drugs.service.drug;
//
//import org.junit.jupiter.api.Disabled;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.mock.mockito.MockBean;
//
//import javax.annotation.Resource;
//
//import com.sqlapk.medicine.framework.test.core.ut.BaseDbUnitTest;
//
//import com.sqlapk.medicine.module.drugs.controller.admin.drug.vo.*;
//import com.sqlapk.medicine.module.drugs.dal.dataobject.drug.DrugsDO;
//import com.sqlapk.medicine.module.drugs.dal.mysql.drug.DrugsMapper;
//import com.sqlapk.medicine.framework.common.pojo.PageResult;
//
//import javax.annotation.Resource;
//import org.springframework.context.annotation.Import;
//import java.util.*;
//import java.time.LocalDateTime;
//
//import static cn.hutool.core.util.RandomUtil.*;
//import static com.sqlapk.medicine.module.drugs.enums.ErrorCodeConstants.*;
//import static com.sqlapk.medicine.framework.test.core.util.AssertUtils.*;
//import static com.sqlapk.medicine.framework.test.core.util.RandomUtils.*;
//import static com.sqlapk.medicine.framework.common.util.date.LocalDateTimeUtils.*;
//import static com.sqlapk.medicine.framework.common.util.object.ObjectUtils.*;
//import static com.sqlapk.medicine.framework.common.util.date.DateUtils.*;
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
///**
// * {@link DrugsServiceImpl} 的单元测试类
// *
// * @author 管理员
// */
//@Import(DrugsServiceImpl.class)
//public class DrugsServiceImplTest extends BaseDbUnitTest {
//
//    @Resource
//    private DrugsServiceImpl Service;
//
//    @Resource
//    private DrugsMapper Mapper;
//
//    @Test
//    public void testCreate_success() {
//        // 准备参数
//        DrugsCreateReqVO reqVO = randomPojo(DrugsCreateReqVO.class);
//
//        // 调用
//        Integer Id = Service.create(reqVO);
//        // 断言
//        assertNotNull(Id);
//        // 校验记录的属性是否正确
//        DrugsDO  = Mapper.selectById(Id);
//        assertPojoEquals(reqVO, );
//    }
//
//    @Test
//    public void testUpdate_success() {
//        // mock 数据
//        DrugsDO db = randomPojo(DrugsDO.class);
//        Mapper.insert(db);// @Sql: 先插入出一条存在的数据
//        // 准备参数
//        DrugsUpdateReqVO reqVO = randomPojo(DrugsUpdateReqVO.class, o -> {
//            o.setDrugId(db.getDrugId()); // 设置更新的 ID
//        });
//
//        // 调用
//        Service.update(reqVO);
//        // 校验是否更新正确
//        DrugsDO  = Mapper.selectById(reqVO.getId()); // 获取最新的
//        assertPojoEquals(reqVO, );
//    }
//
//    @Test
//    public void testUpdate_notExists() {
//        // 准备参数
//        DrugsUpdateReqVO reqVO = randomPojo(DrugsUpdateReqVO.class);
//
//        // 调用, 并断言异常
//        assertServiceException(() -> Service.update(reqVO), _NOT_EXISTS);
//    }
//
//    @Test
//    public void testDelete_success() {
//        // mock 数据
//        DrugsDO db = randomPojo(DrugsDO.class);
//        Mapper.insert(db);// @Sql: 先插入出一条存在的数据
//        // 准备参数
//        Integer id = db.getId();
//
//        // 调用
//        Service.delete(id);
//       // 校验数据不存在了
//       assertNull(Mapper.selectById(id));
//    }
//
//    @Test
//    public void testDelete_notExists() {
//        // 准备参数
//        Integer id = randomIntegerId();
//
//        // 调用, 并断言异常
//        assertServiceException(() -> Service.delete(id), _NOT_EXISTS);
//    }
//
//    @Test
//    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
//    public void testGetPage() {
//       // mock 数据
//       DrugsDO db = randomPojo(DrugsDO.class, o -> { // 等会查询到
//           o.setDrugName(null);
//           o.setSpecification(null);
//           o.setManufacturer(null);
//           o.setApprovalNumber(null);
//           o.setExpiryDate(null);
//           o.setCategory(null);
//           o.setDosageForm(null);
//           o.setIngredients(null);
//           o.setIndications(null);
//           o.setDosage(null);
//           o.setSideEffects(null);
//           o.setContraindications(null);
//           o.setPrecautions(null);
//           o.setInteractions(null);
//           o.setImageUrl(null);
//       });
//       Mapper.insert(db);
//       // 测试 drugName 不匹配
//       Mapper.insert(cloneIgnoreId(db, o -> o.setDrugName(null)));
//       // 测试 specification 不匹配
//       Mapper.insert(cloneIgnoreId(db, o -> o.setSpecification(null)));
//       // 测试 manufacturer 不匹配
//       Mapper.insert(cloneIgnoreId(db, o -> o.setManufacturer(null)));
//       // 测试 approvalNumber 不匹配
//       Mapper.insert(cloneIgnoreId(db, o -> o.setApprovalNumber(null)));
//       // 测试 expiryDate 不匹配
//       Mapper.insert(cloneIgnoreId(db, o -> o.setExpiryDate(null)));
//       // 测试 category 不匹配
//       Mapper.insert(cloneIgnoreId(db, o -> o.setCategory(null)));
//       // 测试 dosageForm 不匹配
//       Mapper.insert(cloneIgnoreId(db, o -> o.setDosageForm(null)));
//       // 测试 ingredients 不匹配
//       Mapper.insert(cloneIgnoreId(db, o -> o.setIngredients(null)));
//       // 测试 indications 不匹配
//       Mapper.insert(cloneIgnoreId(db, o -> o.setIndications(null)));
//       // 测试 dosage 不匹配
//       Mapper.insert(cloneIgnoreId(db, o -> o.setDosage(null)));
//       // 测试 sideEffects 不匹配
//       Mapper.insert(cloneIgnoreId(db, o -> o.setSideEffects(null)));
//       // 测试 contraindications 不匹配
//       Mapper.insert(cloneIgnoreId(db, o -> o.setContraindications(null)));
//       // 测试 precautions 不匹配
//       Mapper.insert(cloneIgnoreId(db, o -> o.setPrecautions(null)));
//       // 测试 interactions 不匹配
//       Mapper.insert(cloneIgnoreId(db, o -> o.setInteractions(null)));
//       // 测试 imageUrl 不匹配
//       Mapper.insert(cloneIgnoreId(db, o -> o.setImageUrl(null)));
//       // 准备参数
//       DrugsPageReqVO reqVO = new DrugsPageReqVO();
//       reqVO.setDrugName(null);
//       reqVO.setSpecification(null);
//       reqVO.setManufacturer(null);
//       reqVO.setApprovalNumber(null);
//       reqVO.setExpiryDate(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
//       reqVO.setCategory(null);
//       reqVO.setDosageForm(null);
//       reqVO.setIngredients(null);
//       reqVO.setIndications(null);
//       reqVO.setDosage(null);
//       reqVO.setSideEffects(null);
//       reqVO.setContraindications(null);
//       reqVO.setPrecautions(null);
//       reqVO.setInteractions(null);
//       reqVO.setImageUrl(null);
//
//       // 调用
//       PageResult<DrugsDO> pageResult = Service.getPage(reqVO);
//       // 断言
//       assertEquals(1, pageResult.getTotal());
//       assertEquals(1, pageResult.getList().size());
//       assertPojoEquals(db, pageResult.getList().get(0));
//    }
//
//    @Test
//    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
//    public void testGetList() {
//       // mock 数据
//       DrugsDO db = randomPojo(DrugsDO.class, o -> { // 等会查询到
//           o.setDrugName(null);
//           o.setSpecification(null);
//           o.setManufacturer(null);
//           o.setApprovalNumber(null);
//           o.setExpiryDate(null);
//           o.setCategory(null);
//           o.setDosageForm(null);
//           o.setIngredients(null);
//           o.setIndications(null);
//           o.setDosage(null);
//           o.setSideEffects(null);
//           o.setContraindications(null);
//           o.setPrecautions(null);
//           o.setInteractions(null);
//           o.setImageUrl(null);
//       });
//       Mapper.insert(db);
//       // 测试 drugName 不匹配
//       Mapper.insert(cloneIgnoreId(db, o -> o.setDrugName(null)));
//       // 测试 specification 不匹配
//       Mapper.insert(cloneIgnoreId(db, o -> o.setSpecification(null)));
//       // 测试 manufacturer 不匹配
//       Mapper.insert(cloneIgnoreId(db, o -> o.setManufacturer(null)));
//       // 测试 approvalNumber 不匹配
//       Mapper.insert(cloneIgnoreId(db, o -> o.setApprovalNumber(null)));
//       // 测试 expiryDate 不匹配
//       Mapper.insert(cloneIgnoreId(db, o -> o.setExpiryDate(null)));
//       // 测试 category 不匹配
//       Mapper.insert(cloneIgnoreId(db, o -> o.setCategory(null)));
//       // 测试 dosageForm 不匹配
//       Mapper.insert(cloneIgnoreId(db, o -> o.setDosageForm(null)));
//       // 测试 ingredients 不匹配
//       Mapper.insert(cloneIgnoreId(db, o -> o.setIngredients(null)));
//       // 测试 indications 不匹配
//       Mapper.insert(cloneIgnoreId(db, o -> o.setIndications(null)));
//       // 测试 dosage 不匹配
//       Mapper.insert(cloneIgnoreId(db, o -> o.setDosage(null)));
//       // 测试 sideEffects 不匹配
//       Mapper.insert(cloneIgnoreId(db, o -> o.setSideEffects(null)));
//       // 测试 contraindications 不匹配
//       Mapper.insert(cloneIgnoreId(db, o -> o.setContraindications(null)));
//       // 测试 precautions 不匹配
//       Mapper.insert(cloneIgnoreId(db, o -> o.setPrecautions(null)));
//       // 测试 interactions 不匹配
//       Mapper.insert(cloneIgnoreId(db, o -> o.setInteractions(null)));
//       // 测试 imageUrl 不匹配
//       Mapper.insert(cloneIgnoreId(db, o -> o.setImageUrl(null)));
//       // 准备参数
//       DrugsExportReqVO reqVO = new DrugsExportReqVO();
//       reqVO.setDrugName(null);
//       reqVO.setSpecification(null);
//       reqVO.setManufacturer(null);
//       reqVO.setApprovalNumber(null);
//       reqVO.setExpiryDate(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
//       reqVO.setCategory(null);
//       reqVO.setDosageForm(null);
//       reqVO.setIngredients(null);
//       reqVO.setIndications(null);
//       reqVO.setDosage(null);
//       reqVO.setSideEffects(null);
//       reqVO.setContraindications(null);
//       reqVO.setPrecautions(null);
//       reqVO.setInteractions(null);
//       reqVO.setImageUrl(null);
//
//       // 调用
//       List<DrugsDO> list = Service.getList(reqVO);
//       // 断言
//       assertEquals(1, list.size());
//       assertPojoEquals(db, list.get(0));
//    }
//
//}
