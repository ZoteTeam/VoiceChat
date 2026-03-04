package com.reider745.voicechat.processing;

import com.reider745.voicechat.Voice;
import com.reider745.voicechat.config.ClientConfig;
import com.reider745.voicechat.network.LocalPlayerList;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.NativeAPI;

public class VoiceJammingDistanceProcessing implements VoiceProcessing {
    private static final short[] EMPTY = new short[0];

    @Override
    public short[] process(String username, short[] voice) {
        long uid = 0;
        ClientConfig clientConfig = Voice.getClient().getClientConfig();

        for(long ent : LocalPlayerList.getPlayers()) {
            if(NativeAPI.getNameTag(ent).equals(username)) {
                uid = ent;
                break;
            }
        }

        if(uid == 0 || clientConfig == null) {
            return EMPTY;
        }

        float[] playerPos = new float[3];
        float[] speakPlayerPos = new float[3];

        NativeAPI.getPosition(uid, playerPos);
        NativeAPI.getPosition(NativeAPI.getLocalPlayer(), speakPlayerPos);

        double distance = Math.sqrt(Math.pow(playerPos[0] - speakPlayerPos[0], 2) + Math.pow(playerPos[1] - speakPlayerPos[1], 2) + Math.pow(playerPos[2] - speakPlayerPos[2], 2));
        double gain = (clientConfig.getDistance() - distance) / clientConfig.getDistance();

        for(short i = 0; i < voice.length; i++) {
            voice[i] = (short) Math.min(Math.max(voice[i] * gain, Short.MIN_VALUE), Short.MAX_VALUE);
        }
        return voice;
    }
}
