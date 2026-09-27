package com.medicine.service.admin;

import com.medicine.domain.medication.AdherenceDoseRow;
import com.medicine.repository.medication.MedicationDoseTaskMapper;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;

@Service
public class AdherenceService {
    private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");
    private static final Duration WINDOW = Duration.ofMinutes(60);
    private final MedicationDoseTaskMapper taskMapper;

    public AdherenceService(MedicationDoseTaskMapper taskMapper) {
        this.taskMapper = taskMapper;
    }

    public Summary summarize(Long tenantId, Long patientId, Long planId, int days, LocalDateTime asOf) {
        if (days != 7 && days != 30) throw new IllegalArgumentException("days must be 7 or 30");
        LocalDate end = asOf.atOffset(ZoneOffset.UTC).atZoneSameInstant(SHANGHAI).toLocalDate();
        LocalDate start = end.minusDays(days - 1);
        return summarizeRange(tenantId, patientId, planId, start, end, asOf);
    }

    public Summary summarizeRange(Long tenantId, Long patientId, Long planId, LocalDate start, LocalDate end, LocalDateTime asOf) {
        List<AdherenceDoseRow> rows = taskMapper.selectAdherenceRows(tenantId, patientId, planId, start, end, asOf);
        long onTime = 0, early = 0, completed = 0, pendingWindow = 0;
        for (AdherenceDoseRow row : rows) {
            if ("COMPLETED".equals(row.getStatus())) completed++;
            if (row.getScheduledAt().plusMinutes(60).isAfter(asOf) && !"TAKEN".equals(row.getResult())) pendingWindow++;
            if (!"TAKEN".equals(row.getResult()) || row.getActualTakenAt() == null) continue;
            Duration difference = Duration.between(row.getScheduledAt(), row.getActualTakenAt());
            if (difference.abs().compareTo(WINDOW) <= 0) onTime++;
            else if (difference.compareTo(WINDOW.negated()) < 0) early++;
        }
        return new Summary(start, end, asOf, rows.size(), completed, onTime, early, pendingWindow);
    }

    public String toCsv(Long patientId, Long planId, Summary summary) {
        String rate = summary.dueTasks() == 0 ? "" : String.format(Locale.ROOT, "%.2f", summary.onTimeTakenTasks() * 100.0 / summary.dueTasks());
        return "\uFEFFpatientId,planId,from,to,asOf,dueTasks,onTimeTakenTasks,earlyOver60Minutes,pendingOnTimeWindow,自报按时服药率\n"
                + patientId + "," + (planId == null ? "" : planId) + "," + summary.from() + "," + summary.to() + ","
                + summary.asOf().toInstant(ZoneOffset.UTC) + "," + summary.dueTasks() + "," + summary.onTimeTakenTasks()
                + "," + summary.earlyOver60Minutes() + "," + summary.pendingOnTimeWindow() + "," + rate + "\n";
    }

    public record Summary(LocalDate from, LocalDate to, LocalDateTime asOf, long dueTasks,
                          long completedTasks, long onTimeTakenTasks, long earlyOver60Minutes,
                          long pendingOnTimeWindow) {
        public Double selfReportedOnTimeRate() {
            return dueTasks == 0 ? null : onTimeTakenTasks * 100.0 / dueTasks;
        }
    }
}
