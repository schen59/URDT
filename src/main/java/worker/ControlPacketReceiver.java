package worker;

import common.Constants;
import common.RawData;
import common.UdtException;
import factory.PacketFactory;
import packet.AckPacket;
import packet.NakPacket;

import java.net.DatagramPacket;
import java.util.Arrays;
import java.util.logging.Logger;

/**
 * Class responsible for receiving control packet from client.
 * @author Shaofeng Chen
 * @since 4/26/14
 */
public class ControlPacketReceiver extends Receiver {

    private final DataSender dataSender;
    private final static Logger logger = Logger.getLogger(ControlPacketReceiver.class.getName());
    private boolean isInterrupted;

    public ControlPacketReceiver(DataSender dataSender) {
        super(dataSender.getSocket());
        this.dataSender = dataSender;
        isInterrupted = false;
    }

    @Override
    protected boolean isDone() {
        return dataSender.isDone() && dataSender.isClear() || isInterrupted;
    }

    @Override
    protected void fallAsleep() {
        try {
            sleep(Constants.SYN_TIMEOUT);
        } catch (InterruptedException ex) {
            throw new UdtException("ControlPacketReceiver interrupted.", ex);
        }
    }

    @Override
    protected void process(DatagramPacket datagramPacket) {
        byte[] data = Arrays.copyOf(datagramPacket.getData(), datagramPacket.getLength());
        RawData rawData = new RawData(data);
        int type = rawData.get32();
        if (type == Constants.ACK) {
            AckPacket ackPacket = PacketFactory.createAckPacket(rawData);
            dataSender.process(ackPacket);
        } else if (type == Constants.NAK) {
            NakPacket nakPacket = PacketFactory.createNakPacket(rawData);
            dataSender.process(nakPacket);
        }
    }

    public void shutdown() {
        isInterrupted = true;
    }
}
