package com.reider745.voicechat.data;

import com.reider745.voicechat.network.LocalPlayerList;
import com.zhekasmirnov.innercore.api.NativeAPI;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VoiceProcessingContext {
    private final String username;

    private float gain = 1;
    private float leftGain = 1;
    private float rightGain = 1;

    private short[] voice;
    private int length;

    public VoiceProcessingContext(String username, short[] voice) {
        this.username = username;
        this.voice = voice;
        this.length = voice.length;
    }

    public VoiceProcessingContext(String username, short[] voice, int length) {
        this.username = username;
        this.voice = voice;
        this.length = length;
    }

    public long getPlayerUid() {
        if(this.username == null) return 0;

        for(long ent : LocalPlayerList.getPlayers()) {
            if(NativeAPI.getNameTag(ent).equals(this.username)) {
                return ent;
            }
        }

        return 0;
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
