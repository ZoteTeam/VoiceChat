package com.reider745.voicechat.service.impl.mic;

import android.media.AudioRecord;
import android.media.MediaRecorder;
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

        Thread thread = new Thread(() -> {
            final short[] buffer = new short[Constants.BUFFER_SIZE];

            try {
                this.recorder.startRecording();
            } catch (Throwable t) {
                Logger.error("VoiceMod", ICLog.getStackTrace(t));
            }
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

                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
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
