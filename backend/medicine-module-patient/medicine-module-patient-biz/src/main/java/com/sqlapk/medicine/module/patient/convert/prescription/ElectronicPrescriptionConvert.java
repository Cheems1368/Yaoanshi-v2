package com.sqlapk.medicine.module.patient.convert.prescription;

import com.sqlapk.medicine.module.patient.api.dto.prescription.ElectronicPrescriptionReqDTO;
import com.sqlapk.medicine.module.patient.api.dto.prescription.ElectronicPrescriptionRespDTO;
import com.sqlapk.medicine.module.patient.dal.dataobject.prescription.ElectronicPrescriptionDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 电子处方转换器?
 *
 * @author sqlapk
 */
@Mapper(componentModel = "spring")
public interface ElectronicPrescriptionConvert {

    ElectronicPrescriptionConvert INSTANCE = Mappers.getMapper(ElectronicPrescriptionConvert.class);

    /**
     * 请求DTO转DO
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "prescriptionCode", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "digitalSignature", ignore = true)
    @Mapping(target = "signatureTime", ignore = true)
    @Mapping(target = "signatureCertificate", ignore = true)
    @Mapping(target = "pdfUrl", ignore = true)
    @Mapping(target = "expiryTime", ignore = true)
    @Mapping(target = "reviewComments", ignore = true)
    @Mapping(target = "reviewTime", ignore = true)
    @Mapping(target = "dispensingStatus", ignore = true)
    @Mapping(target = "dispensingTime", ignore = true)
    @Mapping(target = "pickupTime", ignore = true)
    @Mapping(target = "medications", ignore = true)
    ElectronicPrescriptionDO convert(ElectronicPrescriptionReqDTO reqDTO);

    /**
     * DO转响应DTO
     */
    @Mapping(target = "patientName", ignore = true)
    @Mapping(target = "doctorName", ignore = true)
    @Mapping(target = "pharmacistName", ignore = true)
    @Mapping(target = "medications", ignore = true)
    ElectronicPrescriptionRespDTO convert(ElectronicPrescriptionDO prescriptionDO);
}
