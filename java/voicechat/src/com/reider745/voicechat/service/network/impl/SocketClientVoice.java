package com.reider745.voicechat.service.network.impl;

import com.zhekasmirnov.horizon.runtime.logger.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class SocketClientVoice {
    private static final byte[] EMPTY = new byte[0];

    private Socket socket;
    private final DataInputStream inputStream;
    private final DataOutputStream outputStream;

    public SocketClientVoice(Socket socket, DataInputStream inputStream, DataOutputStream outputStream) {
        this.socket = socket;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    public byte[] handle() {
        try {
            int length = inputStream.available();
            if(length > 0) {
                byte[] bytes = new byte[length];
                inputStream.readFully(bytes, 0, length);
                return bytes;
            }
            return EMPTY;
        } catch (IOException e) {
            return EMPTY;
        }
    }

    public void send(byte[] buff, int length) {
        try {
            outputStream.write(buff, 0, Math.min(buff.length, length));
            outputStream.flush();
        } catch (IOException ignored) {}
    }

    public void send(short[] buff, int length) {
        try {
            for(int i = 0; i < Math.min(buff.length, length); i++) {
                outputStream.writeShort(buff[i]);
            }
            outputStream.flush();
        } catch (IOException ignored) {}
    }

    public void close() {
        try {
            socket.close();
            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
