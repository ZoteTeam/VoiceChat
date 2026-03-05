package com.reider745.voicechat.processing;

import com.reider745.voicechat.data.VoiceProcessingContext;
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
    public void process(VoiceProcessingContext context) {
        if(context.isValid()) {
            context.setVoice(denoiser.denoise(context.getVoice()));
        }
    }
}
