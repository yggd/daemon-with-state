package org.yggd.spring.daemonwithstate.state;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class StateProcessor {

    private static final Logger logger = LoggerFactory.getLogger(StateProcessor.class);

    @Autowired
    private PollingState pollingState;

    private volatile boolean shutdownCalled = false;

    @Async
    public void process() {
        logger.info("start process.");
        final StateContext context = new StateContext();
        context.setState(pollingState);

        while(!shutdownCalled) {
            final State current = context.getState();
            try {
                current.action(context);
            } catch (InterruptedException e) {
                break;
            }
        }
        logger.info("end process.");
    }

    public void prepareShutdown() {
        this.shutdownCalled = true;
    }
}
