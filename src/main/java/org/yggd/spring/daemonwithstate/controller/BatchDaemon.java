package org.yggd.spring.daemonwithstate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yggd.spring.daemonwithstate.receiver.ReceiveMessage;
import org.yggd.spring.daemonwithstate.receiver.RequestState;
import org.yggd.spring.daemonwithstate.state.StateProcessor;

import java.util.concurrent.BlockingQueue;

@Component
public class BatchDaemon {

    private static final Logger logger = LoggerFactory.getLogger(BatchDaemon.class);

    @Autowired
    private StateProcessor stateProcessor;

    @Autowired
    private BlockingQueue<ReceiveMessage> queue;

    @Autowired
    private PollingTask pollingTask;

    private volatile boolean shutdownCalled = false;

    public void start() {
        stateProcessor.process(); // process async.
        while(!shutdownCalled) {
            try {
                final ReceiveMessage message = queue.take();
                if (message.getState() == RequestState.SHUTDOWN) {
                    logger.info("normal shutdown sequence.");
                    break;
                }
                pollingTask.execute(message);
            } catch (InterruptedException e) {
                logger.warn("abnormal shutdown interrupted.");
                break;
            }
        }
    }

    public void prepareShutdown() {
        shutdownCalled = true;
        stateProcessor.prepareShutdown();
    }
}
