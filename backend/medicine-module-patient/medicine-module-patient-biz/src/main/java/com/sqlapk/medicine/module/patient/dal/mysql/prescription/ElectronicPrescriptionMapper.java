package com.sqlapk.medicine.module.patient.dal.mysql.prescription;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sqlapk.medicine.module.patient.dal.dataobject.prescription.ElectronicPrescriptionDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 电子处方 Mapper
 *
 * @author sqlapk
 */
@Mapper
public interface ElectronicPrescriptionMapper extends BaseMapper<ElectronicPrescriptionDO> {

    /**
     * 根据处方编号查询处方
     */
    @Select("SELECT * FROM electronic_prescriptions WHERE prescription_code = #{prescriptionCode} " +
            "AND deleted = 0")
    ElectronicPrescriptionDO selectByPrescriptionCode(@Param("prescriptionCode") String prescriptionCode);

    /**
     * 根据工单ID查询处方
     */
    @Select("SELECT * FROM electronic_prescriptions WHERE order_id = #{orderId} " +
            "AND deleted = 0 ORDER BY create_time DESC")
    List<ElectronicPrescriptionDO> selectByOrderId(@Param("orderId") Long orderId);

    /**
     * 根据患者ID查询处方列表
     */
    @Select("SELECT * FROM electronic_prescriptions WHERE patient_id = #{patientId} " +
            "AND deleted = 0 ORDER BY create_time DESC LIMIT #{limit}")
    List<ElectronicPrescriptionDO> selectByPatientId(@Param("patientId") Long patientId, @Param("limit") Integer limit);

    /**
     * 根据医生ID查询待审核处方
     */
    @Select("SELECT * FROM electronic_prescriptions WHERE doctor_id = #{doctorId} " +
            "AND status = 'confirmed' AND deleted = 0 ORDER BY create_time ASC")
    List<ElectronicPrescriptionDO> selectPendingByDoctorId(@Param("doctorId") Long doctorId);

    /**
     * 更新处方状态
     */
    @Update("UPDATE electronic_prescriptions SET status = #{status}, update_time = NOW() " +
            "WHERE id = #{id} AND deleted = 0")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    /**
     * 更新电子签名
     */
    @Update("UPDATE electronic_prescriptions SET digital_signature = #{signature}, " +
            "signature_time = #{signatureTime}, signature_certificate = #{certificate}, " +
            "status = 'signed', update_time = NOW() " +
            "WHERE id = #{id} AND deleted = 0")
    int updateDigitalSignature(@Param("id") Long id, @Param("signature") String signature,
                               @Param("signatureTime") LocalDateTime signatureTime,
                               @Param("certificate") String certificate);

    /**
     * 更新PDF文件URL
     */
    @Update("UPDATE electronic_prescriptions SET pdf_url = #{pdfUrl}, update_time = NOW() " +
            "WHERE id = #{id} AND deleted = 0")
    int updatePdfUrl(@Param("id") Long id, @Param("pdfUrl") String pdfUrl);

    /**
     * 查询即将过期的处方
     */
    @Select("SELECT * FROM electronic_prescriptions WHERE expiry_time BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL 7 DAY) " +
            "AND status IN ('signed', 'dispensed') AND deleted = 0")
    List<ElectronicPrescriptionDO> selectExpiringPrescriptions();
}
