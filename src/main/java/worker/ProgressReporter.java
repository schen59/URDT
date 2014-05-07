package worker;

import common.UdtException;
import core.UdtClient;

/**
 * Class responsible for reporting the data receiving progress.
 * @author Shaofeng Chen
 * @since 4/30/14
 */
public class ProgressReporter extends Thread {

    private final UdtClient udtClient;

    public ProgressReporter(UdtClient udtClient) {
        this.udtClient = udtClient;
    }

    public void run() {
        while (true) {
            long bytesReceived = udtClient.getBytesReceived();
            long totalBytes = udtClient.getTotalBytes();
            System.out.print(String.format("Received:%.1f%% Total:%s\r",
                       ((double)bytesReceived / totalBytes)*100, totalBytes));
            try {
                sleep(1000);
            } catch (InterruptedException ex) {
                throw new UdtException("Progress reporter interrupted.", ex);
            }
        }
    }

    public void shutdown() {
        long totalBytes = udtClient.getTotalBytes();
        System.out.println(String.format("Received:100%% Total:%s", totalBytes));
        System.exit(0);
    }
}
