package worker;

import util.UdpSocketUtil;

import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Class responsible for handling data sending to client.
 * @author Shaofeng Chen
 * @since 4/26/14
 */
public class RequestHandler extends Thread {

    private final InetAddress remoteAddr;
    private final int remotePort;
    private final String filePath;
    private final int initSeqNo;
    private final DatagramSocket socket;
    private final DataSender dataSender;
    private final ControlPacketReceiver controlPacketReceiver;

    public RequestHandler(InetAddress remoteAddr, int remotePort, String filePath, int initSeqNo) {
        this.remoteAddr = remoteAddr;
        this.remotePort = remotePort;
        this.filePath = filePath;
        this.initSeqNo = initSeqNo;
        socket = UdpSocketUtil.createSocket();
        dataSender = new DataSender(remoteAddr, remotePort, filePath, initSeqNo,
                socket);
        controlPacketReceiver = new ControlPacketReceiver(dataSender);
    }

    public String getFilePath() {
        return filePath;
    }

    public InetAddress getRemoteAddr() {
        return remoteAddr;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public DatagramSocket getSocket() {
        return socket;
    }

    public int getInitSeqNo() {
        return initSeqNo;
    }

    @Override
    public void run() {
        dataSender.start();
        controlPacketReceiver.start();
    }

    public void shutdown() {
        dataSender.shutdown();
        controlPacketReceiver.shutdown();
    }
}
