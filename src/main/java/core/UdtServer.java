package core;

import common.Constants;
import common.RawData;
import common.UdtException;
import factory.PacketFactory;
import packet.HandshakePacket;
import util.UdpSocketUtil;
import worker.RequestHandler;

import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

import static common.Messages.*;

/**
 * Server for data sending.
 * @author Shaofeng Chen
 * @since 4/25/14
 */
public class UdtServer {
    private final int port;
    private final DatagramSocket socket;
    private final Map<String, RequestHandler> servedClients;
    private final Map<String, String> clients;
    private final int initSeqNo;
    private static final Logger logger = Logger.getLogger(UdtServer.class.getName());

    public UdtServer(int port) {
        this.port = port;
        initSeqNo = new Random().nextInt(Constants.MAX_SEQ_NO);
        socket = UdpSocketUtil.datagramSocket(port);
        servedClients = new HashMap<String, RequestHandler>();
        clients = new HashMap<String, String>();
    }

    public void run() {
        logger.info(String.format("Running server with init seq no %s.", initSeqNo));
        while (true) {
            DatagramPacket inPacket = UdpSocketUtil.receiveFrom(socket);
            dispatch(inPacket);
        }
    }

    private void dispatch(DatagramPacket datagramPacket) {
        byte[] data = Arrays.copyOf(datagramPacket.getData(), datagramPacket.getLength());
        int remotePort = datagramPacket.getPort();
        InetAddress remoteAddr = datagramPacket.getAddress();
        String clientId = getClientId(remoteAddr, remotePort);
        RawData rawData = new RawData(data);
        int type = rawData.get32();
        if (type == Constants.HANDSHAKE_REQ) {
            HandshakePacket request = PacketFactory.createHandshakePacket(type, rawData);
            process(request, remoteAddr, remotePort);
        } else if (type == Constants.HANDSHAKE_RSP) {
            if (clients.containsKey(clientId) && !servedClients.containsKey(clientId)) {
                serve(remoteAddr, remotePort, clients.get(clientId));
            }
        } else if (type == Constants.SHUTDOWN) {
            servedClients.get(clientId).shutdown();
        }
    }

    private void send(DatagramPacket datagramPacket) {
        UdpSocketUtil.send(socket, datagramPacket);
    }

    private void serve(InetAddress remoteAddr, int remotePort, String filePath) {
        logger.info(String.format("Serve request from %s:%s.", remoteAddr, remotePort));
        RequestHandler requestHandler = new RequestHandler(remoteAddr, remotePort, filePath,
                initSeqNo);
        String clientId = String.format("%s:%s", remoteAddr, remotePort);
        servedClients.put(clientId, requestHandler);
        requestHandler.start();

    }

    private String getClientId(InetAddress inetAddress, int port) {
        return String.format("%s:%s", inetAddress, port);
    }

    private void process(HandshakePacket request, InetAddress remoteAddr, int remotePort) {
        String filePath = request.getMessage();
        File file = new File(filePath);
        String clientId = getClientId(remoteAddr, remotePort);
        HandshakePacket response;
        if (file.exists()) {
            clients.put(clientId, filePath);
            response = new HandshakePacket(Constants.HANDSHAKE_RSP, initSeqNo,
                    Constants.MAX_BUFFER_SIZE, String.valueOf(file.length()));
        } else {
            response = new HandshakePacket(Constants.HANDSHAKE_RSP, initSeqNo,
                    Constants.MAX_BUFFER_SIZE, FILE_NOT_EXIST_ERR);
        }
        logger.info(String.format("Get handshake request from %s.", clientId));
        DatagramPacket outPacket = new DatagramPacket(response.toByteArray(),
                response.getByteLength(), remoteAddr, remotePort);
        send(outPacket);
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            throw new UdtException(SERVER_USAGE);
        }
        int port = Integer.parseInt(args[0]);
        UdtServer server = new UdtServer(port);
        server.run();
    }
}
