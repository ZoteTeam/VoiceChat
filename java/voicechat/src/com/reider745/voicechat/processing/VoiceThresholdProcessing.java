package com.reider745.voicechat.processing;

import com.reider745.voicechat.data.VoiceProcessingContext;

public class VoiceThresholdProcessing implements VoiceProcessing {
    private final double threshold;

    public VoiceThresholdProcessing(double threshold) {
        this.threshold = threshold;
    }

    private double calculateDb(short[] buffer, int length) {
        long sum = 0;
        for(int i = 0; i < length; i++) {
            short sample = buffer[i];
            sum += sample * sample;
        }

        double rms = Math.sqrt((double) sum / buffer.length);
        if (rms <= 0) {
            return -Double.MAX_VALUE;
        }

        return 20.0 * Math.log10(rms / 32767.0);
    }

    @Override
    public void process(VoiceProcessingContext context) {
        if (context.isValid() && threshold < calculateDb(context.getVoice(), context.getLength()))
            context.setVoice(null);
    }
}
