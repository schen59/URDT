package predictor;

import packet.Ack2Packet;
import packet.AckPacket;

import java.util.HashMap;
import java.util.Map;

/**
 * Round trip time predictor.
 * @author Shaofeng Chen
 * @since 4/28/14
 */
public class RttPredictor {

    private double estimatedRtt;
    private double estimatedRttVar;
    private final Map<Integer, Long> packetRecorder;
    private final static double ALPHA_RTT = 0.875;
    private final static double BETA_RTT = 0.125;
    private final static double ALPHA_RTTVAR = 0.75;
    private final static double BETA_RTTVAR = 0.25;
    private final static double MILISECS_PER_SEC = 1000.0;


    public RttPredictor() {
        packetRecorder = new HashMap<Integer, Long>();
        estimatedRtt = 0;
        estimatedRttVar = 0;
    }

    public void record(AckPacket ackPacket, long departureTime) {
        packetRecorder.put(ackPacket.getSeqNo(), departureTime);
    }

    public void record(Ack2Packet ack2Packet, long arivalTime) {
        int seqNo = ack2Packet.getSeqNo();
        if (packetRecorder.containsKey(seqNo)) {
            long departureTime = packetRecorder.get(seqNo);
            double rtt =  (arivalTime - departureTime) / MILISECS_PER_SEC;
            estimatedRtt = estimatedRtt* ALPHA_RTT + rtt* BETA_RTT;
            estimatedRttVar = estimatedRttVar*ALPHA_RTTVAR + Math.abs(estimatedRtt-rtt)*BETA_RTTVAR;
            packetRecorder.remove(seqNo);
        }
    }

    public double getEstimatedRtt() {
        return estimatedRtt;
    }

    public double getEstimatedRttVar() {
        return estimatedRttVar;
    }
}
