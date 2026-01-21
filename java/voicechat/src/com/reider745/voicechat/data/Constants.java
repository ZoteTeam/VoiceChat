package com.reider745.voicechat.data;

import android.media.AudioFormat;
import android.media.AudioRecord;

public class Constants {
    public static final int RATE = 44100;
    public static final int CHANNEL_IN = AudioFormat.CHANNEL_IN_MONO;
    public static final int CHANNEL_OUT = AudioFormat.CHANNEL_OUT_MONO;
    public static final int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private static final double FRAME = 480;
    private static final double MIN_BUFFER = AudioRecord.getMinBufferSize(RATE, CHANNEL_IN, AUDIO_ENCODING);

    public static final int BUFFER_SIZE = (int) (Math.ceil(MIN_BUFFER / FRAME) * FRAME);

    static {
        if(BUFFER_SIZE < MIN_BUFFER) {
            throw new RuntimeException("Buffer size too small");
        }
    }
}
