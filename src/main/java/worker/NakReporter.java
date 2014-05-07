package worker;

import common.Constants;
import common.UdtException;
import core.UdtClient;
import packet.NakPacket;

import java.net.InetAddress;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * Class responsible for sending nak packet to server.
 * @author Shaofeng Chen
 * @since 4/26/14
 */
public class NakReporter extends Sender {

    private final UdtClient udtClient;

    public NakReporter(UdtClient udtClient, InetAddress remoteAddr, int remotePort) {
        super(remoteAddr, remotePort, udtClient.getSocket());
        this.udtClient = udtClient;
    }

    @Override
    protected boolean isDone() {
        return udtClient.isDone();
    }

    @Override
    protected void fallAsleep() {
        try {
            sleep(Constants.NAK_TIMEOUt);
        } catch (InterruptedException ex) {
            throw new UdtException("Nak reporter interrupted.", ex);
        }
    }

    @Override
    protected byte[] createDatagram() {
        NakPacket nakPacket = createNakPacket();
        if (nakPacket == null) {
            return null;
        }
        return nakPacket.toByteArray();
    }

    @Override
    protected void process() {

    }

    private NakPacket createNakPacket() {
        NakPacket nakPacket = new NakPacket();
        Set<Integer> lostList = udtClient.getLostList();
        if (lostList.isEmpty()) {
            return null;
        }
        PriorityQueue<Integer> priorityQueue = new PriorityQueue<Integer>(udtClient.getLostList());
        for (int seqNo : priorityQueue) {
            if (nakPacket.getByteLength()+nakPacket.getByteLength() > Constants.DEFAULT_MTU) {
                break;
            }
            if (seqNo >= udtClient.getSeqNo()) {
                nakPacket.addLostPacket(seqNo);
            }
        }
        return nakPacket;
    }
}
