package worker;

import common.Constants;
import common.UdtException;

/**
 * Created by Sherwin on 4/27/14.
 */
public class ExpTrigger extends Thread {

    private final DataSender dataSender;

    public ExpTrigger(DataSender dataSender) {
        this.dataSender = dataSender;
    }

    @Override
    public void run() {
        while (!dataSender.isDone() && !dataSender.isClear()) {
            try {
                sleep(Constants.SYN_TIMEOUT);
                dataSender.triggerExpEvent();
            } catch (InterruptedException ex) {
                throw new UdtException("ExpTrigger interrupted.", ex);
            }
        }
    }
}
