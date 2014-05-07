package worker;

import common.Constants;
import common.UdtException;
import core.UdtClient;
import packet.HandshakePacket;

import java.util.logging.Logger;

import static common.Messages.*;

/**
 * Class responsible for sending handshake packet.
 * @author Shaofeng Chen
 * @since 4/25/14
 */
public class HandShaker extends Sender {

    private final UdtClient udtClient;
    private static final Logger logger = Logger.getLogger(HandShaker.class.getName());

    public HandShaker(UdtClient udtClient) {
        super(udtClient.getRemoteServer(), udtClient.getRemotePort(), udtClient.getSocket());
        this.udtClient = udtClient;
    }

    @Override
    protected boolean isDone() {
        return udtClient.isConnected();
    }

    @Override
    protected void fallAsleep() {
        try {
            sleep(Constants.HANDSHAKE_TIMEOUT);
        } catch (InterruptedException ex) {
            throw new UdtException("Handshaker interrupted.", ex);
        }
    }

    @Override
    protected byte[] createDatagram() {
        HandshakePacket handshakePacket = createPacket();
        return handshakePacket.toByteArray();
    }

    @Override
    protected void process() {

    }

    private HandshakePacket createPacket() {
        HandshakePacket handshakePacket = new HandshakePacket(udtClient.getHandshakePhase(), 0,
                udtClient.getBufferSize(), udtClient.getRemoteFilePath());
        if (handshakePacket.getByteLength() > Constants.DEFAULT_MTU) {
            throw new UdtException(PACKET_SIZE_ERR);
        }
        return handshakePacket;
    }
}
