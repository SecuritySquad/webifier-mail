package de.securitysquad.webifier.config;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by samuel on 08.11.16.
 */
public class WebifierTesterConfig {
    @JsonProperty("command")
    private String command;
    @JsonProperty("timeout")
    private int timeout;
    @JsonProperty("parallel")
    private int parallel;

    public String getCommand() {
        return command;
    }

    public int getTimeout() {
        return timeout;
    }

    public int getParallel() {
        return parallel;
    }
}