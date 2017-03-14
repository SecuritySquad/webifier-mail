package de.securitysquad.webifier.core.mail;

import de.securitysquad.webifier.core.tester.WebifierOverallTesterResult;
import de.securitysquad.webifier.core.tester.WebifierTesterResultListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by samuel on 08.03.17.
 */
public class MailRequest implements WebifierTesterResultListener {
    private MailData mailData;
    private final MailSendService sendService;
    private final List<UrlWrapper> urls;
    private long startTimestamp;

    public MailRequest(MailData mailData, MailSendService sendService) {
        this.mailData = mailData;
        this.sendService = sendService;
        urls = new ArrayList<>();
        startTimestamp = System.currentTimeMillis();
    }

    @Override
    public void onStarted(String id, String url) {
        if (urls.isEmpty()) {
            long executionTimestamp = System.currentTimeMillis();
            if (executionTimestamp - startTimestamp > 5000) {
                sendService.sendStartMail(mailData, urls.stream().map(UrlWrapper::getUrl).collect(Collectors.toList()));
            }
        }
        urls.add(new UrlWrapper(url, id));
    }

    @Override
    public void onFinished(String id, WebifierOverallTesterResult result) {
        System.out.println("Finished");
        urls.stream().filter(wrapper -> wrapper.getTesterId().equals(id)).findAny().ifPresent(wrapper -> wrapper.setResult(result));
        if (urls.stream().allMatch(wrapper -> wrapper.getResult() != null)) {
            Map<String, WebifierOverallTesterResult> urlResults = new HashMap<>();
            urls.forEach(wrapper -> urlResults.put(wrapper.getUrl(), wrapper.getResult()));
            sendService.sendResultMail(mailData, urlResults);
        }
    }

    private class UrlWrapper {
        private String url;
        private String testerId;
        private WebifierOverallTesterResult result;

        UrlWrapper(String url, String testerId) {
            this.url = url;
            this.testerId = testerId;
        }

        String getUrl() {
            return url;
        }

        String getTesterId() {
            return testerId;
        }

        WebifierOverallTesterResult getResult() {
            return result;
        }

        void setResult(WebifierOverallTesterResult result) {
            this.result = result;
        }
    }
}