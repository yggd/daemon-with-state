package org.yggd.spring.daemonwithstate.state;

public class StateContext {

    private State state;

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }
}
