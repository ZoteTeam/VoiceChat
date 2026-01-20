const AudioRecord = android.media.AudioRecord;
const MediaRecorder = android.media.MediaRecorder;
const AudioFormat = android.media.AudioFormat;
const AudioTrack = android.media.AudioTrack;
const AudioManager = android.media.AudioManager;
const Thread = java.lang.Thread;
const ByteBuffer = java.nio.ByteBuffer;

type HandlerSound = NonNullable<(buff: number[], length: number) => void>;

__config__.checkAndRestore({
    distance: 32
});