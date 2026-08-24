import { helpOrderService, type HelpOrderVoiceAttachmentPayload } from '@/modules/patient/api/mvp'
import type { VoiceRecording } from './useVoiceRecorder'

export async function uploadHelpOrderVoice(recording: VoiceRecording): Promise<HelpOrderVoiceAttachmentPayload> {
  const playback = await uploadOne(recording.playbackFile, 'HELP_ORDER_AUDIO')
  const transcriptSource = await uploadOne(recording.transcriptSourceFile, 'HELP_ORDER_AUDIO_TRANSCRIPT_SOURCE')
  return {
    playbackFileAssetId: playback.id,
    transcriptSourceFileAssetId: transcriptSource.id,
    durationSeconds: recording.durationSeconds
  }
}

async function uploadOne(file: File, businessType: 'HELP_ORDER_AUDIO' | 'HELP_ORDER_AUDIO_TRANSCRIPT_SOURCE') {
  const contentType = file.type || 'application/octet-stream'
  const presign = await helpOrderService.presignAudio({
    filename: file.name,
    contentType,
    sizeBytes: file.size,
    businessType
  })
  await helpOrderService.uploadToPresignedUrl(presign.data.uploadUrl, file, contentType)
  const registered = await helpOrderService.registerAudio({
    fileAssetId: presign.data.fileAssetId,
    objectKey: presign.data.objectKey,
    originalFilename: file.name || presign.data.filename,
    contentType,
    sizeBytes: file.size,
    businessType
  })
  return registered.data
}
