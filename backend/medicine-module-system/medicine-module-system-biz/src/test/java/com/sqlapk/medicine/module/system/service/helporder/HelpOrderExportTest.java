
package com.sqlapk.medicine.module.system.service.helporder;

import com.sqlapk.medicine.module.system.controller.admin.helporder.vo.HelpOrderExportReqVO;
import com.sqlapk.medicine.module.system.dal.dataobject.helporder.HelpOrderDO;
import com.sqlapk.medicine.module.system.dal.mysql.helporder.HelpOrderMapper;
import com.sqlapk.medicine.module.system.enums.HelpOrderStatus;
import com.sqlapk.medicine.module.system.service.helporder.HelpOrderService;
import com.sqlapk.medicine.module.system.service.helporder.HelpOrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 工单Excel导出功能单元测试
 *
 * @author sqlapk
 */
@ExtendWith(MockitoExtension.class)
class HelpOrderExportTest {

    @Mock
    private HelpOrderMapper helpOrderMapper;

    @InjectMocks
    private HelpOrderServiceImpl helpOrderService;

    private HelpOrderExportReqVO exportReqVO;
    private List<HelpOrderDO> mockHelpOrders;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        mockHelpOrders = Arrays.asList(
                HelpOrderDO.builder()
                        .id(1001L)
                        .patientId(1005L)
                        .title("测试工单1")
                        .description("测试工单描述1")
                        .status(HelpOrderStatus.SUBMITTED)
                        .build(),
                HelpOrderDO.builder()
                        .id(1002L)
                        .patientId(1006L)
                        .title("测试工单2")
                        .description("测试工单描述2")
                        .status(HelpOrderStatus.ASSIGNED_PHARMACIST)
                        .build()
        );

        exportReqVO = new HelpOrderExportReqVO();
    }

    @Test
    void testGetHelpOrderListForExport_WithAllFilters() {
        // Given
        exportReqVO.setStatus("SUBMITTED");
        exportReqVO.setPatientId(1005L);
        exportReqVO.setAssignedPharmacistId(2001L);
        exportReqVO.setAssignedDoctorId(3001L);
        exportReqVO.setTitle("测试");
        exportReqVO.setCreateTime(new LocalDateTime[]{
                LocalDateTime.now().minusDays(7),
                LocalDateTime.now()
        });

        when(helpOrderMapper.selectList(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class))).thenReturn(mockHelpOrders);

        // When
        List<HelpOrderDO> result = helpOrderService.getHelpOrderListForExport(exportReqVO);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1001L, result.get(0).getId());
        assertEquals(1002L, result.get(1).getId());
        verify(helpOrderMapper).selectList(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class));
    }

    @Test
    void testGetHelpOrderListForExport_WithStatusFilter() {
        // Given
        exportReqVO.setStatus("SUBMITTED");

        when(helpOrderMapper.selectList(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class))).thenReturn(
                Arrays.asList(mockHelpOrders.get(0))
        );

        // When
        List<HelpOrderDO> result = helpOrderService.getHelpOrderListForExport(exportReqVO);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(HelpOrderStatus.SUBMITTED, result.get(0).getStatus());
        verify(helpOrderMapper).selectList(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class));
    }

    @Test
    void testGetHelpOrderListForExport_WithPatientIdFilter() {
        // Given
        exportReqVO.setPatientId(1005L);

        when(helpOrderMapper.selectList(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class))).thenReturn(
                Arrays.asList(mockHelpOrders.get(0))
        );

        // When
        List<HelpOrderDO> result = helpOrderService.getHelpOrderListForExport(exportReqVO);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1005L, result.get(0).getPatientId());
        verify(helpOrderMapper).selectList(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class));
    }

    @Test
    void testGetHelpOrderListForExport_WithTitleFilter() {
        // Given
        exportReqVO.setTitle("测试工单");

        when(helpOrderMapper.selectList(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class))).thenReturn(mockHelpOrders);

        // When
        List<HelpOrderDO> result = helpOrderService.getHelpOrderListForExport(exportReqVO);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(order -> order.getTitle().contains("测试工单")));
        verify(helpOrderMapper).selectList(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class));
    }

    @Test
    void testGetHelpOrderListForExport_WithTimeRangeFilter() {
        // Given
        LocalDateTime startTime = LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = LocalDateTime.now();
        exportReqVO.setCreateTime(new LocalDateTime[]{startTime, endTime});

        when(helpOrderMapper.selectList(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class))).thenReturn(mockHelpOrders);

        // When
        List<HelpOrderDO> result = helpOrderService.getHelpOrderListForExport(exportReqVO);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(helpOrderMapper).selectList(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class));
    }

    @Test
    void testGetHelpOrderListForExport_WithPharmacistFilter() {
        // Given
        exportReqVO.setAssignedPharmacistId(2001L);

        when(helpOrderMapper.selectList(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class))).thenReturn(
                Arrays.asList(mockHelpOrders.get(1))
        );

        // When
        List<HelpOrderDO> result = helpOrderService.getHelpOrderListForExport(exportReqVO);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(2001L, result.get(0).getAssignedPharmacistId());
        verify(helpOrderMapper).selectList(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class));
    }

    @Test
    void testGetHelpOrderListForExport_WithDoctorFilter() {
        // Given
        exportReqVO.setAssignedDoctorId(3001L);

        when(helpOrderMapper.selectList(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class))).thenReturn(
                Arrays.asList(mockHelpOrders.get(1))
        );

        // When
        List<HelpOrderDO> result = helpOrderService.getHelpOrderListForExport(exportReqVO);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(3001L, result.get(0).getAssignedDoctorId());
        verify(helpOrderMapper).selectList(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class));
    }

    @Test
    void testGetHelpOrderListForExport_EmptyResult() {
        // Given
        exportReqVO.setStatus("NON_EXISTENT_STATUS");

        when(helpOrderMapper.selectList(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class))).thenReturn(Arrays.asList());

        // When
        List<HelpOrderDO> result = helpOrderService.getHelpOrderListForExport(exportReqVO);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(helpOrderMapper).selectList(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class));
    }

    @Test
    void testGetHelpOrderListForExport_NoFilters() {
        // Given - 不设置任何过滤条件
        when(helpOrderMapper.selectList(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class))).thenReturn(mockHelpOrders);

        // When
        List<HelpOrderDO> result = helpOrderService.getHelpOrderListForExport(exportReqVO);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(helpOrderMapper).selectList(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class));
    }

    @Test
    void testGetHelpOrderListForExport_OrderByDesc() {
        // Given
        when(helpOrderMapper.selectList(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class))).thenReturn(mockHelpOrders);

        // When
        List<HelpOrderDO> result = helpOrderService.getHelpOrderListForExport(exportReqVO);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        // 验证结果按ID降序排列（因为设置了orderByDesc）
        assertTrue(result.get(0).getId() >= result.get(1).getId());
        verify(helpOrderMapper).selectList(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class));
    }
}

