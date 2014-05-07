package common;

/**
 * Constants used for whole project.
 * @author Shaofeng Chen
 * @since 4/25/14
 */
public class Constants {

    public static final int MAX_BUFFER_SIZE = 1024000;
    public static final int DEFAULT_WINDOW_SIZE = 16;
    public static final int DEFAULT_MTU = 1024;
    public static final int MAX_SEQ_NO = (1<<31) - 1;

    public static final int HANDSHAKE_REQ = 0;
    public static final int HANDSHAKE_RSP = 1;
    public static final int ACK = 2;
    public static final int NAK = 3;
    public static final int ACK2 = 4;
    public static final int DATA = 5;
    public static final int SHUTDOWN = 6;

    public static final int SYN_TIMEOUT = 10;
    public static final int ACK_TIMEOUT = 10;
    public static final int NAK_TIMEOUt = 10;
    public static final int HANDSHAKE_TIMEOUT = 10;
    public static final int EXP_TIMEOUT = 500;

    public static final int DATA_PACK_OVERHEAD = 8;
}
