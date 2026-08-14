# T5 Voice Sample Report

Date: 2026-05-16

## Scope

This report records the local sample-audio verification status for Phase T5 help-order voice messages and XFYUN transcription.

## Local Samples

Directory: `测试语音/`

| File | Size bytes | Last modified |
|---|---:|---|
| `14343152360712_hd.mp4` | 82491216 | 2026-05-16 13:19:15 |
| `16465304412616_hd.mp4` | 18909364 | 2026-05-16 13:21:24 |
| `16951873776516_hd.mp4` | 16238796 | 2026-05-16 13:20:54 |
| `16951881176900_hd.mp4` | 14752716 | 2026-05-16 13:20:46 |
| `17045873025476_hd.mp4` | 12812105 | 2026-05-16 13:20:28 |
| `17062315857864_hd.mp4` | 12440953 | 2026-05-16 13:19:43 |

## Extraction Status

`ffmpeg` / `ffprobe` were installed through `winget` package `Gyan.FFmpeg` version `8.1.1`.

Installed binaries:

- `C:\Users\Z5089\AppData\Local\Microsoft\WinGet\Packages\Gyan.FFmpeg_Microsoft.Winget.Source_8wekyb3d8bbwe\ffmpeg-8.1.1-full_build\bin\ffmpeg.exe`
- `C:\Users\Z5089\AppData\Local\Microsoft\WinGet\Packages\Gyan.FFmpeg_Microsoft.Winget.Source_8wekyb3d8bbwe\ffmpeg-8.1.1-full_build\bin\ffprobe.exe`

The current system session may need a new terminal to pick up PATH changes, but the full paths above were used for verification.

## Extraction Verification

Each sample was probed and converted to mono 16 kHz `pcm_s16le` WAV in a temporary directory: `%TEMP%\yaoanshi-t5-voice-samples`.

| File | Duration seconds | Source audio | Extracted WAV | Extracted bytes |
|---|---:|---|---|---:|
| `14343152360712_hd.mp4` | 260.20 | `aac,44100,2` | `pcm_s16le,16000,1` | 1920078 |
| `16465304412616_hd.mp4` | 65.25 | `aac,44100,2` | `pcm_s16le,16000,1` | 1920074 |
| `16951873776516_hd.mp4` | 56.38 | `aac,44100,2` | `pcm_s16le,16000,1` | 1804176 |
| `16951881176900_hd.mp4` | 51.94 | `aac,44100,2` | `pcm_s16le,16000,1` | 1662256 |
| `17045873025476_hd.mp4` | 45.14 | `aac,44100,2` | `pcm_s16le,16000,1` | 1444546 |
| `17062315857864_hd.mp4` | 44.21 | `aac,44100,2` | `pcm_s16le,16000,1` | 1414824 |

For samples longer than 60 seconds, extraction used the first 60 seconds to match the help-order voice-message limit.

## Implemented Acceptance Notes

- Patient app records microphone audio, uploads a playback file and hidden WAV recognition source, then sends the voice message.
- Patient and pharmacist can trigger transcription after the original voice message is sent.
- Doctor views can play original audio and view existing transcription results, but do not expose a transcription trigger.
- Missing XFYUN credentials do not block voice sending; transcription status becomes unavailable.
