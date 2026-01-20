interface MicService {
    setListener(handler: HandlerSound): void;

    start(): void;
    
    stop(): void;

    isRecording(): boolean;
}


class MicAndroidApiServiceImpl implements MicService {
    private readonly recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, RATE, CHANNEL_IN, AUDIO_ENCODING,BUFFER_SIZE);
    private handler: HandlerSound;
    private record: boolean = false;

    public setListener(handler: HandlerSound): void {
        this.handler = handler;
    }

    public start(): void {
        if(this.record) throw new Error("Record enabled");

        Threading.initThread("mic", () => {
            const buffer: number[] = ByteBuffer.allocate(BUFFER_SIZE).array();

            this.recorder.startRecording();
            this.record = true;

            while (this.record) {
                const bytesRead = this.recorder.read(buffer, 0, BUFFER_SIZE);
                if (bytesRead > 0) {
                    this.handler(buffer, bytesRead);
                } else if (bytesRead == AudioRecord.ERROR_INVALID_OPERATION) {
                    Logger.error("VoiceMod", "ERROR_INVALID_OPERATION: Check recorder state");
                } else if (bytesRead == AudioRecord.ERROR_BAD_VALUE) {
                    Logger.error("VoiceMod", "ERROR_BAD_VALUE: Invalid parameters");
                    break;
                } else if (bytesRead == AudioRecord.ERROR_DEAD_OBJECT) {
                    Logger.error("VoiceMod", "ERROR_DEAD_OBJECT: AudioRecord is not valid");
                    break;
                }

                Thread.sleep(10);
            }

            this.recorder.stop();
        });
    }

    public stop(): void {
        if(!this.record) throw new Error("Record disabled");

        this.record = false;
    }

    public isRecording(): boolean {
        return this.record;
    }
}