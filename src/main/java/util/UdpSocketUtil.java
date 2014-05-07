package util;

import common.Constants;
import common.UdtException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import static common.Messages.*;

/**
 * Utility functions for udp socket.
 * @author Shaofeng Chen
 * @since 4/25/14
 */
public class UdpSocketUtil {

    public static DatagramSocket createSocket() {
        try {
            return new DatagramSocket();
        } catch (IOException ex) {
            throw new UdtException(CREATE_SOCKET_ERR, ex);
        }
    }

    public static DatagramSocket datagramSocket(int port) {
        try {
            return new DatagramSocket(port);
        } catch (IOException ex) {
            throw new UdtException(String.format(CREATE_SOCKET_ERR_FMT, port),
                    ex);
        }
    }

    public static void send(DatagramSocket socket, DatagramPacket packet) {
        try {
            socket.send(packet);
        } catch (IOException ex) {
            throw new UdtException(SEND_PACKET_ERR, ex);
        }
    }

    public static DatagramPacket receiveFrom(DatagramSocket socket) {
        byte[] buffer = new byte[Constants.DEFAULT_MTU];
        DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
        try {
            socket.receive(datagramPacket);
        } catch (IOException ex) {
            throw new UdtException(RCV_PACKET_ERR, ex);
        }
        return datagramPacket;
    }

    public static DatagramPacket receiveWithTimeout(DatagramSocket socket,
            int timeout) throws SocketTimeoutException {
        try {
            socket.setSoTimeout(timeout);
        } catch (SocketException ex) {
            throw new UdtException(String.format(SET_TIMEOUT_ERR_FMT,
                    timeout), ex);
        }
        byte[] buffer = new byte[Constants.DEFAULT_MTU];
        DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
        try {
            socket.receive(datagramPacket);
        } catch (SocketTimeoutException ex) {
            throw new SocketTimeoutException();
        } catch (IOException ex) {
            throw new UdtException(RCV_PACKET_ERR, ex);
        }
        return datagramPacket;
    }
}
