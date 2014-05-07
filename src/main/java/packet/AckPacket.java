package packet;

import common.Constants;

import java.nio.ByteBuffer;

/**
 * Class for ack packet format.
 * @author Shaofeng Chen
 * @since 4/25/14
 */
public class AckPacket implements Packet {

    private final int type;
    private final int ackSeqNo;
    private final int seqNo;
    private final int rtt;
    private final int rttVar;
    private final int bufferSize;
    private final int recvRate;
    private final int estLinkCap;

    public AckPacket(int ackSeqNo, int seqNo, int rtt, int rttVar, int bufferSize, int recvRate,
            int estLinkCap) {
        type = Constants.ACK;
        this.ackSeqNo = ackSeqNo;
        this.seqNo = seqNo;
        this.rtt = rtt;
        this.rttVar = rttVar;
        this.bufferSize = bufferSize;
        this.recvRate = recvRate;
        this.estLinkCap = estLinkCap;
    }

    public int getAckSeqNo() {
        return ackSeqNo;
    }

    public int getSeqNo() {
        return seqNo;
    }

    public int getRtt() {
        return rtt;
    }

    public int getRttVar() {
        return rttVar;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public int getRecvRate() {
        return recvRate;
    }

    public int getEstLinkCap() {
        return estLinkCap;
    }

    @Override
    public int getByteLength() {
        return 32;
    }

    @Override
    public byte[] toByteArray() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(getByteLength());
        byteBuffer.putInt(type);
        byteBuffer.putInt(ackSeqNo);
        byteBuffer.putInt(seqNo);
        byteBuffer.putInt(rtt);
        byteBuffer.putInt(rttVar);
        byteBuffer.putInt(bufferSize);
        byteBuffer.putInt(recvRate);
        byteBuffer.putInt(estLinkCap);
        return byteBuffer.array();
    }
}
