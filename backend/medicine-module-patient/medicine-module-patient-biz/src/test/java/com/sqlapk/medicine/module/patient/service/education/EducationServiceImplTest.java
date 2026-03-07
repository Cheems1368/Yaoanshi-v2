package com.sqlapk.medicine.module.patient.service.education;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.framework.test.core.ut.BaseDbUnitTest;
import com.sqlapk.medicine.module.patient.controller.admin.education.vo.EducationCreateReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.education.vo.EducationExportReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.education.vo.EducationPageReqVO;
import com.sqlapk.medicine.module.patient.controller.admin.education.vo.EducationUpdateReqVO;
import com.sqlapk.medicine.module.patient.dal.dataobject.education.EducationDO;
import com.sqlapk.medicine.module.patient.dal.mysql.education.EducationMapper;
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
import static com.sqlapk.medicine.module.patient.enums.ErrorCodeConstants.EDUCTION_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link EducationServiceImpl} 的单元测试类
 *
 * @author 管理员
 */
@Import(EducationServiceImpl.class)
public class EducationServiceImplTest extends BaseDbUnitTest {

    @Resource
    private EducationServiceImpl eductionService;

    @Resource
    private EducationMapper eductionMapper;

    @Test
    public void testCreateEduction_success() {
        // 准备参数
        EducationCreateReqVO reqVO = randomPojo(EducationCreateReqVO.class);

        // 调用
        Long eductionId = eductionService.createEduction(reqVO);
        // 断言
        assertNotNull(eductionId);
        // 校验记录的属性是否正确
        EducationDO eduction = eductionMapper.selectById(eductionId);
        assertPojoEquals(reqVO, eduction);
    }

    @Test
    public void testUpdateEduction_success() {
        // mock 数据
        EducationDO dbEduction = randomPojo(EducationDO.class);
        eductionMapper.insert(dbEduction);// @Sql: 先插入出一条存在的数据
        // 准备参数
        EducationUpdateReqVO reqVO = randomPojo(EducationUpdateReqVO.class, o -> {
            o.setId(dbEduction.getId()); // 设置更新的 ID
        });

        // 调用
        eductionService.updateEduction(reqVO);
        // 校验是否更新正确
        EducationDO eduction = eductionMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, eduction);
    }

    @Test
    public void testUpdateEduction_notExists() {
        // 准备参数
        EducationUpdateReqVO reqVO = randomPojo(EducationUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> eductionService.updateEduction(reqVO), EDUCTION_NOT_EXISTS);
    }

    @Test
    public void testDeleteEduction_success() {
        // mock 数据
        EducationDO dbEduction = randomPojo(EducationDO.class);
        eductionMapper.insert(dbEduction);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbEduction.getId();

        // 调用
        eductionService.deleteEduction(id);
       // 校验数据不存在了
       assertNull(eductionMapper.selectById(id));
    }

    @Test
    public void testDeleteEduction_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> eductionService.deleteEduction(id), EDUCTION_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetEductionPage() {
       // mock 数据
       EducationDO dbEduction = randomPojo(EducationDO.class, o -> { // 等会查询到
           o.setMedicineName(null);
           o.setPatientId(null);
           o.setDisease(null);
           o.setContent(null);
           o.setContentBasis(null);
           o.setCreateTime(null);
       });
       eductionMapper.insert(dbEduction);
       // 测试 medicineName 不匹配
       eductionMapper.insert(cloneIgnoreId(dbEduction, o -> o.setMedicineName(null)));
       // 测试 patientId 不匹配
       eductionMapper.insert(cloneIgnoreId(dbEduction, o -> o.setPatientId(null)));
       // 测试 disease 不匹配
       eductionMapper.insert(cloneIgnoreId(dbEduction, o -> o.setDisease(null)));
       // 测试 content 不匹配
       eductionMapper.insert(cloneIgnoreId(dbEduction, o -> o.setContent(null)));
       // 测试 contentBasis 不匹配
       eductionMapper.insert(cloneIgnoreId(dbEduction, o -> o.setContentBasis(null)));
       // 测试 createTime 不匹配
       eductionMapper.insert(cloneIgnoreId(dbEduction, o -> o.setCreateTime(null)));
       // 准备参数
       EducationPageReqVO reqVO = new EducationPageReqVO();
       reqVO.setMedicineName(null);
       reqVO.setPatientId(null);
       reqVO.setDisease(null);
       reqVO.setContent(null);
       reqVO.setContentBasis(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       PageResult<EducationDO> pageResult = eductionService.getEductionPage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbEduction, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetEductionList() {
       // mock 数据
       EducationDO dbEduction = randomPojo(EducationDO.class, o -> { // 等会查询到
           o.setMedicineName(null);
           o.setPatientId(null);
           o.setDisease(null);
           o.setContent(null);
           o.setContentBasis(null);
           o.setCreateTime(null);
       });
       eductionMapper.insert(dbEduction);
       // 测试 medicineName 不匹配
       eductionMapper.insert(cloneIgnoreId(dbEduction, o -> o.setMedicineName(null)));
       // 测试 patientId 不匹配
       eductionMapper.insert(cloneIgnoreId(dbEduction, o -> o.setPatientId(null)));
       // 测试 disease 不匹配
       eductionMapper.insert(cloneIgnoreId(dbEduction, o -> o.setDisease(null)));
       // 测试 content 不匹配
       eductionMapper.insert(cloneIgnoreId(dbEduction, o -> o.setContent(null)));
       // 测试 contentBasis 不匹配
       eductionMapper.insert(cloneIgnoreId(dbEduction, o -> o.setContentBasis(null)));
       // 测试 createTime 不匹配
       eductionMapper.insert(cloneIgnoreId(dbEduction, o -> o.setCreateTime(null)));
       // 准备参数
       EducationExportReqVO reqVO = new EducationExportReqVO();
       reqVO.setMedicineName(null);
       reqVO.setPatientId(null);
       reqVO.setDisease(null);
       reqVO.setContent(null);
       reqVO.setContentBasis(null);
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));

       // 调用
       List<EducationDO> list = eductionService.getEductionList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbEduction, list.get(0));
    }

}
