package de.securitysquad.webifier.core.tester;

/**
 * Created by samuel on 13.02.17.
 */
public class WebifierLauncherResult {
    private String id;
    private int waitingPosition;

    public WebifierLauncherResult(String id, int waitingPosition) {
        this.id = id;
        this.waitingPosition = waitingPosition;
    }

    public String getId() {
        return id;
    }

    public int getWaitingPosition() {
        return waitingPosition;
    }
}