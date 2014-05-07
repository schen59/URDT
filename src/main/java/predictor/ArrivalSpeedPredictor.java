package predictor;

import packet.DataPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Packet arrival speed predictor.
 * @author Shaofeng Chen
 * @since 4/28/14
 */
public class ArrivalSpeedPredictor {

    private final static int PACKETS_TO_KEEP = 16;
    private final static int MILISECS_PER_SEC = 1000;
    private final List<Long> packetRecorder;

    public ArrivalSpeedPredictor() {
        packetRecorder = new CopyOnWriteArrayList<Long>();
    }

    public void record(DataPacket dataPacket, long arrivalTime) {
        synchronized (packetRecorder) {
            if (packetRecorder.size() == PACKETS_TO_KEEP) {
                packetRecorder.remove(0);
            }
            packetRecorder.add(arrivalTime);
        }
    }

    public double getEstimatedArrivalSpeed() {
        double estimatedArrivalSpeed = 0;
        List<Long> intervals = new ArrayList<Long>();
        double avgInterval = calculateIntervals(intervals);
        int validIntervals = 0;
        int cutoff = PACKETS_TO_KEEP / 2;
        for (int i=0; i<intervals.size(); i++) {
            if (intervals.get(i) > avgInterval/cutoff && intervals.get(i) < avgInterval*cutoff) {
                validIntervals++;
                estimatedArrivalSpeed += intervals.get(i);
            }
        }
        if (validIntervals >= cutoff) {
            return validIntervals*MILISECS_PER_SEC / estimatedArrivalSpeed;
        }
        return 0;
    }

    private double calculateIntervals(List<Long> intervals) {
        double avgInterval = 0;
        synchronized (packetRecorder) {
            for (int i=1; i<packetRecorder.size(); i++) {
                intervals.add(packetRecorder.get(i) - packetRecorder.get(i-1));
                avgInterval += packetRecorder.get(i) - packetRecorder.get(i-1);
            }
        }
        return avgInterval / intervals.size();
    }
}
