package com.reider745.voicechat.service.impl.speak;

import android.media.AudioManager;
import android.media.AudioTrack;
import com.reider745.voicechat.config.ClientConfig;
import com.reider745.voicechat.data.Constants;
import com.reider745.voicechat.service.SpeakService;

public class SpeakAndroidApiServiceImpl implements SpeakService {
    private AudioTrack track;

    @Override
    public void refreshConfig(ClientConfig clientConfig) {
        if(this.track != null) {
            this.track.stop();
            this.track.release();
        }

        this.track = new AudioTrack(AudioManager.STREAM_MUSIC, clientConfig.getRate(), Constants.CHANNEL_OUT, Constants.AUDIO_ENCODING, clientConfig.getBuffer(), AudioTrack.MODE_STREAM);
        track.play();
    }

    @Override
    public void play(short[] buff, int length) {
        if(this.track == null || buff == null || buff.length == 0 || length <= 0) return;
        this.track.write(buff, 0, length);
    }
}
