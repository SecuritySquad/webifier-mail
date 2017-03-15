package de.securitysquad.webifier.core.mail;

import de.securitysquad.webifier.core.tester.WebifierOverallTesterResult;
import de.securitysquad.webifier.core.tester.WebifierTesterResultListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * Created by samuel on 08.03.17.
 */
public class MailRequest implements WebifierTesterResultListener {
    private MailData mailData;
    private final MailSendService sendService;
    private final List<UrlWrapper> urls;

    public MailRequest(MailData mailData, List<String> urls, MailSendService sendService) {
        this.mailData = mailData;
        this.sendService = sendService;
        this.urls = urls.stream().map(UrlWrapper::new).collect(toList());
    }

    @Override
    public void onQueued(String id, String url, int waitingPosition) {
        urls.stream().filter(w -> w.getUrl().equals(url)).findFirst().ifPresent(w -> {
            w.setTesterId(id);
            w.setWaitingPosition(waitingPosition);
        });
        if (urls.stream().allMatch(w -> w.getTesterId() != null)) {
            sendService.sendStartMail(mailData, urls.stream().collect(toMap(UrlWrapper::getUrl, UrlWrapper::getWaitingPosition)));
        }
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
        private int waitingPosition;
        private String testerId;
        private WebifierOverallTesterResult result;

        UrlWrapper(String url) {
            this.url = url;
        }

        String getUrl() {
            return url;
        }

        public int getWaitingPosition() {
            return waitingPosition;
        }

        public void setWaitingPosition(int waitingPosition) {
            this.waitingPosition = waitingPosition;
        }

        public void setTesterId(String testerId) {
            this.testerId = testerId;
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