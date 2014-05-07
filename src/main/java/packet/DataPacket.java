package packet;

import common.Constants;

import java.nio.ByteBuffer;

/**
 * Class for data packet format.
 * @author Shaofeng Chen
 * @since 4/24/14
 */
public class DataPacket implements Packet {
    private final int type;
    private final int seqNo;
    private final byte[] payload;

    public DataPacket(int seqNo, byte[] payload) {
        type = Constants.DATA;
        this.seqNo = seqNo;
        this.payload = payload;
    }

    public int getSeqNo() {
        return seqNo;
    }

    public byte[] getPayload() {
        return payload.clone();
    }

    @Override
    public int getByteLength() {
        return 8 + (payload != null ? payload.length : 0);
    }

    @Override
    public byte[] toByteArray() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(getByteLength());
        byteBuffer.putInt(type);
        byteBuffer.putInt(seqNo);
        if (payload != null) {
            byteBuffer.put(payload);
        }
        return byteBuffer.array();
    }
}
