package org.yggd.spring.daemonwithstate.state;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.yggd.spring.daemonwithstate.controller.BatchDaemon;
import org.yggd.spring.daemonwithstate.receiver.ReceiveMessage;
import org.yggd.spring.daemonwithstate.receiver.RequestState;

import java.util.concurrent.BlockingQueue;

@Component
public class ShutdownState implements State {

    private static final Logger logger = LoggerFactory.getLogger(ShutdownState.class);

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    private BlockingQueue<ReceiveMessage> queue;

    @Autowired
    private BatchDaemon batchDaemon;

    @Override
    public void action(StateContext context) throws InterruptedException {
        logger.info("enter ShutdownState.");
        context.setState(this);
        final ReceiveMessage shutdownMessage = new ReceiveMessage();
        shutdownMessage.setState(RequestState.SHUTDOWN);
        queue.put(shutdownMessage);
        batchDaemon.prepareShutdown();
        taskExecutor.shutdown();
    }
}
