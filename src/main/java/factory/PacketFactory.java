package factory;

import common.RawData;
import packet.Ack2Packet;
import packet.AckPacket;
import packet.DataPacket;
import packet.HandshakePacket;
import packet.NakPacket;

/**
 * Factory class for creating packet for raw byte data.
 * @author Shaofeng Chen
 * @since 4/25/14
 */
public class PacketFactory {

    public static HandshakePacket createHandshakePacket(int type, RawData rawData) {
        int initSeqNo = rawData.get32();
        int windowSize = rawData.get32();
        String message = new String(rawData.getRemainingBytes());
        return new HandshakePacket(type, initSeqNo, windowSize, message);
    }

    public static DataPacket createDataPacket(RawData rawData) {
        int seqNo = rawData.get32();
        byte[] payload = rawData.getRemainingBytes();
        return new DataPacket(seqNo, payload);
    }

    public static Ack2Packet createAck2Packet(RawData rawData) {
        int ackSeqNo = rawData.get32();
        int seqNo = rawData.get32();
        return new Ack2Packet(ackSeqNo, seqNo);
    }

    public static AckPacket createAckPacket(RawData rawData) {
        int ackSeqNo = rawData.get32();
        int seqNo = rawData.get32();
        int rtt = rawData.get32();
        int rttVar = rawData.get32();
        int bufferSize = rawData.get32();
        int recvRate = rawData.get32();
        int estLinkCap = rawData.get32();
        return new AckPacket(ackSeqNo, seqNo, rtt, rttVar, bufferSize, recvRate, estLinkCap);
    }

    public static NakPacket createNakPacket(RawData rawData) {
        int size = rawData.get32();
        NakPacket nakPacket = new NakPacket();
        for (int i=0; i<size; i++) {
            nakPacket.addLostPacket(rawData.get32());
        }
        return nakPacket;
    }
}
