package common;

/**
 * Created by Sherwin on 5/1/14.
 */
public class Messages {

    public static final String EOF = "EOF error.";

    public static final String LOST_CONN_ERR = "Lost connection to server %s on port %s.";

    public static final String CONN_ERR = "Unable to connect to remote server %s on " +
            "port %s.";

    public static final String FILE_NOT_EXIST_ERR = "File not exist.";

    public static final String FILE_NOT_EXIST_ERR_FMT = "File %s doesn't exist on server %s.";

    public static final String CLIENT_USAGE = "Usage: java -cp [jar-name] core.UdtClient server-name " +
            "server-port " +
            "remote-file-path local-file-path";

    public static final String SERVER_USAGE = "Usage: java -cp [jar-name] core.UdtServer " +
            "port-number";

    public static final String FILE_NOT_EXIST_FMT = "File %s does not exist.";

    public static final String READ_FILE_STREAM_ERR = "Can not read from file input stream.";

    public static final String WRITE_FILE_STREAM_ERR = "Can not write to file output stream.";

    public static final String CLOSE_FILE_STREAM_ERR = "Can not close file output stream.";

    public static final String CREATE_SOCKET_ERR = "Can not create udp socket.";

    public static final String CREATE_SOCKET_ERR_FMT = "Can not create udp socket on port %s.";

    public static final String SEND_PACKET_ERR = "Can not send datagram packet.";

    public static final String RCV_PACKET_ERR = "Can not receive packet.";

    public static final String SET_TIMEOUT_ERR_FMT = "Unable to set timeout as %s milisecs.";

    public static final String PACKET_SIZE_ERR = "Packet size greater than mtu.";
}
