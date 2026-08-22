package com.medicine.service.storage;

import com.medicine.domain.file.FileAsset;
import com.medicine.repository.file.FileAssetMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrphanFileCleanupJobTest {
    @Mock private FileAssetMapper fileAssetMapper;
    @Mock private S3CompatibleObjectStorage objectStorage;

    @Test
    void shouldDeleteAndMarkClaimedOrphan() {
        FileAsset asset = asset();
        when(fileAssetMapper.selectCleanupCandidates(any(), any(), any(), eq(50))).thenReturn(List.of(asset));
        when(fileAssetMapper.claimCleanup(eq(1L), eq(42L), anyString(), any(), any())).thenReturn(1);
        OrphanFileCleanupJob job = new OrphanFileCleanupJob(fileAssetMapper, objectStorage);

        job.cleanupOrphans();

        verify(objectStorage).deleteObject("help-orders/images/orphan.png");
        verify(fileAssetMapper).markCleanupSucceeded(eq(1L), eq(42L), anyString(), any(LocalDateTime.class));
    }

    @Test
    void shouldRetainFailedDeleteForRetry() {
        FileAsset asset = asset();
        when(fileAssetMapper.selectCleanupCandidates(any(), any(), any(), eq(50))).thenReturn(List.of(asset));
        when(fileAssetMapper.claimCleanup(eq(1L), eq(42L), anyString(), any(), any())).thenReturn(1);
        doThrow(new IllegalStateException("storage unavailable")).when(objectStorage).deleteObject(asset.getObjectKey());
        OrphanFileCleanupJob job = new OrphanFileCleanupJob(fileAssetMapper, objectStorage);

        job.cleanupOrphans();

        verify(fileAssetMapper).markCleanupFailed(eq(1L), eq(42L), anyString(), any(LocalDateTime.class),
                eq("storage unavailable"), any(LocalDateTime.class));
    }

    private FileAsset asset() {
        FileAsset asset = new FileAsset();
        asset.setId(42L); asset.setTenantId(1L); asset.setObjectKey("help-orders/images/orphan.png");
        asset.setCleanupAttemptCount(0);
        return asset;
    }
}
