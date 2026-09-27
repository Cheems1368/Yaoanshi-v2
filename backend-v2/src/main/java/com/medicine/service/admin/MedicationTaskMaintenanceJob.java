package com.medicine.service.admin;

import com.medicine.domain.medication.MedicationPlan;
import com.medicine.repository.medication.MedicationPlanMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MedicationTaskMaintenanceJob {
    private final MedicationPlanMapper medicationPlanMapper;
    private final AdminMedicationPlanService planService;

    public MedicationTaskMaintenanceJob(MedicationPlanMapper medicationPlanMapper, AdminMedicationPlanService planService) {
        this.medicationPlanMapper = medicationPlanMapper;
        this.planService = planService;
    }

    @Scheduled(fixedDelayString = "${medicine.medication.task-maintenance-delay-ms:3600000}")
    public void maintain() {
        for (MedicationPlan patient : medicationPlanMapper.selectActivePatientKeys()) {
            planService.maintainPatientTasks(patient.getTenantId(), patient.getPatientId());
        }
    }
}
