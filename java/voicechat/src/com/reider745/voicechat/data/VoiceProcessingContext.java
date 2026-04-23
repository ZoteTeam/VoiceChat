package com.reider745.voicechat.data;

import com.reider745.voicechat.network.LocalPlayerList;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.NativeAPI;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VoiceProcessingContext {
    private final long playerUid;
    private final String username;

    private float gain = 1;
    private float leftGain = 1;
    private float rightGain = 1;

    private short[] voice;
    private int length;

    public VoiceProcessingContext(long playerUid, String username, short[] voice) {
        this.playerUid = playerUid;
        this.username = username;
        this.voice = voice;
        this.length = voice.length;
    }

    public VoiceProcessingContext(long playerUid, String username, short[] voice, int length) {
        this.playerUid = playerUid;
        this.username = username;
        this.voice = voice;
        this.length = length;
    }

    public void setVoice(short[] voice) {
        this.voice = voice;
        if (voice != null) this.length = voice.length;
        else this.length = 0;
    }

    public void setVoice(short[] voice, int length) {
        this.voice = voice;
        this.length = length;
    }

    public boolean isValid() {
        return voice != null && voice.length > 0;
    }
}
