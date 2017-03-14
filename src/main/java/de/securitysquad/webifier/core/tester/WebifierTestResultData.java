package de.securitysquad.webifier.core.tester;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by samuel on 17.02.17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebifierTestResultData {
    @JsonProperty
    private String typ;
    @JsonProperty
    private String message;
    @JsonProperty("tester_id")
    private String launchId;
    @JsonProperty("test_name")
    private String testName;
    @JsonProperty
    private Object result;

    public String getTyp() {
        return typ;
    }

    public String getMessage() {
        return message;
    }

    public String getLaunchId() {
        return launchId;
    }

    public String getTestName() {
        return testName;
    }

    public Object getResult() {
        return result;
    }
}