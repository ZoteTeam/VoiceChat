interface SpeakService {
    play(buff: number[], length: number): void;
}

class SpeakAndroidApiServiceImpl implements SpeakService {
    private readonly track = new AudioTrack(AudioManager.STREAM_MUSIC, RATE, AudioFormat.CHANNEL_OUT_MONO, AUDIO_ENCODING, BUFFER_SIZE, AudioTrack.MODE_STREAM)

    constructor() {
        this.track.play();
    }

    public play(buff: number[], length: number): void {
        if(buff == null || buff.length <= 0 || length == null || length <= 0) throw new Error("Invalid play");
        this.track.write(buff, 0, length);
    }
}