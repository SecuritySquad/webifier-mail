package de.securitysquad.webifier.core.tester;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.Assert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by samuel on 10.02.17.
 */
public class WebifierTester {
    private final long creationIndex;
    private final String id;
    private final String command;
    private final WebifierTesterResultListener listener;
    private final int timeoutInMinutes;
    private WebifierTesterState state;
    private Process testerProcess;
    private Thread testerThread;
    private Thread inputThread;
    private String url;
    private WebifierOverallTesterResult result;
    private boolean finished;

    public WebifierTester(String id, String url, String command, WebifierTesterResultListener listener, int timeoutInMinutes) {
        creationIndex = System.currentTimeMillis();
        Assert.notNull(id, "id must not be null!");
        Assert.notNull(url, "url must not be null!");
        Assert.notNull(command, "command must not be null!");
        this.id = id;
        this.url = url;
        this.command = command;
        this.listener = listener;
        this.timeoutInMinutes = timeoutInMinutes;
        result = new WebifierOverallTesterResult(id);
        state = WebifierTesterState.WAITING;
    }

    public void launch() {
        state = WebifierTesterState.RUNNING;
        fireStartedEvent();
        testerThread = new Thread(() -> {
            try {
                System.out.println(command);
                testerProcess = Runtime.getRuntime().exec(command);
                listenForInput(testerProcess.getInputStream());
                testerProcess.waitFor(timeoutInMinutes, TimeUnit.MINUTES);
                state = (testerProcess.exitValue() == 0) ? WebifierTesterState.FINISHED : WebifierTesterState.ERROR;
            } catch (IOException | InterruptedException e) {
                state = WebifierTesterState.ERROR;
                fireFinishedEventWithoutResult();
            }
        });
        testerThread.start();
    }

    private void listenForInput(InputStream inputStream) {
        inputThread = new Thread(() -> {
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            ObjectMapper mapper = new ObjectMapper();
            try {
                String line;
                while ((line = br.readLine()) != null && !inputThread.isInterrupted()) {
                    System.out.println(line);
                    try {
                        WebifierTestResultData result = mapper.readValue(line, WebifierTestResultData.class);
                        switch (result.getTyp()) {
                            case "ResolverFinished":
                                Map<String, Object> resolverResult = (Map<String, Object>) result.getResult();
                                if (resolverResult.containsKey("original")) {
                                    this.result.setFoundUrl((String) resolverResult.get("original"));
                                }
                                if (resolverResult.containsKey("resolved")) {
                                    this.result.setTestedUrl((String) resolverResult.get("resolved"));
                                }
                                break;
                            case "TestFinished":
                                this.result.addTestResult(result);
                                break;
                            case "TesterFinished":
                                this.result.setResult(WebifierTesterResult.valueOf((String) result.getResult()));
                                break;
                        }
                    } catch (IOException e) {
                        // no json line found
                    }
                }
                fireFinishedEvent();
            } catch (IOException e) {
                state = WebifierTesterState.ERROR;
                fireFinishedEventWithoutResult();
            }
        });
        inputThread.setDaemon(true);
        inputThread.start();
    }

    public WebifierTesterState getState() {
        return state;
    }

    public String getUrl() {
        return url;
    }

    public long getCreationIndex() {
        return creationIndex;
    }

    public void exit() {
        if (testerProcess.isAlive()) {
            testerProcess.destroy();
        }
        if (testerThread.isAlive()) {
            testerThread.interrupt();
        }
        if (inputThread.isAlive()) {
            inputThread.interrupt();
        }
    }

    private void fireStartedEvent() {
        if (listener != null)
            listener.onStarted(id, url);
    }

    private void fireFinishedEventWithoutResult() {
        if (listener != null && !finished)
            listener.onFinished(id, null);
        finished = true;
    }

    private void fireFinishedEvent() {
        if (listener != null && !finished)
            listener.onFinished(id, result);
        finished = true;
    }
}
