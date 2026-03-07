package com.sqlapk.medicine.module.patient.api;

import com.sqlapk.medicine.module.patient.api.dto.PatientsRespDTO;
import com.sqlapk.medicine.module.patient.convert.patient.PatientsConvert;
import com.sqlapk.medicine.module.patient.dal.dataobject.patient.PatientsDO;
import com.sqlapk.medicine.module.patient.service.patient.PatientsService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 患者 API 实现类
 *
 * @author sqlapk
 */
@Service
public class PatientsApiImpl implements PatientsApi {

    @Resource
    private PatientsService patientsService;

    @Resource
    private PatientsConvert patientsConvert;

    @Override
    public PatientsRespDTO getPatients(Long id) {
        PatientsDO patients = patientsService.gets(id);
        return patientsConvert.convertToApi(patients);
    }

    @Override
    public List<PatientsRespDTO> getPatientsList(Collection<Long> ids) {
        List<PatientsDO> patientsList = patientsService.getsList(ids);
        return patientsList.stream()
                .map(patientsConvert::convertToApi)
                .collect(Collectors.toList());
    }

    @Override
    public PatientsRespDTO getPatientsByUserId(Long userId) {
        PatientsDO patients = patientsService.getByUserId(userId);
        return patientsConvert.convertToApi(patients);
    }

}
