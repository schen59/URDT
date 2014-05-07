package worker;

import common.Constants;
import common.UdtException;
import core.UdtClient;
import packet.AckPacket;
import predictor.ArrivalSpeedPredictor;
import predictor.RttPredictor;
import util.CommonUtil;

import java.net.InetAddress;
import java.util.Random;
import java.util.logging.Logger;

/**
 * Class responsible for sending ack packet.
 * @author Shaofeng Chen
 * @since 4/26/14
 */
public class AckReporter extends Sender {

    private int largestAckSeqNo;
    private int seqNo;
    private final UdtClient udtClient;
    private final RttPredictor rttPredictor;
    private final ArrivalSpeedPredictor arrivalSpeedPredictor;

    private final static Logger logger = Logger.getLogger(AckReporter.class.getName());

    public AckReporter(UdtClient udtClient, InetAddress remoteAddr, int remotePort,
            int initSeqNo) {
        super(remoteAddr, remotePort, udtClient.getSocket());
        this.udtClient = udtClient;
        largestAckSeqNo = initSeqNo;
        seqNo = new Random().nextInt(Constants.MAX_SEQ_NO);
        rttPredictor = udtClient.getRttPredictor();
        arrivalSpeedPredictor = udtClient.getArrivalSpeedPredictor();
    }

    @Override
    protected boolean isDone() {
        return udtClient.isDone();
    }

    @Override
    protected void fallAsleep() {
        try {
            sleep(Constants.ACK_TIMEOUT);
        } catch (InterruptedException ex) {
            throw new UdtException("AckReporter interrupted.", ex);
        }
    }

    @Override
    protected byte[] createDatagram() {
        AckPacket ackPacket = createAckPacket();
        if (ackPacket == null) {
            return null;
        }
        return ackPacket.toByteArray();
    }

    @Override
    protected void process() {
    }

    private AckPacket createAckPacket() {
        if (udtClient.getAckSeqNo() <= largestAckSeqNo) {
            return null;
        }
        AckPacket ackPacket = new AckPacket(udtClient.getAckSeqNo(), seqNo,
                (int)rttPredictor.getEstimatedRtt(), (int)rttPredictor.getEstimatedRttVar(),
                udtClient.getBufferSize(), (int)arrivalSpeedPredictor.getEstimatedArrivalSpeed(), 0);
        rttPredictor.record(ackPacket, System.currentTimeMillis());
        seqNo = CommonUtil.nextSeqNo(seqNo);
        largestAckSeqNo = ackPacket.getAckSeqNo();
        return ackPacket;
    }
}
