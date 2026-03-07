package com.sqlapk.medicine.module.system.service.helporder;

import com.sqlapk.medicine.framework.common.pojo.PageResult;
import com.sqlapk.medicine.module.system.api.helporder.dto.HelpOrderCreateReqDTO;
import com.sqlapk.medicine.module.system.api.helporder.dto.HelpOrderPageReqDTO;
import com.sqlapk.medicine.module.system.api.helporder.dto.HelpOrderRespDTO;
import com.sqlapk.medicine.module.system.api.helporder.dto.HelpOrderUpdateReqDTO;
import com.sqlapk.medicine.module.system.controller.admin.helporder.vo.*;
import com.sqlapk.medicine.module.system.convert.helporder.HelpOrderConvert;
import com.sqlapk.medicine.module.system.dal.dataobject.helporder.HelpOrderDO;
import com.sqlapk.medicine.module.system.dal.mysql.helporder.HelpOrderMapper;
import com.sqlapk.medicine.module.system.enums.HelpOrderStatus;
import com.sqlapk.medicine.module.system.service.helporder.HelpOrderServiceImpl;
import com.sqlapk.medicine.framework.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 工单服务测试类
 */
@ExtendWith(MockitoExtension.class)
class HelpOrderServiceTest {

    @Mock
    private HelpOrderMapper helpOrderMapper;

    @InjectMocks
    private HelpOrderServiceImpl helpOrderService;

    private HelpOrderDO testHelpOrder;
    private HelpOrderCreateReqVO testCreateReqVO;
    private HelpOrderUpdateReqVO testUpdateReqVO;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        testHelpOrder = new HelpOrderDO();
        testHelpOrder.setId(1001L);
        testHelpOrder.setPatientId(1005L);
        testHelpOrder.setTitle("测试工单");
        testHelpOrder.setDescription("测试工单描述");
        testHelpOrder.setIntendedDrugs("[\"药品1\",\"药品2\"]");
        testHelpOrder.setAttachments("[\"附件1.jpg\"]");
        testHelpOrder.setToDoctor(false);
        testHelpOrder.setStatus(HelpOrderStatus.SUBMITTED);
        testHelpOrder.setCreateTime(LocalDateTime.now());
        testHelpOrder.setUpdateTime(LocalDateTime.now());

        testCreateReqVO = new HelpOrderCreateReqVO();
        testCreateReqVO.setPatientId(1005L);
        testCreateReqVO.setTitle("测试工单");
        testCreateReqVO.setDescription("测试工单描述");
        testCreateReqVO.setIntendedDrugs(Arrays.asList("药品1", "药品2"));
        testCreateReqVO.setAttachments(Arrays.asList("附件1.jpg"));
        testCreateReqVO.setToDoctor(false);

        testUpdateReqVO = new HelpOrderUpdateReqVO();
        testUpdateReqVO.setId(1001L);
        testUpdateReqVO.setTitle("更新后的工单标题");
        testUpdateReqVO.setDescription("更新后的工单描述");
        testUpdateReqVO.setIntendedDrugs(Arrays.asList("更新药品1", "更新药品2"));
        testUpdateReqVO.setAttachments(Arrays.asList("更新附件1.jpg"));
        testUpdateReqVO.setToDoctor(true);
    }

    @Test
    void testCreateHelpOrder_Success() {
        // 准备测试数据
        when(helpOrderMapper.insert(any(HelpOrderDO.class))).thenReturn(1);

        // 执行测试
        HelpOrderCreateReqDTO createReqDTO = HelpOrderConvert.INSTANCE.convert(testCreateReqVO);
        Long result = helpOrderService.createHelpOrder(createReqDTO);

        // 验证结果
        assertNotNull(result);
        verify(helpOrderMapper, times(1)).insert(any(HelpOrderDO.class));
    }

    @Test
    void testCreateHelpOrder_ValidationError() {
        // 准备测试数据 - 缺少必填字段
        testCreateReqVO.setTitle(null);

        // 执行测试并验证异常
        assertThrows(ServiceException.class, () -> {
            HelpOrderCreateReqDTO createReqDTO = HelpOrderConvert.INSTANCE.convert(testCreateReqVO);
            helpOrderService.createHelpOrder(createReqDTO);
        });
    }

    @Test
    void testGetHelpOrder_Success() {
        // 准备测试数据
        when(helpOrderMapper.selectById(1001L)).thenReturn(testHelpOrder);

        // 执行测试
        HelpOrderRespDTO result = helpOrderService.getHelpOrder(1001L);

        // 验证结果
        assertNotNull(result);
        assertEquals(1001L, result.getId());
        assertEquals("测试工单", result.getTitle());
        assertEquals("测试工单描述", result.getDescription());
        assertEquals("SUBMITTED", result.getStatus());
    }

    @Test
    void testGetHelpOrder_NotFound() {
        // 准备测试数据
        when(helpOrderMapper.selectById(9999L)).thenReturn(null);

        // 执行测试并验证异常
        assertThrows(ServiceException.class, () -> {
            helpOrderService.getHelpOrder(9999L);
        });
    }

    @Test
    void testUpdateHelpOrder_Success() {
        // 准备测试数据
        when(helpOrderMapper.selectById(1001L)).thenReturn(testHelpOrder);
        when(helpOrderMapper.updateById(any(HelpOrderDO.class))).thenReturn(1);

        // 执行测试
        HelpOrderUpdateReqDTO updateReqDTO = HelpOrderConvert.INSTANCE.convert(testUpdateReqVO);
        helpOrderService.updateHelpOrder(updateReqDTO);

        // 验证结果
        verify(helpOrderMapper, times(1)).updateById(any(HelpOrderDO.class));
    }

    @Test
    void testUpdateHelpOrder_NotFound() {
        // 准备测试数据
        when(helpOrderMapper.selectById(9999L)).thenReturn(null);

        // 执行测试并验证异常
        assertThrows(ServiceException.class, () -> {
            HelpOrderUpdateReqDTO updateReqDTO = HelpOrderConvert.INSTANCE.convert(testUpdateReqVO);
            helpOrderService.updateHelpOrder(updateReqDTO);
        });
    }

    @Test
    void testGetHelpOrderPage_Success() {
        // 准备测试数据
        HelpOrderPageReqVO pageReqVO = new HelpOrderPageReqVO();
        pageReqVO.setPageNo(1);
        pageReqVO.setPageSize(10);
        pageReqVO.setStatus("SUBMITTED");

        PageResult<HelpOrderDO> mockPageResult = new PageResult<>();
        mockPageResult.setList(Collections.singletonList(testHelpOrder));
        mockPageResult.setTotal(1L);
        
        when(helpOrderMapper.selectPage(any(com.sqlapk.medicine.framework.common.pojo.PageParam.class), any(com.baomidou.mybatisplus.core.conditions.Wrapper.class))).thenReturn(mockPageResult);

        // 执行测试
        HelpOrderPageReqDTO pageReqDTO = HelpOrderConvert.INSTANCE.convert(pageReqVO);
        PageResult<HelpOrderRespDTO> result = helpOrderService.getHelpOrderPage(pageReqDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getList().size());
        assertEquals(1L, result.getTotal());
    }

    @Test
    void testGetHelpOrderPage_ValidationError() {
        // 准备测试数据 - 缺少必填参数
        HelpOrderPageReqVO pageReqVO = new HelpOrderPageReqVO();
        pageReqVO.setPageNo(0); // 无效页码
        pageReqVO.setPageSize(10);

        // 执行测试并验证异常
        assertThrows(ServiceException.class, () -> {
            HelpOrderPageReqDTO pageReqDTO = HelpOrderConvert.INSTANCE.convert(pageReqVO);
            helpOrderService.getHelpOrderPage(pageReqDTO);
        });
    }

    @Test
    void testGetHelpOrderList_Success() {
        // 准备测试数据
        List<Long> ids = Arrays.asList(1001L, 1002L);
        when(helpOrderMapper.selectBatchIds(ids)).thenReturn(Arrays.asList(testHelpOrder));

        // 执行测试
        List<HelpOrderRespDTO> result = helpOrderService.getHelpOrderList(ids);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1001L, result.get(0).getId());
    }

    @Test
    void testAssignPharmacist_Success() {
        // 准备测试数据
        HelpOrderAssignReqVO assignReqVO = new HelpOrderAssignReqVO();
        assignReqVO.setId(1001L);
        assignReqVO.setPharmacistId(1001L);

        when(helpOrderMapper.selectById(1001L)).thenReturn(testHelpOrder);
        when(helpOrderMapper.updateById(any(HelpOrderDO.class))).thenReturn(1);

        // 执行测试
        HelpOrderRespDTO result = helpOrderService.assignToPharmacist(assignReqVO.getId(), assignReqVO.getPharmacistId());

        // 验证结果
        assertNotNull(result);
        assertEquals(1001L, result.getId());
        assertEquals("ASSIGNED_PHARMACIST", result.getStatus());
        assertEquals(1001L, result.getAssignedPharmacistId());
    }

    @Test
    void testAssignPharmacist_AlreadyAssigned() {
        // 准备测试数据
        testHelpOrder.setStatus(HelpOrderStatus.ASSIGNED_PHARMACIST);
        testHelpOrder.setAssignedPharmacistId(1001L);

        HelpOrderAssignReqVO assignReqVO = new HelpOrderAssignReqVO();
        assignReqVO.setId(1001L);
        assignReqVO.setPharmacistId(1002L);

        when(helpOrderMapper.selectById(1001L)).thenReturn(testHelpOrder);

        // 执行测试并验证异常
        assertThrows(ServiceException.class, () -> {
            helpOrderService.assignToPharmacist(assignReqVO.getId(), assignReqVO.getPharmacistId());
        });
    }

    @Test
    void testEscalateToDoctor_Success() {
        // 准备测试数据
        testHelpOrder.setStatus(HelpOrderStatus.PHARMACIST_REVIEW);

        HelpOrderEscalateReqVO escalateReqVO = new HelpOrderEscalateReqVO();
        escalateReqVO.setId(1001L);
        escalateReqVO.setNeedDoctor(true);
        escalateReqVO.setDoctorId(1003L);

        when(helpOrderMapper.selectById(1001L)).thenReturn(testHelpOrder);
        when(helpOrderMapper.updateById(any(HelpOrderDO.class))).thenReturn(1);

        // 执行测试
        HelpOrderRespDTO result = helpOrderService.escalateToDoctor(escalateReqVO.getId(), escalateReqVO.getNeedDoctor(), escalateReqVO.getDoctorId());

        // 验证结果
        assertNotNull(result);
        assertEquals(1001L, result.getId());
        assertEquals("TO_DOCTOR", result.getStatus());
        assertEquals(1003L, result.getAssignedDoctorId());
    }

    @Test
    void testEscalateToDoctor_InvalidStatus() {
        // 准备测试数据
        testHelpOrder.setStatus(HelpOrderStatus.DONE);

        HelpOrderEscalateReqVO escalateReqVO = new HelpOrderEscalateReqVO();
        escalateReqVO.setId(1001L);
        escalateReqVO.setNeedDoctor(true);
        escalateReqVO.setDoctorId(1003L);

        when(helpOrderMapper.selectById(1001L)).thenReturn(testHelpOrder);

        // 执行测试并验证异常
        assertThrows(ServiceException.class, () -> {
            helpOrderService.escalateToDoctor(escalateReqVO.getId(), escalateReqVO.getNeedDoctor(), escalateReqVO.getDoctorId());
        });
    }

    @Test
    void testRejectHelpOrder_Success() {
        // 准备测试数据
        HelpOrderRejectReqVO rejectReqVO = new HelpOrderRejectReqVO();
        rejectReqVO.setId(1001L);
        rejectReqVO.setReason("测试驳回原因");

        when(helpOrderMapper.selectById(1001L)).thenReturn(testHelpOrder);
        when(helpOrderMapper.updateById(any(HelpOrderDO.class))).thenReturn(1);

        // 执行测试
        HelpOrderRespDTO result = helpOrderService.rejectHelpOrder(rejectReqVO.getId(), rejectReqVO.getReason());

        // 验证结果
        assertNotNull(result);
        assertEquals(1001L, result.getId());
        assertEquals("REJECTED", result.getStatus());
        assertEquals("测试驳回原因", result.getRejectReason());
    }

    @Test
    void testRejectHelpOrder_InvalidStatus() {
        // 准备测试数据
        testHelpOrder.setStatus(HelpOrderStatus.DONE);

        HelpOrderRejectReqVO rejectReqVO = new HelpOrderRejectReqVO();
        rejectReqVO.setId(1001L);
        rejectReqVO.setReason("测试驳回原因");

        when(helpOrderMapper.selectById(1001L)).thenReturn(testHelpOrder);

        // 执行测试并验证异常
        assertThrows(ServiceException.class, () -> {
            helpOrderService.rejectHelpOrder(rejectReqVO.getId(), rejectReqVO.getReason());
        });
    }

    @Test
    void testUpdateHelpOrderStatus_Success() {
        // 准备测试数据
        when(helpOrderMapper.selectById(1001L)).thenReturn(testHelpOrder);
        when(helpOrderMapper.updateById(any(HelpOrderDO.class))).thenReturn(1);

        // 执行测试
        HelpOrderRespDTO result = helpOrderService.updateHelpOrderStatus(1001L, "ASSIGNED_PHARMACIST", "测试状态更新原因");

        // 验证结果
        assertNotNull(result);
        assertEquals(1001L, result.getId());
        assertEquals("ASSIGNED_PHARMACIST", result.getStatus());
    }

    @Test
    void testUpdateHelpOrderStatus_InvalidTransition() {
        // 准备测试数据
        testHelpOrder.setStatus(HelpOrderStatus.DONE);

        when(helpOrderMapper.selectById(1001L)).thenReturn(testHelpOrder);

        // 执行测试并验证异常
        assertThrows(ServiceException.class, () -> {
            helpOrderService.updateHelpOrderStatus(1001L, "ASSIGNED_PHARMACIST", "测试状态更新原因");
        });
    }

    @Test
    void testUpdateHelpOrderStatus_InvalidStatus() {
        // 准备测试数据
        when(helpOrderMapper.selectById(1001L)).thenReturn(testHelpOrder);

        // 执行测试并验证异常
        assertThrows(ServiceException.class, () -> {
            helpOrderService.updateHelpOrderStatus(1001L, "INVALID_STATUS", "测试状态更新原因");
        });
    }

    @Test
    void testGetHelpOrderListForExport_Success() {
        // 准备测试数据
        HelpOrderExportReqVO exportReqVO = new HelpOrderExportReqVO();
        exportReqVO.setStatus("SUBMITTED");

        when(helpOrderMapper.selectList(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class))).thenReturn(Arrays.asList(testHelpOrder));

        // 执行测试
        List<HelpOrderDO> result = helpOrderService.getHelpOrderListForExport(exportReqVO);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1001L, result.get(0).getId());
    }

    @Test
    void testDeleteHelpOrder_Success() {
        // 准备测试数据
        when(helpOrderMapper.selectById(1001L)).thenReturn(testHelpOrder);
        when(helpOrderMapper.deleteById(1001L)).thenReturn(1);

        // 执行测试
        helpOrderService.deleteHelpOrder(1001L);

        // 验证结果
        verify(helpOrderMapper, times(1)).deleteById(1001L);
    }

    @Test
    void testDeleteHelpOrder_NotFound() {
        // 准备测试数据
        when(helpOrderMapper.selectById(9999L)).thenReturn(null);

        // 执行测试并验证异常
        assertThrows(ServiceException.class, () -> {
            helpOrderService.deleteHelpOrder(9999L);
        });
    }

}
