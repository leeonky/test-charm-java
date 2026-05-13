package org.testcharm.cucumber.swarm;

public class RemoteException extends Throwable {
    public final String target;

    public RemoteException(Throwable throwable) {
        super(throwable.getMessage());
        target = throwable.getClass().getName();
        setStackTrace(throwable.getStackTrace());
    }
}
