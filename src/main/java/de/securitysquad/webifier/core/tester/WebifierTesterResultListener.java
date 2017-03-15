package de.securitysquad.webifier.core.tester;

/**
 * Created by samuel on 08.11.16.
 */
public interface WebifierTesterResultListener {

    void onQueued(String id, String url, int waitingPosition);

    void onFinished(String id, WebifierOverallTesterResult result);
}
