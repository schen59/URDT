package worker;

import util.UdpSocketUtil;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.logging.Logger;

/**
 * Common class for packet receiver.
 * @author Shaofeng Chen
 * @since 4/26/14
 */
public abstract class Receiver extends Thread {

    private final DatagramSocket socket;
    private final static Logger logger = Logger.getLogger(Receiver.class.getName());

    public Receiver(DatagramSocket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        while(!isDone()) {
            fallAsleep();
            DatagramPacket datagramPacket = receive();
            process(datagramPacket);
        }
    }

    private DatagramPacket receive() {
        return UdpSocketUtil.receiveFrom(socket);
    }

    /**
     * Check if receiver has done with its job.
     * @return boolean
     */
    protected abstract boolean isDone();

    /**
     * Let the receiver fall asleep.
     */
    protected abstract void fallAsleep();

    /**
     * Process the received datagram packet.
     * @param datagramPacket
     */
    protected abstract void process(DatagramPacket datagramPacket);
}
