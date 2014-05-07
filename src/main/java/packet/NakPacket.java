package packet;

import common.Constants;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for nak packet format.
 * @author Shaofeng Chen
 * @since 4/25/14
 */
public class NakPacket implements Packet {

    private final int type;
    private final List<Integer> lostPackets;

    public NakPacket() {
        type = Constants.NAK;
        lostPackets = new ArrayList<Integer>();
    }

    public void addLostPacket(int seqNo) {
        lostPackets.add(seqNo);
    }

    public List<Integer> getLostPackets() {
        return lostPackets;
    }

    @Override
    public int getByteLength() {
        return 8 + lostPackets.size() * 4;
    }

    @Override
    public byte[] toByteArray() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(getByteLength());
        byteBuffer.putInt(type);
        byteBuffer.putInt(lostPackets.size());
        for (int lostPacketInfo : lostPackets) {
            byteBuffer.putInt(lostPacketInfo);
        }
        return byteBuffer.array();
    }
}
