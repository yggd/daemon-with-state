package org.yggd.spring.daemonwithstate.receiver;

public interface Receiver {
    ReceiveMessage receive() throws InterruptedException;
}
