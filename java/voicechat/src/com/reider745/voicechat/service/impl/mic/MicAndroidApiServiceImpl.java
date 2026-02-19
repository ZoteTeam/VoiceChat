package com.reider745.voicechat.service.impl.mic;

import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import com.reider745.voicechat.data.Constants;
import com.reider745.voicechat.data.HandlerSound;
import com.reider745.voicechat.service.MicService;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.log.ICLog;
import de.maxhenkel.rnnoise4j.Denoiser;

import java.io.IOException;

public class MicAndroidApiServiceImpl implements MicService {
    private final Denoiser denoiser;
    private final AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, Constants.RATE, Constants.CHANNEL_IN, Constants.AUDIO_ENCODING, Constants.BUFFER_SIZE);
    private HandlerSound listener = (buff, length) -> {};
    private boolean record = false;
    private AcousticEchoCanceler echoCanceler;

    public MicAndroidApiServiceImpl() {
        try {
            this.denoiser = new Denoiser();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setListener(HandlerSound listener) {
        this.listener = listener;
    }

    @Override
    public void start() {
        if(this.record) throw new RuntimeException("Record enabled");

        final short[] buffer = new short[Constants.BUFFER_SIZE];
        Thread thread = new Thread(() -> {
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

            this.recorder.startRecording();
            this.record = true;

            while (this.record) {
                int bytesRead = this.recorder.read(buffer, 0, Constants.BUFFER_SIZE);
                if (bytesRead > 0) {
                    this.listener.apply(denoiser.denoise(buffer), bytesRead);
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
    public void stop() {
        if(!this.record) throw new RuntimeException("Record disabled");

        this.record = false;
    }

    @Override
    public boolean isRecording() {
        return this.record;
    }
}
