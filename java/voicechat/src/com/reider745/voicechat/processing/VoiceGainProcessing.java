package com.reider745.voicechat.processing;

public class VoiceGainProcessing implements VoiceProcessing {
    private final float gain;

    public VoiceGainProcessing(float gain) {
        this.gain = gain;
    }

    @Override
    public short[] process(String username, short[] voice) {
        for(short i = 0; i < voice.length; i++) {
            voice[i] = (short) Math.min(Math.max(voice[i] * gain, Short.MIN_VALUE), Short.MAX_VALUE);
        }
        return voice;
    }
}
