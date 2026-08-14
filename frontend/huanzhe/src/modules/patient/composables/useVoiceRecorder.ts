export interface VoiceRecording {
  playbackFile: File
  transcriptSourceFile: File
  durationSeconds: number
}

declare global {
  interface Window {
    webkitAudioContext?: typeof AudioContext
  }
}

const SAMPLE_RATE = 16000
const MAX_DURATION_MS = 60_000

type RecorderState = {
  stream: MediaStream
  audioContext: AudioContext
  source: MediaStreamAudioSourceNode
  processor: ScriptProcessorNode
  mediaRecorder?: MediaRecorder
  chunks: Blob[]
  samples: Float32Array[]
  startedAt: number
  timeoutId: number
}

let activeRecorder: RecorderState | null = null

export async function startVoiceRecording(onAutoStop?: () => void) {
  if (activeRecorder) {
    throw new Error('VOICE_RECORDING_ACTIVE')
  }
  if (!navigator.mediaDevices?.getUserMedia) {
    throw new Error('VOICE_RECORDER_UNSUPPORTED')
  }

  const stream = await navigator.mediaDevices.getUserMedia({
    audio: {
      channelCount: 1,
      echoCancellation: true,
      noiseSuppression: true,
      autoGainControl: true
    }
  })
  const AudioContextClass = window.AudioContext || window.webkitAudioContext
  const audioContext = new AudioContextClass()
  const source = audioContext.createMediaStreamSource(stream)
  const processor = audioContext.createScriptProcessor(4096, 1, 1)
  const chunks: Blob[] = []
  const samples: Float32Array[] = []

  processor.onaudioprocess = (event) => {
    samples.push(new Float32Array(event.inputBuffer.getChannelData(0)))
  }
  source.connect(processor)
  processor.connect(audioContext.destination)

  let mediaRecorder: MediaRecorder | undefined
  if (typeof MediaRecorder !== 'undefined') {
    const mimeType = MediaRecorder.isTypeSupported('audio/webm;codecs=opus') ? 'audio/webm;codecs=opus' : 'audio/webm'
    mediaRecorder = new MediaRecorder(stream, { mimeType })
    mediaRecorder.ondataavailable = (event) => {
      if (event.data.size > 0) {
        chunks.push(event.data)
      }
    }
    mediaRecorder.start()
  }

  const timeoutId = window.setTimeout(() => {
    onAutoStop?.()
  }, MAX_DURATION_MS)

  activeRecorder = {
    stream,
    audioContext,
    source,
    processor,
    mediaRecorder,
    chunks,
    samples,
    startedAt: Date.now(),
    timeoutId
  }
}

export async function stopVoiceRecording(): Promise<VoiceRecording> {
  const recorder = activeRecorder
  if (!recorder) {
    throw new Error('VOICE_RECORDING_NOT_ACTIVE')
  }
  activeRecorder = null

  window.clearTimeout(recorder.timeoutId)
  const durationSeconds = Math.max(1, Math.min(60, Math.round((Date.now() - recorder.startedAt) / 1000)))
  const recorderStop = new Promise<void>((resolve) => {
    if (!recorder.mediaRecorder || recorder.mediaRecorder.state === 'inactive') {
      resolve()
      return
    }
    recorder.mediaRecorder.onstop = () => resolve()
    recorder.mediaRecorder.stop()
  })

  recorder.processor.disconnect()
  recorder.source.disconnect()
  recorder.stream.getTracks().forEach((track) => track.stop())
  await recorderStop
  await recorder.audioContext.close()

  const wavBlob = encodeWav(downsample(concatSamples(recorder.samples), recorder.audioContext.sampleRate, SAMPLE_RATE), SAMPLE_RATE)
  const playbackBlob = recorder.chunks.length
    ? new Blob(recorder.chunks, { type: recorder.chunks[0].type || 'audio/webm' })
    : wavBlob
  const timestamp = Date.now()

  return {
    playbackFile: new File([playbackBlob], `help-order-voice-${timestamp}.${extensionFor(playbackBlob.type)}`, {
      type: playbackBlob.type || 'audio/webm'
    }),
    transcriptSourceFile: new File([wavBlob], `help-order-voice-source-${timestamp}.wav`, { type: 'audio/wav' }),
    durationSeconds
  }
}

export function cancelVoiceRecording() {
  const recorder = activeRecorder
  if (!recorder) return
  activeRecorder = null
  window.clearTimeout(recorder.timeoutId)
  if (recorder.mediaRecorder && recorder.mediaRecorder.state !== 'inactive') {
    recorder.mediaRecorder.stop()
  }
  recorder.processor.disconnect()
  recorder.source.disconnect()
  recorder.stream.getTracks().forEach((track) => track.stop())
  void recorder.audioContext.close()
}

function concatSamples(chunks: Float32Array[]) {
  const totalLength = chunks.reduce((sum, chunk) => sum + chunk.length, 0)
  const result = new Float32Array(totalLength)
  let offset = 0
  for (const chunk of chunks) {
    result.set(chunk, offset)
    offset += chunk.length
  }
  return result
}

function downsample(samples: Float32Array, inputRate: number, outputRate: number) {
  if (inputRate === outputRate) return samples
  const ratio = inputRate / outputRate
  const length = Math.round(samples.length / ratio)
  const result = new Float32Array(length)
  let inputOffset = 0
  for (let i = 0; i < length; i += 1) {
    const nextOffset = Math.round((i + 1) * ratio)
    let sum = 0
    let count = 0
    for (let j = inputOffset; j < nextOffset && j < samples.length; j += 1) {
      sum += samples[j]
      count += 1
    }
    result[i] = count ? sum / count : 0
    inputOffset = nextOffset
  }
  return result
}

function encodeWav(samples: Float32Array, sampleRate: number) {
  const buffer = new ArrayBuffer(44 + samples.length * 2)
  const view = new DataView(buffer)
  writeString(view, 0, 'RIFF')
  view.setUint32(4, 36 + samples.length * 2, true)
  writeString(view, 8, 'WAVE')
  writeString(view, 12, 'fmt ')
  view.setUint32(16, 16, true)
  view.setUint16(20, 1, true)
  view.setUint16(22, 1, true)
  view.setUint32(24, sampleRate, true)
  view.setUint32(28, sampleRate * 2, true)
  view.setUint16(32, 2, true)
  view.setUint16(34, 16, true)
  writeString(view, 36, 'data')
  view.setUint32(40, samples.length * 2, true)
  let offset = 44
  for (const sample of samples) {
    const clamped = Math.max(-1, Math.min(1, sample))
    view.setInt16(offset, clamped < 0 ? clamped * 0x8000 : clamped * 0x7fff, true)
    offset += 2
  }
  return new Blob([buffer], { type: 'audio/wav' })
}

function writeString(view: DataView, offset: number, text: string) {
  for (let i = 0; i < text.length; i += 1) {
    view.setUint8(offset + i, text.charCodeAt(i))
  }
}

function extensionFor(mimeType: string) {
  if (mimeType.includes('mp4')) return 'm4a'
  if (mimeType.includes('wav')) return 'wav'
  return 'webm'
}
