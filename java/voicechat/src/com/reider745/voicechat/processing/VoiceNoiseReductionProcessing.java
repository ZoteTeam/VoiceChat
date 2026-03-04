package com.reider745.voicechat.processing;

import de.maxhenkel.rnnoise4j.Denoiser;

import java.io.IOException;

public class VoiceNoiseReductionProcessing implements VoiceProcessing {
    private final Denoiser denoiser;

    public VoiceNoiseReductionProcessing() {
        try {
            this.denoiser = new Denoiser();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public short[] process(String username, short[] voice) {
        return denoiser.denoise(voice);
    }
}
