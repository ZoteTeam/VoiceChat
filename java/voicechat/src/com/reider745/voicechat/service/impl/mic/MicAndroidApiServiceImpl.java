package com.reider745.voicechat.service.impl.mic;

import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import com.reider745.voicechat.config.ClientConfig;
import com.reider745.voicechat.data.Constants;
import com.reider745.voicechat.data.HandlerSound;
import com.reider745.voicechat.service.MicService;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.log.ICLog;

public class MicAndroidApiServiceImpl implements MicService {
    private AudioRecord recorder;
    private HandlerSound listener = (buff, length) -> {};
    private boolean record = false;
    private AcousticEchoCanceler echoCanceler;

    private ClientConfig clientConfig;

    @Override
    public void setListener(HandlerSound listener) {
        this.listener = listener;
    }

    @Override
    public void start() {
        if(this.record || recorder == null || clientConfig == null) return;

        int bufferSize = clientConfig.getBuffer();

        final short[] buffer = new short[bufferSize];
        Thread thread = new Thread(() -> {
            this.record = true;

            this.recorder.startRecording();

            try {
                if (!AcousticEchoCanceler.isAvailable()) {
                    Logger.error("VoiceMod", "Acoustic Echo Canceler is not available on this device.");
                    return;
                }

                int audioSessionId = recorder.getAudioSessionId();
                if (audioSessionId == 0) {
                    Logger.error("VoiceMod", "Invalid audio session ID for AEC.");
                    return;
                }

                echoCanceler = AcousticEchoCanceler.create(audioSessionId);
                if (echoCanceler != null) {
                    echoCanceler.setEnabled(true);
                    Logger.debug("VoiceMod", "Acoustic Echo Canceler enabled for session: " + audioSessionId);
                } else {
                    Logger.error("VoiceMod", "Failed to create Acoustic Echo Canceler.");
                }
            } catch (Throwable t) {
                Logger.error("VoiceMod", ICLog.getStackTrace(t));
            }

            while (this.record) {
                int bytesRead = this.recorder.read(buffer, 0, bufferSize);
                if (bytesRead > 0) {
                    this.listener.apply(buffer, bytesRead);
                } else if (bytesRead == AudioRecord.ERROR_INVALID_OPERATION) {
                    Logger.error("VoiceMod", "ERROR_INVALID_OPERATION: Check recorder state");
                } else if (bytesRead == AudioRecord.ERROR_BAD_VALUE) {
                    Logger.error("VoiceMod", "ERROR_BAD_VALUE: Invalid parameters");
                    break;
                } else if (bytesRead == AudioRecord.ERROR_DEAD_OBJECT) {
                    Logger.error("VoiceMod", "ERROR_DEAD_OBJECT: AudioRecord is not valid");
                    break;
                }
            }

            if(echoCanceler != null) {
                echoCanceler.setEnabled(false);
                echoCanceler.release();
                echoCanceler = null;
            }

            this.recorder.stop();
        });
        thread.setName("mic");
        thread.start();
    }

    @Override
    public void refreshConfig(ClientConfig clientConfig) {
        boolean isRecording = this.isRecording();
        this.stop();

        this.clientConfig = clientConfig;
        this.recorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, clientConfig.getRate(), Constants.CHANNEL_IN, Constants.AUDIO_ENCODING, clientConfig.getBuffer());

        if(isRecording) {
            this.start();
        }
    }

    @Override
    public void stop() {
        if(!this.record) return;

        this.record = false;
    }

    @Override
    public boolean isRecording() {
        return this.record;
    }
}
