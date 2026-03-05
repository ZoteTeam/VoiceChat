package com.reider745.voicechat.processing;

import com.reider745.voicechat.data.VoiceProcessingContext;
import com.zhekasmirnov.innercore.api.NativeAPI;

public class Voice3dProcessing implements VoiceProcessing {
    @Override
    public void process(VoiceProcessingContext context) {
        final long uid = context.getPlayerUid();

        if (uid == 0) {
            context.setLeftGain(0);
            context.setRightGain(0);
            return;
        }

        final float[] listenerPos = new float[3];
        final float[] speakerPos = new float[3];

        NativeAPI.getPosition(NativeAPI.getLocalPlayer(), listenerPos);
        NativeAPI.getPosition(uid, speakerPos);

        float dx = speakerPos[0] - listenerPos[0];
        float dy = speakerPos[1] - listenerPos[1];
        float dz = speakerPos[2] - listenerPos[2];

        float distance = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (distance < 0.01f) distance = 0.01f;

        float distanceAttenuation = Math.min(1.0f / distance, 1.0f);

        float horizontalDistance = (float) Math.sqrt(dx * dx + dz * dz);
        if (horizontalDistance < 0.001f) {
            context.setLeftGain(distanceAttenuation * 0.707f);
            context.setRightGain(distanceAttenuation * 0.707f);
            return;
        }

        float sinAzimuth = dx / horizontalDistance;

        float p = (sinAzimuth + 1.0f) * 0.5f;  // от 0 до 1
        float leftGain = (float) Math.cos(p * Math.PI / 2);
        float rightGain = (float) Math.sin(p * Math.PI / 2);

        leftGain *= distanceAttenuation;
        rightGain *= distanceAttenuation;

        context.setLeftGain(leftGain);
        context.setRightGain(rightGain);
    }
}
