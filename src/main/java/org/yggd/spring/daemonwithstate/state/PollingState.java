package org.yggd.spring.daemonwithstate.state;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yggd.spring.daemonwithstate.receiver.ReceiveMessage;
import org.yggd.spring.daemonwithstate.receiver.Receiver;

import java.util.concurrent.BlockingQueue;

@Component
public class PollingState implements State {

    private static final Logger logger = LoggerFactory.getLogger(PollingState.class);

    @Autowired
    private Receiver receiver;

    @Autowired
    private ShutdownState shutdownState;

    @Autowired
    private PauseState pauseState;

    @Autowired
    private BlockingQueue<ReceiveMessage> queue;

    @Override
    public void action(StateContext context) throws InterruptedException {
        logger.info("enter PollingState.");
        final ReceiveMessage receive = receiver.receive();
        switch (receive.getState()) {
            case POLLING:
                context.setState(this);
                queue.put(receive);
                break;
            case SHUTDOWN:
                context.setState(shutdownState);
                break;
            case PAUSE:
                context.setState(pauseState);
                break;
            default:
                logger.warn("Illegal state: {} ignored.", receive.getState());
        }
    }
}
