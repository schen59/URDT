package packet;

import common.Constants;

import java.nio.ByteBuffer;

/**
 * Class for shutdown packet format.
 * @author Shaofeng Chen
 * @since 4/30/14
 */
public class ShutdownPacket implements Packet {

    private final int type;

    public ShutdownPacket() {
        type = Constants.SHUTDOWN;
    }

    @Override
    public int getByteLength() {
        return 4;
    }

    @Override
    public byte[] toByteArray() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(getByteLength());
        byteBuffer.putInt(type);
        return byteBuffer.array();
    }
}
