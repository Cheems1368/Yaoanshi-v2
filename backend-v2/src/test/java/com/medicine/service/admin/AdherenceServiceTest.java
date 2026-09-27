package com.medicine.service.admin;

import com.medicine.domain.medication.AdherenceDoseRow;
import com.medicine.repository.medication.MedicationDoseTaskMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdherenceServiceTest {
    private final MedicationDoseTaskMapper mapper = mock(MedicationDoseTaskMapper.class);
    private final AdherenceService service = new AdherenceService(mapper);

    @Test
    void countsInclusiveSixtyMinuteBoundariesAndKeepsMissingActualInDenominator() {
        LocalDateTime asOf = LocalDateTime.of(2026, 9, 26, 12, 0);
        LocalDateTime scheduled = LocalDateTime.of(2026, 9, 25, 8, 0);
        when(mapper.selectAdherenceRows(1L, 2L, null, LocalDate.parse("2026-09-20"),
                LocalDate.parse("2026-09-26"), asOf)).thenReturn(List.of(
                row(scheduled, scheduled.minusMinutes(60)),
                row(scheduled, scheduled.plusMinutes(60)),
                row(scheduled, scheduled.minusMinutes(61)),
                row(scheduled, null)));

        AdherenceService.Summary summary = service.summarize(1L, 2L, null, 7, asOf);

        assertThat(summary.dueTasks()).isEqualTo(4);
        assertThat(summary.onTimeTakenTasks()).isEqualTo(2);
        assertThat(summary.earlyOver60Minutes()).isEqualTo(1);
        assertThat(summary.selfReportedOnTimeRate()).isEqualTo(50.0);
        assertThat(service.toCsv(2L, null, summary))
                .startsWith("\uFEFFpatientId,")
                .contains("pendingOnTimeWindow,自报按时服药率\n");
    }

    @Test
    void zeroDenominatorHasNoRate() {
        LocalDateTime asOf = LocalDateTime.of(2026, 9, 26, 12, 0);
        AdherenceService.Summary summary = service.summarize(1L, 2L, null, 30, asOf);
        assertThat(summary.dueTasks()).isZero();
        assertThat(summary.selfReportedOnTimeRate()).isNull();
    }

    private AdherenceDoseRow row(LocalDateTime scheduled, LocalDateTime actual) {
        AdherenceDoseRow row = new AdherenceDoseRow();
        row.setScheduledAt(scheduled);
        row.setStatus("COMPLETED");
        row.setResult("TAKEN");
        row.setActualTakenAt(actual);
        return row;
    }
}
