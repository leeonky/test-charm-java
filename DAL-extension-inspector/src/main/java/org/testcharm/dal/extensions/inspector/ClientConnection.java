package org.testcharm.dal.extensions.inspector;

import org.testcharm.util.Sneaky;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

class ClientConnection {
    private final String sessionId;
    private final Socket socket;
    private final InputStream in;
    private final OutputStream out;
    private volatile boolean open = true;

    ClientConnection(String sessionId, Socket socket) throws IOException {
        this.sessionId = sessionId;
        this.socket = socket;
        in = socket.getInputStream();
        out = socket.getOutputStream();
    }

    String sessionId() {
        return sessionId;
    }

    boolean isOpen() {
        return open;
    }

    synchronized void sendText(String message) throws IOException {
        if (!open) {
            return;
        }
        byte[] payload = message.getBytes(StandardCharsets.UTF_8);
        writeFrame((byte) 0x1, payload);
    }

    private synchronized void sendPong(byte[] payload) throws IOException {
        if (!open) {
            return;
        }
        writeFrame((byte) 0xA, payload);
    }

    private synchronized void writeFrame(byte opcode, byte[] payload) throws IOException {
        int length = payload.length;
        out.write(0x80 | (opcode & 0x0F));
        if (length <= 125) {
            out.write(length);
        } else if (length <= 65535) {
            out.write(126);
            out.write((length >>> 8) & 0xFF);
            out.write(length & 0xFF);
        } else {
            out.write(127);
            for (int i = 7; i >= 0; i--) {
                out.write((int) ((long) length >>> (8 * i)) & 0xFF);
            }
        }
        out.write(payload);
        out.flush();
    }

    void readLoop() throws IOException {
        while (open) {
            int b0 = in.read();
            if (b0 == -1) {
                return;
            }
            int b1 = in.read();
            if (b1 == -1) {
                return;
            }

            int opcode = b0 & 0x0F;
            boolean masked = (b1 & 0x80) != 0;
            long payloadLength = b1 & 0x7F;

            if (payloadLength == 126) {
                payloadLength = ((long) readByte() << 8) | readByte();
            } else if (payloadLength == 127) {
                payloadLength = 0;
                for (int i = 0; i < 8; i++) {
                    payloadLength = (payloadLength << 8) | readByte();
                }
            }

            if (payloadLength > Integer.MAX_VALUE) {
                throw new IOException("WebSocket payload too large");
            }

            byte[] maskKey = null;
            if (masked) {
                maskKey = new byte[]{(byte) readByte(), (byte) readByte(), (byte) readByte(), (byte) readByte()};
            }

            byte[] payload = new byte[(int) payloadLength];
            readFully(payload);
            if (masked && maskKey != null) {
                for (int i = 0; i < payload.length; i++) {
                    payload[i] = (byte) (payload[i] ^ maskKey[i % 4]);
                }
            }

            if (opcode == 0x8) {
                return;
            }
            if (opcode == 0x9) {
                sendPong(payload);
            }
        }
    }

    private int readByte() throws IOException {
        int value = in.read();
        if (value == -1) {
            throw new EOFException();
        }
        return value & 0xFF;
    }

    private void readFully(byte[] bytes) throws IOException {
        int offset = 0;
        while (offset < bytes.length) {
            int read = in.read(bytes, offset, bytes.length - offset);
            if (read == -1) {
                throw new EOFException();
            }
            offset += read;
        }
    }

    void close() {
        open = false;
        Sneaky.run(socket::close);
    }
}
