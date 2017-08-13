package org.yggd.spring.daemonwithstate.state;

public interface State {
    void action(StateContext context) throws InterruptedException;
}
