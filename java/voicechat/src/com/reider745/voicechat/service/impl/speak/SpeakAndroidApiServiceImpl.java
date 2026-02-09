package com.reider745.voicechat.service.impl.speak;

import android.media.AudioManager;
import android.media.AudioTrack;
import com.reider745.voicechat.data.Constants;
import com.reider745.voicechat.service.SpeakService;

public class SpeakAndroidApiServiceImpl implements SpeakService {
    private final AudioTrack track = new AudioTrack(AudioManager.STREAM_VOICE_CALL, Constants.RATE, Constants.CHANNEL_OUT, Constants.AUDIO_ENCODING, Constants.BUFFER_SIZE, AudioTrack.MODE_STREAM);

    public SpeakAndroidApiServiceImpl() {
        track.play();
    }

    @Override
    public void play(byte[] buff, int length) {
        if(buff == null || buff.length <= 0 || length <= 0) throw new Error("Invalid play");
        this.track.write(buff, 0, length);
    }
}
