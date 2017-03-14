package de.securitysquad.webifier.core.tester;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by samuel on 08.03.17.
 */
public class WebifierOverallTesterResult {
    private String id;
    private String foundUrl;
    private String testedUrl;
    private WebifierTesterResult result;
    private List<WebifierTestResultData> testResults;

    public WebifierOverallTesterResult(String id) {
        this.id = id;
        this.testResults = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setFoundUrl(String foundUrl) {
        this.foundUrl = foundUrl;
    }

    public String getFoundUrl() {
        return foundUrl;
    }

    public String getTestedUrl() {
        return testedUrl;
    }

    public void setTestedUrl(String testedUrl) {
        this.testedUrl = testedUrl;
    }

    public void setResult(WebifierTesterResult result) {
        this.result = result;
    }

    public WebifierTesterResult getResult() {
        return result;
    }

    public void addTestResult(WebifierTestResultData result) {
        testResults.add(result);
    }

    public List<WebifierTestResultData> getTestResults() {
        return testResults;
    }
}