package com.medicine.service.storage;

import com.medicine.domain.file.FileAsset;
import com.medicine.repository.file.FileAssetMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Component
@ConditionalOnProperty(prefix = "medicine.storage.cleanup", name = "enabled", havingValue = "true", matchIfMissing = true)
public class OrphanFileCleanupJob {
    private final FileAssetMapper fileAssetMapper;
    private final S3CompatibleObjectStorage objectStorage;

    public OrphanFileCleanupJob(FileAssetMapper fileAssetMapper, S3CompatibleObjectStorage objectStorage) {
        this.fileAssetMapper = fileAssetMapper;
        this.objectStorage = objectStorage;
    }

    @Scheduled(fixedDelayString = "${medicine.storage.cleanup.fixed-delay-ms:300000}")
    public void cleanupOrphans() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime staleBefore = now.minusMinutes(10);
        for (FileAsset candidate : fileAssetMapper.selectCleanupCandidates(now.minusHours(1), now, staleBefore, 50)) {
            String token = UUID.randomUUID().toString();
            if (fileAssetMapper.claimCleanup(candidate.getTenantId(), candidate.getId(), token, now, staleBefore) != 1) continue;
            try {
                objectStorage.deleteObject(candidate.getObjectKey());
                fileAssetMapper.markCleanupSucceeded(candidate.getTenantId(), candidate.getId(), token, now);
            } catch (RuntimeException ex) {
                int attempts = candidate.getCleanupAttemptCount() == null ? 1 : candidate.getCleanupAttemptCount() + 1;
                long delayMinutes = Math.min(60, 1L << Math.min(attempts - 1, 6));
                String reason = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
                fileAssetMapper.markCleanupFailed(candidate.getTenantId(), candidate.getId(), token,
                        now.plusMinutes(delayMinutes), reason.substring(0, Math.min(reason.length(), 255)), now);
            }
        }
    }
}
