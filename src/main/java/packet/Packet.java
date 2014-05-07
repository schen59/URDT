package packet;

/**
 * Common interface for packet format.
 * @author Shaofeng Chen
 * @since 4/23/14
 */
public interface Packet {

    /**
     * Get packet length in byte.
     * @return int
     */
    public int getByteLength();

    /**
     * Convert the packet into byte array.
     * @return byte[]
     */
    public byte[] toByteArray();
}
