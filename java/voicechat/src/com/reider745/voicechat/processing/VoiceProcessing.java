package com.reider745.voicechat.processing;

public interface VoiceProcessing {
    short[] process(String username, short[] voice);
}
