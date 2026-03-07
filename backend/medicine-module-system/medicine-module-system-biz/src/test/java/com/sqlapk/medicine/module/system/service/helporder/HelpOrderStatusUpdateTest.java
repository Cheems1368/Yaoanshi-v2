package com.sqlapk.medicine.module.system.service.helporder;

import com.sqlapk.medicine.framework.common.exception.ServiceException;
import com.sqlapk.medicine.module.system.dal.dataobject.helporder.HelpOrderDO;
import com.sqlapk.medicine.module.system.dal.mysql.helporder.HelpOrderMapper;
import com.sqlapk.medicine.module.system.enums.HelpOrderStatus;
import com.sqlapk.medicine.module.system.service.helporder.HelpOrderService;
import com.sqlapk.medicine.module.system.service.helporder.HelpOrderServiceImpl;
import com.sqlapk.medicine.module.system.service.helporder.HelpOrderValidationUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * 工单状态更新功能单元测试
 *
 * @author sqlapk
 */
@ExtendWith(MockitoExtension.class)
class HelpOrderStatusUpdateTest {

    @Mock
    private HelpOrderMapper helpOrderMapper;

    @Mock
    private HelpOrderValidationUtils validationUtils;

    @InjectMocks
    private HelpOrderServiceImpl helpOrderService;

    private HelpOrderDO testHelpOrder;

    @BeforeEach
    void setUp() {
        testHelpOrder = HelpOrderDO.builder()
                .id(1001L)
                .patientId(1005L)
                .title("测试工单")
                .description("测试工单描述")
                .status(HelpOrderStatus.SUBMITTED)
                .build();
    }

    @Test
    void testUpdateHelpOrderStatus_Success() {
        // Given
        Long orderId = 1001L;
        HelpOrderStatus newStatus = HelpOrderStatus.ASSIGNED_PHARMACIST;
        String reason = "测试状态更新";

        when(validationUtils.validateHelpOrderExistsAndGet(orderId)).thenReturn(testHelpOrder);
        when(helpOrderMapper.updateById(any(HelpOrderDO.class))).thenReturn(1);

        // When
        var result = helpOrderService.updateHelpOrderStatus(orderId, newStatus, reason);

        // Then
        assertNotNull(result);
        assertEquals(newStatus, result.getStatus());
        verify(validationUtils).validateHelpOrderExistsAndGet(orderId);
        verify(helpOrderMapper).updateById(any(HelpOrderDO.class));
    }

    @Test
    void testUpdateHelpOrderStatus_WithStringStatus_Success() {
        // Given
        Long orderId = 1001L;
        String statusStr = "ASSIGNED_PHARMACIST";
        String reason = "测试字符串状态更新";

        when(validationUtils.validateHelpOrderExistsAndGet(orderId)).thenReturn(testHelpOrder);
        when(helpOrderMapper.updateById(any(HelpOrderDO.class))).thenReturn(1);

        // When
        var result = helpOrderService.updateHelpOrderStatus(orderId, statusStr, reason);

        // Then
        assertNotNull(result);
        assertEquals(HelpOrderStatus.ASSIGNED_PHARMACIST, result.getStatus());
        verify(validationUtils).validateHelpOrderExistsAndGet(orderId);
        verify(helpOrderMapper).updateById(any(HelpOrderDO.class));
    }

    @Test
    void testUpdateHelpOrderStatus_InvalidStatusString_ThrowsException() {
        // Given
        Long orderId = 1001L;
        String invalidStatusStr = "INVALID_STATUS";
        String reason = "测试无效状态";

        when(validationUtils.validateHelpOrderExistsAndGet(orderId)).thenReturn(testHelpOrder);

        // When & Then
        assertThrows(ServiceException.class, () -> {
            helpOrderService.updateHelpOrderStatus(orderId, invalidStatusStr, reason);
        });

        verify(validationUtils).validateHelpOrderExistsAndGet(orderId);
        verify(helpOrderMapper, never()).updateById(any(HelpOrderDO.class));
    }

    @Test
    void testUpdateHelpOrderStatus_OrderNotFound_ThrowsException() {
        // Given
        Long orderId = 9999L;
        HelpOrderStatus newStatus = HelpOrderStatus.ASSIGNED_PHARMACIST;
        String reason = "测试工单不存在";

        when(validationUtils.validateHelpOrderExistsAndGet(orderId))
                .thenThrow(ServiceException.class);

        // When & Then
        assertThrows(ServiceException.class, () -> {
            helpOrderService.updateHelpOrderStatus(orderId, newStatus, reason);
        });

        verify(validationUtils).validateHelpOrderExistsAndGet(orderId);
        verify(helpOrderMapper, never()).updateById(any(HelpOrderDO.class));
    }

    @Test
    void testRejectHelpOrder_Success() {
        // Given
        Long orderId = 1001L;
        String reason = "测试驳回工单";

        when(validationUtils.validateHelpOrderExistsAndGet(orderId)).thenReturn(testHelpOrder);
        when(helpOrderMapper.updateById(any(HelpOrderDO.class))).thenReturn(1);

        // When
        var result = helpOrderService.rejectHelpOrder(orderId, reason);

        // Then
        assertNotNull(result);
        assertEquals(HelpOrderStatus.REJECTED, result.getStatus());
        assertEquals(reason, result.getRejectReason());
        assertNotNull(result.getRejectedAt());
        verify(validationUtils).validateHelpOrderExistsAndGet(orderId);
        verify(helpOrderMapper).updateById(any(HelpOrderDO.class));
    }

    @Test
    void testRejectHelpOrder_AlreadyCompleted_ThrowsException() {
        // Given
        Long orderId = 1001L;
        String reason = "测试已完成工单驳回";
        testHelpOrder.setStatus(HelpOrderStatus.DONE);

        when(validationUtils.validateHelpOrderExistsAndGet(orderId)).thenReturn(testHelpOrder);

        // When & Then
        assertThrows(ServiceException.class, () -> {
            helpOrderService.rejectHelpOrder(orderId, reason);
        });

        verify(validationUtils).validateHelpOrderExistsAndGet(orderId);
        verify(helpOrderMapper, never()).updateById(any(HelpOrderDO.class));
    }

    @Test
    void testAssignToPharmacist_Success() {
        // Given
        Long orderId = 1001L;
        Long pharmacistId = 2001L;

        when(validationUtils.validateHelpOrderExistsAndGet(orderId)).thenReturn(testHelpOrder);
        when(helpOrderMapper.updateById(any(HelpOrderDO.class))).thenReturn(1);

        // When
        var result = helpOrderService.assignToPharmacist(orderId, pharmacistId);

        // Then
        assertNotNull(result);
        assertEquals(pharmacistId, result.getAssignedPharmacistId());
        assertEquals(HelpOrderStatus.ASSIGNED_PHARMACIST, result.getStatus());
        assertNotNull(result.getAssignedAt());
        verify(validationUtils).validateHelpOrderExistsAndGet(orderId);
        verify(helpOrderMapper).updateById(any(HelpOrderDO.class));
    }

    @Test
    void testEscalateToDoctor_Success() {
        // Given
        Long orderId = 1001L;
        Boolean needDoctor = true;
        Long doctorId = 3001L;
        testHelpOrder.setStatus(HelpOrderStatus.PHARMACIST_REVIEW);

        when(validationUtils.validateHelpOrderExistsAndGet(orderId)).thenReturn(testHelpOrder);
        when(helpOrderMapper.updateById(any(HelpOrderDO.class))).thenReturn(1);

        // When
        var result = helpOrderService.escalateToDoctor(orderId, needDoctor, doctorId);

        // Then
        assertNotNull(result);
        assertEquals(doctorId, result.getAssignedDoctorId());
        assertEquals(HelpOrderStatus.TO_DOCTOR, result.getStatus());
        assertNotNull(result.getEscalatedAt());
        verify(validationUtils).validateHelpOrderExistsAndGet(orderId);
        verify(helpOrderMapper).updateById(any(HelpOrderDO.class));
    }
}
