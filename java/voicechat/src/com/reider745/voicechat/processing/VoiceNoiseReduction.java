package com.reider745.voicechat.processing;

import de.maxhenkel.rnnoise4j.Denoiser;

import java.io.IOException;

public class VoiceNoiseReduction implements VoiceProcessing {
    private final Denoiser denoiser;

    public VoiceNoiseReduction() {
        try {
            this.denoiser = new Denoiser();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public short[] process(short[] voice) {
        return denoiser.denoise(voice);
    }
}
