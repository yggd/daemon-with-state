package org.yggd.spring.daemonwithstate.receiver;

import java.util.Arrays;

public enum RequestState {
    POLLING,
    PAUSE,
    RESTART,
    SHUTDOWN;

    public static boolean containsString(String enumStr) {
        return Arrays.stream(RequestState.values()).anyMatch( r -> enumStr.equals(r.name()));
    }
}
