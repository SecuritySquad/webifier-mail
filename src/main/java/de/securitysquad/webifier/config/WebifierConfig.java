package de.securitysquad.webifier.config;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by samuel on 08.11.16.
 */
public class WebifierConfig {
    @JsonProperty("tester")
    private WebifierTesterConfig tester;

    public WebifierTesterConfig getTester() {
        return tester;
    }
}