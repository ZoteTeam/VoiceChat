package com.reider745.voicechat.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class VoiceEntry {
    private final String username;
    private final byte[] sound;
}
