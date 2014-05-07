package core;

import common.Constants;
import common.RawData;
import common.UdtException;
import factory.PacketFactory;
import packet.Ack2Packet;
import packet.DataPacket;
import packet.HandshakePacket;
import packet.ShutdownPacket;
import predictor.ArrivalSpeedPredictor;
import predictor.RttPredictor;
import util.CommonUtil;
import util.UdpSocketUtil;
import worker.AckReporter;
import worker.HandShaker;
import worker.NakReporter;
import worker.ProgressReporter;

import java.io.FileOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Logger;

import static common.Messages.*;

/**
 * Client for data receiving.
 * @author Shaofeng Chen
 * @since 4/25/14
 */
public class UdtClient extends Thread {

    private int bufferSize;
    private Map<Integer, DataPacket> buffer;
    private boolean isHandShakeDone;
    private boolean isTransferStarted;
    private boolean isTransferDone;
    private int handshakePhase;
    private int lrsn;
    private int seqNo;
    private long totalBytes;
    private long bytesReceived;
    private Set<Integer> lostList;
    private final String remoteServer;
    private final int remotePort;
    private final String remoteFilePath;
    private final FileOutputStream fileOutputStream;
    private final DatagramSocket socket;
    private final RttPredictor rttPredictor;
    private final ArrivalSpeedPredictor arrivalSpeedPredictor;
    private final ProgressReporter progressReporter;
    private static final Logger logger = Logger.getLogger(UdtClient.class.getName());

    public UdtClient(String remoteServer, int remotePort, String remoteFilePath,
            String localFilePath) {
        this.remoteServer = remoteServer;
        this.remotePort = remotePort;
        this.remoteFilePath = remoteFilePath;
        socket = UdpSocketUtil.createSocket();
        bufferSize = Constants.MAX_BUFFER_SIZE;
        buffer = new HashMap<Integer, DataPacket>();
        lostList = new CopyOnWriteArraySet<Integer>();
        bytesReceived = 0;
        isHandShakeDone = false;
        isTransferDone = false;
        isTransferStarted = false;
        handshakePhase = Constants.HANDSHAKE_REQ;
        fileOutputStream = CommonUtil.createFileOutputStream(localFilePath);
        rttPredictor = new RttPredictor();
        arrivalSpeedPredictor = new ArrivalSpeedPredictor();
        progressReporter = new ProgressReporter(this);
    }

    /**
     * Get the phase of handshake, handshake has two phases(request, response).
     * @return int
     */
    public int getHandshakePhase() {
        return handshakePhase;
    }

    @Override
    public void run() {
        handshake();
        receiveData();
        shutdown();
    }

    /**
     * Get total bytes of the remote file.
     * @return long
     */
    public long getTotalBytes() {
        return totalBytes;
    }

    /**
     * Get total byte received for the remote file.
     * @return long
     */
    public long getBytesReceived() {
        return bytesReceived;
    }

    /**
     * Get the Round Trip Time predictor.
     * @return RttPredictor
     */
    public RttPredictor getRttPredictor() {
        return rttPredictor;
    }

    /**
     * Get the packet arrival speed predictor.
     * @return ArrivalSpeedPredictor
     */
    public ArrivalSpeedPredictor getArrivalSpeedPredictor() {
        return arrivalSpeedPredictor;
    }

    /**
     * Get current highest acknowledged packet sequence number.
     * @return int
     */
    public int getAckSeqNo() {
        synchronized (lostList) {
            PriorityQueue<Integer> priorityQueue = new PriorityQueue<Integer>(lostList);
            return priorityQueue.isEmpty() ? lrsn+1 : priorityQueue.peek();
        }
    }

    /**
     * Get current expected starting sequence number.
     * @return int
     */
    public int getSeqNo() {
        return seqNo;
    }

    /**
     * Get current lost list.
     * @return Set
     */
    public Set<Integer> getLostList() {
        synchronized (lostList) {
            return lostList;
        }
    }

    /**
     * Get current buffer size.
     * @return int
     */
    public int getBufferSize() {
        return bufferSize;
    }

    public String getRemoteFilePath() {
        return remoteFilePath;
    }

    public DatagramSocket getSocket() {
        return socket;
    }

    public String getRemoteServer() {
        return remoteServer;
    }

    public int getRemotePort() {
        return remotePort;
    }

    /**
     * Check if the client is connected to server.
     * @return boolean
     */
    public boolean isConnected() {
        return isHandShakeDone && isTransferStarted;
    }

    /**
     * Check if the data transfer is done.
     * @return boolean
     */
    public boolean isDone() {
        return isTransferDone;
    }

    private void receiveData() {
        while (!isTransferDone) {
            try {
                DatagramPacket datagramPacket = UdpSocketUtil.receiveWithTimeout(socket, 30000);
                dispatch(datagramPacket);
            } catch (SocketTimeoutException ex) {
                throw new UdtException(String.format(LOST_CONN_ERR,
                        remoteServer, remotePort), ex);
            }
        }
    }

    private void shutdown() {
        ShutdownPacket shutdownPacket = new ShutdownPacket();
        DatagramPacket datagramPacket = new DatagramPacket(shutdownPacket.toByteArray(),
                shutdownPacket.getByteLength(), CommonUtil.getInetAddressFrom(remoteServer),
                remotePort);
        UdpSocketUtil.send(socket, datagramPacket);
        progressReporter.shutdown();
        CommonUtil.closeFileOutputStream(fileOutputStream);
    }

    private void dispatch(DatagramPacket datagramPacket) {
        byte[] data = Arrays.copyOf(datagramPacket.getData(), datagramPacket.getLength());
        RawData rawData = new RawData(data);
        int type = rawData.get32();
        if (type == Constants.DATA) {
            if (!isTransferStarted) {
                startDataReceiving(datagramPacket.getAddress(), datagramPacket.getPort());
            }
            DataPacket dataPacket = PacketFactory.createDataPacket(rawData);
            process(dataPacket);
        } else if (type == Constants.ACK2) {
            Ack2Packet ack2Packet = PacketFactory.createAck2Packet(rawData);
            process(ack2Packet);
        } else if (type == Constants.SHUTDOWN) {
            isTransferDone = true;
            CommonUtil.closeFileOutputStream(fileOutputStream);
        } else if (type == Constants.HANDSHAKE_RSP) {
            handshakePhase = Constants.HANDSHAKE_RSP;
            isHandShakeDone = true;
            HandshakePacket handshakePacket = PacketFactory.createHandshakePacket(type,
                    rawData);
            process(handshakePacket);
        }
    }

    private void startDataReceiving(InetAddress remoteAddr, int remotePort) {
        isTransferStarted = true;
        new AckReporter(this, remoteAddr, remotePort, seqNo).start();
        new NakReporter(this, remoteAddr, remotePort).start();
        progressReporter.start();
    }

    private void handshake() {
        HandShaker handShaker = new HandShaker(this);
        handShaker.start();
        while (!isHandShakeDone) {
            try {
                DatagramPacket datagramPacket = UdpSocketUtil.receiveWithTimeout(socket, 3000);
                dispatch(datagramPacket);
            } catch (SocketTimeoutException ex) {
                logger.info(String.format(CONN_ERR, remoteServer, remotePort));
                System.exit(1);
            }
        }
    }

    private void process(DataPacket dataPacket) {
        arrivalSpeedPredictor.record(dataPacket, System.currentTimeMillis());
        int curSeqNo = dataPacket.getSeqNo();
        if (!buffer.containsKey(curSeqNo) && bufferSize-Constants.DEFAULT_MTU >= 0) {
            buffer.put(curSeqNo, dataPacket);
            bufferSize -= Constants.DEFAULT_MTU;
        }
        if (buffer.containsKey(curSeqNo)) {
            if (curSeqNo > lrsn + 1) {
                for (int i=lrsn+1; i<curSeqNo; i++) {
                    if (i >= seqNo) {
                        lostList.add(i);
                    }
                }
            } else if (curSeqNo < lrsn) {
                lostList.remove(curSeqNo);
            }
            lrsn = (lrsn < curSeqNo ? curSeqNo : lrsn);
        }
    }

    private void process(Ack2Packet ack2Packet) {
        rttPredictor.record(ack2Packet, System.currentTimeMillis());
        for (int i = seqNo; i<ack2Packet.getAckSeqNo(); i++) {
            DataPacket dataPacket = buffer.get(i);
            bytesReceived += Constants.DEFAULT_MTU - Constants.DATA_PACK_OVERHEAD;
            CommonUtil.writeFileOutputStream(fileOutputStream, dataPacket.getPayload());

            buffer.remove(i);
            lostList.remove(i);
            bufferSize += Constants.DEFAULT_MTU;
        }
        if (bytesReceived >= totalBytes) {
            isTransferDone = true;
        }
        seqNo = ack2Packet.getAckSeqNo();
    }

    private void process(HandshakePacket handshakePacket) {
        lrsn = handshakePacket.getInitSeqNo() - 1;
        seqNo = handshakePacket.getInitSeqNo();
        if (handshakePacket.getMessage().equalsIgnoreCase(FILE_NOT_EXIST_ERR)) {
            logger.info(String.format(FILE_NOT_EXIST_ERR_FMT,
                    remoteFilePath, remoteServer));
            System.exit(1);
        }
        totalBytes = Long.parseLong(handshakePacket.getMessage());
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            throw new UdtException(CLIENT_USAGE);
        }
        String server = args[0];
        int port = Integer.parseInt(args[1]);
        String filePath = args[2];
        String localFilePath = args[3];
        UdtClient client = new UdtClient(server, port, filePath, localFilePath);
        client.start();
    }

}
