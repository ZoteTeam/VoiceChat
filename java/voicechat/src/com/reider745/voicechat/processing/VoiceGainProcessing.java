package com.reider745.voicechat.processing;

import com.reider745.voicechat.data.VoiceProcessingContext;

public class VoiceGainProcessing implements VoiceProcessing {
    private final float gain;

    public VoiceGainProcessing(float gain) {
        this.gain = gain;
    }

    @Override
    public void process(VoiceProcessingContext context) {
        context.setGain(context.getGain() * gain);
    }
}
