package com.reider745.voicechat.processing;

import com.reider745.voicechat.Voice;
import com.reider745.voicechat.config.ClientConfig;
import com.reider745.voicechat.data.VoiceProcessingContext;
import com.zhekasmirnov.innercore.api.NativeAPI;

public class VoiceJammingDistanceProcessing implements VoiceProcessing {
    @Override
    public void process(VoiceProcessingContext context) {
        final long uid = context.getPlayerUid();
        final ClientConfig clientConfig = Voice.getClient().getClientConfig();

        if(uid == 0 || clientConfig == null) {
            return;
        }

        final float[] playerPos = new float[3];
        final float[] speakPlayerPos = new float[3];

        NativeAPI.getPosition(uid, playerPos);
        NativeAPI.getPosition(NativeAPI.getLocalPlayer(), speakPlayerPos);

        final double distance = Math.sqrt(Math.pow(playerPos[0] - speakPlayerPos[0], 2) + Math.pow(playerPos[1] - speakPlayerPos[1], 2) + Math.pow(playerPos[2] - speakPlayerPos[2], 2));
        final float gain = (float) (clientConfig.getDistance() - distance) / clientConfig.getDistance();

        context.setGain(context.getGain() * gain);
    }
}
