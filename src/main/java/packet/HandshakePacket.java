package packet;

import java.nio.ByteBuffer;

/**
 * Class for handshake packet format.
 * @author Shaofeng Chen
 * @since 4/24/14
 */
public class HandshakePacket implements Packet {

    private final int type;
    private final int initSeqNo;
    private final int bufferSize;
    private final String message;

    public HandshakePacket(int type, int initSeqNo, int bufferSize, String message) {
        this.type = type;
        this.initSeqNo = initSeqNo;
        this.bufferSize = bufferSize;
        this.message = message;
    }

    public int getInitSeqNo() {
        return initSeqNo;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public String getMessage() {
        return message;
    }

    public int getByteLength() {
        return 12 + message.getBytes().length;
    }

    public byte[] toByteArray() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(getByteLength());
        byteBuffer.putInt(type);
        byteBuffer.putInt(initSeqNo);
        byteBuffer.putInt(bufferSize);
        byteBuffer.put(message.getBytes());
        return byteBuffer.array();
    }
}
