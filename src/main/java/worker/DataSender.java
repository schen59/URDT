package worker;

import common.Constants;
import common.UdtException;
import packet.Ack2Packet;
import packet.AckPacket;
import packet.DataPacket;
import packet.NakPacket;
import util.CommonUtil;

import java.io.FileInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Logger;

/**
 * Class responsible for sending data to client.
 * @author Shaofeng Chen
 * @since 4/26/14
 */
public class DataSender extends Sender {

    private int ackSeqNo;
    private int unackSeqNo;
    private int windowSize;
    private int bufferSize;
    private int packetInterval;
    private Set<Integer> lostList;
    private Map<Integer, DataPacket> buffer;
    private boolean isFinished;
    private boolean isInterrupted;
    private long expTimer;
    private final FileInputStream fileInputStream;
    private final DatagramSocket socket;
    private final static Logger logger = Logger.getLogger(DataSender.class.getName());

    public DataSender(InetAddress romoteAddr, int remotePort, String filePath, int initSeqNo,
            DatagramSocket socket) {
        super(romoteAddr, remotePort, socket);
        this.socket = socket;
        ackSeqNo = initSeqNo;
        unackSeqNo = ackSeqNo - 1;
        bufferSize = Constants.MAX_BUFFER_SIZE;
        windowSize = Constants.DEFAULT_WINDOW_SIZE;
        lostList = new CopyOnWriteArraySet<Integer>();
        buffer = new HashMap<Integer, DataPacket>();
        fileInputStream = CommonUtil.createFileInputStream(filePath);
        isFinished = false;
        isInterrupted = false;
        expTimer = 0;
        packetInterval = 0;
    }

    /**
     * Get the socket used for sending data.
     * @return DatagramSocket
     */
    public DatagramSocket getSocket() {
        return socket;
    }

    /**
     * Trigger the expiration event.
     */
    public void triggerExpEvent() {
        if (expTimer > Constants.EXP_TIMEOUT) {
            synchronized (lostList) {
                for (int seqNo = ackSeqNo; seqNo<=unackSeqNo; seqNo++) {
                    lostList.add(seqNo);
                }
            }
            expTimer = 0;
        }
    }

    /**
     * Process the received ack packet from client.
     * @param ackPacket
     */
    public void process(AckPacket ackPacket) {
        update(ackPacket);
        int ackSeqNo = ackPacket.getAckSeqNo();
        synchronized (lostList) {
            for (int seqNo = this.ackSeqNo; seqNo<ackSeqNo; seqNo++) {
                buffer.remove(seqNo);
                bufferSize += Constants.DEFAULT_MTU;
                lostList.remove(seqNo);
            }
        }
        this.ackSeqNo = ackSeqNo;
        Ack2Packet ack2Packet = new Ack2Packet(this.ackSeqNo, ackPacket.getSeqNo());
        DatagramPacket datagramPacket = createDatagramPacket(ack2Packet.toByteArray());
        sendPacket(datagramPacket);
    }

    /**
     * Process the received nak packet from client.
     * @param nakPacket
     */
    public void process(NakPacket nakPacket) {
        synchronized (lostList) {
            for (int seqNo : nakPacket.getLostPackets()) {
                if (seqNo >= ackSeqNo) {
                    lostList.add(seqNo);
                }
            }
        }
        expTimer = 0;
    }

    /**
     * Check if there is no pending data packet.
     * @return boolean
     */
    public boolean isClear() {
        return lostList.isEmpty() && getUnackPacketsNumber() == 0;
    }

    @Override
    public void run() {
        sendData();
        logger.info("Done data sending.*********************************************************");
        cleanUp();
        logger.info("Done transfering.");
    }

    public void shutdown() {
        isInterrupted = true;
    }

    @Override
    protected boolean isDone() {
        return isFinished;
    }

    @Override
    protected void fallAsleep() {
        try {
            sleep(packetInterval);
        } catch (InterruptedException ex) {
            throw new UdtException("DataSender interrupted.", ex);
        }
    }

    @Override
    protected byte[] createDatagram() {
        DataPacket dataPacket = createDataPacket();
        if (dataPacket == null) {
            return null;
        }
        return dataPacket.toByteArray();
    }

    @Override
    protected void process() {
    }

    /**
     * Update window size and packet sending rate according to the received ack packet.
     * @param ackPacket
     */
    private void update(AckPacket ackPacket) {
        if (ackPacket.getRecvRate() > 0) {
            windowSize = ackPacket.getRecvRate() * (Constants.SYN_TIMEOUT + ackPacket.getRtt()) + 16;
            packetInterval = 1000 / ackPacket.getRecvRate();
        } else {
            windowSize = ackPacket.getBufferSize() / Constants.DEFAULT_MTU;
            packetInterval = 0;
        }
    }

    private DataPacket createDataPacket() {
        int seqNo = CommonUtil.nextSeqNo(unackSeqNo);
        unackSeqNo++;
        byte[] dataBuffer = new byte[Constants.DEFAULT_MTU-Constants.DATA_PACK_OVERHEAD];
        int byteRead = CommonUtil.readFileInputStream(fileInputStream, dataBuffer);
        DataPacket dataPacket;
        if (byteRead == -1) {
            isFinished = true;
            return null;
        } else {
            dataPacket = new DataPacket(seqNo, Arrays.copyOf(dataBuffer, byteRead));
        }
        buffer.put(seqNo, dataPacket);
        bufferSize -= Constants.DEFAULT_MTU;
        return dataPacket;
    }

    private int getUnackPacketsNumber() {
        return unackSeqNo - ackSeqNo + 1;
    }

    private void retransmit() {
        int lostSeqNo = new PriorityQueue<Integer>(lostList).peek();
        DataPacket dataPacket = buffer.get(lostSeqNo);
        DatagramPacket datagramPacket = createDatagramPacket(dataPacket.toByteArray());
        sendPacket(datagramPacket);
        lostList.remove(lostSeqNo);
    }

    private void sendData() {
        while (!isDone()) {
            fallAsleep();
            long t1 = System.currentTimeMillis();
            synchronized (lostList) {
                if (!lostList.isEmpty()) {
                    retransmit();
                    continue;
                }
            }
            if (bufferSize > 0 && getUnackPacketsNumber() < windowSize) {
                sendPacket();
            } else {
                fallAsleep();
            }
            long t2 = System.currentTimeMillis();
            expTimer += t2 - t1;
            triggerExpEvent();
        }
    }

    private void cleanUp() {
        while (!isClear() && !isInterrupted) {
            fallAsleep();
            long t1 = System.currentTimeMillis();
            synchronized (lostList) {
                if (!lostList.isEmpty()) {
                    retransmit();
                }
            }
            long t2 = System.currentTimeMillis();
            expTimer += t2 - t1;
            triggerExpEvent();
        }
    }
}
