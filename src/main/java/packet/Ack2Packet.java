package packet;

import common.Constants;

import java.nio.ByteBuffer;

/**
 * Class for ack2 packet format.
 * @author Shaofeng Chen
 * @since 4/25/14
 */
public class Ack2Packet implements Packet {

    private final int type;
    private final int ackSeqNo;
    private final int seqNo;

    public Ack2Packet(int ackSeqNo, int seqNo) {
        type = Constants.ACK2;
        this.ackSeqNo = ackSeqNo;
        this.seqNo = seqNo;
    }

    public int getAckSeqNo() {
        return ackSeqNo;
    }

    public int getSeqNo() {
        return seqNo;
    }

    @Override
    public int getByteLength() {
        return 12;
    }

    @Override
    public byte[] toByteArray() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(getByteLength());
        byteBuffer.putInt(type);
        byteBuffer.putInt(ackSeqNo);
        byteBuffer.putInt(seqNo);
        return byteBuffer.array();
    }
}
