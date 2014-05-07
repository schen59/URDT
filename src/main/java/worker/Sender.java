package worker;

import util.CommonUtil;
import util.UdpSocketUtil;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Logger;

/**
 * Common class for packet sending.
 * @author Shaofeng Chen
 * @since 4/26/14
 */
public abstract class Sender extends Thread {

    private final InetAddress remoteAddr;
    private final int remotePort;
    private final DatagramSocket socket;
    private final static Logger logger = Logger.getLogger(Sender.class.getName());

    public Sender(InetAddress remoteAddr, int remotePort, DatagramSocket socket) {
        this.remoteAddr = remoteAddr;
        this.remotePort = remotePort;
        this.socket = socket;
    }

    public Sender(String remoteAddr, int remotePort, DatagramSocket socket) {
        this(CommonUtil.getInetAddressFrom(remoteAddr), remotePort, socket);
    }

    @Override
    public void run() {
        while (!isDone()) {
            fallAsleep();
            process();
            sendPacket();
        }
    }

    private DatagramPacket createDatagramPacket() {
        byte[] datagram = createDatagram();
        if (datagram == null) {
            return null;
        }
        return createDatagramPacket(datagram);
    }

    protected void sendPacket() {
        DatagramPacket datagramPacket = createDatagramPacket();
        if (datagramPacket != null) {
            sendPacket(datagramPacket);
        }
    }

    protected void sendPacket(DatagramPacket datagramPacket) {
        UdpSocketUtil.send(socket, datagramPacket);
    }

    protected DatagramPacket createDatagramPacket(byte[] datagram) {
        DatagramPacket datagramPacket = new DatagramPacket(datagram, datagram.length, remoteAddr,
                remotePort);
        return datagramPacket;
    }

    /**
     * Check if the sender is done with its job.
     * @return boolean
     */
    protected abstract boolean isDone();

    /**
     * Create datagram for sending.
     * @return byte[]
     */
    protected abstract byte[] createDatagram();

    /**
     * Let the sender fall asleep.
     */
    protected abstract void fallAsleep();

    /**
     * Do the process for the sender.
     */
    protected abstract void process();
}
