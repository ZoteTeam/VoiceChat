package com.reider745.voicechat.config;

import android.media.AudioRecord;
import com.reider745.voicechat.data.Constants;
import com.zhekasmirnov.apparatus.multiplayer.Network;
import lombok.Getter;
import org.json.JSONObject;

@Getter
public class ClientConfig {
    private final int rate;

    private final String host;
    private final int port;
    private final int distance;

    public ClientConfig(JSONObject config) {
        // from server config
        this.host = config.optString("host", "0.0.0.0");
        this.rate = config.optInt("rate", Constants.RATE_DEFAULT);
        this.port = config.optInt("port", Network.getSingleton().getConfig().getDefaultPort() + 1);
        this.distance = config.optInt("distance", 0);
    }

    public int getBuffer() {
        final double MIN_BUFFER = AudioRecord.getMinBufferSize(rate, Constants.CHANNEL_IN, Constants.AUDIO_ENCODING);
        final int BUFFER_SIZE = (int) (Math.ceil(MIN_BUFFER / Constants.FRAME) * Constants.FRAME);

        if(BUFFER_SIZE < MIN_BUFFER) {
            throw new RuntimeException("Buffer size too small");
        }

        return BUFFER_SIZE;
    }

    public static JSONObject from(ServerConfig config) {
        JSONObject json = new JSONObject();

        try {
            json.put("host", config.getHost());
            json.put("port", config.getPort());
            json.put("rate", config.getRate());
            json.put("distance", config.getDistance());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return json;
    }
}
