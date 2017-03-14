package de.securitysquad.webifier.core.tester;

/**
 * Created by samuel on 08.11.16.
 */
public interface WebifierTesterResultListener {

    void onStarted(String id, String url);

    void onFinished(String id, WebifierOverallTesterResult result);
}
