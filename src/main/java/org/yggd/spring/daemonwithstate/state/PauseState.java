package org.yggd.spring.daemonwithstate.state;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yggd.spring.daemonwithstate.receiver.ReceiveMessage;
import org.yggd.spring.daemonwithstate.receiver.Receiver;
import org.yggd.spring.daemonwithstate.receiver.RequestState;

@Component
public class PauseState implements State {

    private static final Logger logger = LoggerFactory.getLogger(PauseState.class);

    @Autowired
    private Receiver receiver;

    @Autowired
    private PollingState pollingState;

    @Autowired
    private ShutdownState shutdownState;

    @Override
    public void action(StateContext context) throws InterruptedException {
        logger.info("enter PauseState");
        final ReceiveMessage receive = receiver.receive();
        switch (receive.getState()) {
            case POLLING:
                context.setState(pollingState);
                break;
            case RESTART:
                context.setState(pollingState);
                break;
            case SHUTDOWN:
                context.setState(shutdownState);
                break;
            default:
                // do nothing.
        }
        // ignore other state (e.g., polling with so_timeout)
    }
}
