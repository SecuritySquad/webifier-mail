package de.securitysquad.webifier.core.mail;

import de.securitysquad.webifier.core.tester.WebifierOverallTesterResult;
import de.securitysquad.webifier.core.tester.WebifierTestResultData;
import de.securitysquad.webifier.core.tester.WebifierTesterResult;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.util.ByteArrayDataSource;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static de.securitysquad.webifier.core.tester.WebifierTesterResult.valueOf;
import static java.lang.Thread.currentThread;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * Created by samuel on 08.03.17.
 */
@Component
public class MailSendService {
    private static final List<String> EXCLUDED_TESTS = asList("Screenshot");

    private final JavaMailSender mailSender;
    private final String from;

    @Autowired
    public MailSendService(JavaMailSender mailSender, @Value("${mail.outgoing.from}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    public void sendStartMail(MailData mailData, Map<String, Integer> queue) {
        queue.forEach((s, p) -> System.out.println(s + ": " + p));
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mailSender.createMimeMessage(), true, "utf-8");
            String message = String.join("\n", queue.entrySet().stream().map(entry -> entry.getKey() + ": " + entry.getValue()).collect(toList()));
            String htmlMessage = IOUtils.toString(currentThread().getContextClassLoader().getResourceAsStream("start.html"), "utf-8").replace("_result_", generateStartHtmlList(queue));
            helper.setText(message, htmlMessage);
            helper.setTo(mailData.getSendTo());
            helper.setSubject("EINGEREIHT: " + mailData.getSubject());
            helper.setFrom(from);
            mailSender.send(helper.getMimeMessage());
            System.out.println("START MAIL SEND SUCCESSFULLY TO " + mailData.getSendTo());
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        }
    }

    public void sendResultMail(MailData mailData, Map<String, WebifierOverallTesterResult> urlResults) {
//        urlResults.forEach((s, webifierOverallTesterResult) -> System.out.println(s + " -> " + webifierOverallTesterResult.getResult()));
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mailSender.createMimeMessage(), true, "utf-8");
            String textMessage = String.join("\n", urlResults.entrySet().stream().map(entry -> entry.getKey() + " -> " + getResultText(entry.getValue().getResult())).collect(toList()));
            String htmlMessage = IOUtils.toString(currentThread().getContextClassLoader().getResourceAsStream("result.html"), "utf-8").replace("_result_", generateResultHtmlList(urlResults));
            helper.setText(textMessage, htmlMessage);
            helper.setTo(mailData.getSendTo());
            helper.setSubject(getMailPrefix(urlResults) + mailData.getSubject());
            helper.setFrom(from);
            getScreenshots(urlResults).forEach((name, source) -> {
                if (source != null) {
                    try {
                        helper.addAttachment(name, source);
                    } catch (MessagingException e) {
                        e.printStackTrace();
                    }
                }
            });
            mailSender.send(helper.getMimeMessage());
            System.out.println("RESULT MAIL SEND SUCCESSFULLY TO " + mailData.getSendTo());
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        }
    }

    private Map<String, ByteArrayDataSource> getScreenshots(Map<String, WebifierOverallTesterResult> urlResults) {
        return urlResults.entrySet().stream().collect(toMap(entry -> urlToFileName(entry.getKey()), entry -> resultToScreenshotSource(entry.getValue())));
    }

    private String urlToFileName(String url) {
        try {
            URL u = new URL(url);
            return u.getHost().replace(".", "_") + ".png";
        } catch (MalformedURLException e) {
            return UUID.randomUUID().toString() + ".png";
        }
    }

    private ByteArrayDataSource resultToScreenshotSource(WebifierOverallTesterResult result) {
        Optional<WebifierTestResultData> screenshotResult = result.getTestResults().stream().filter(r -> "Screenshot".equals(r.getTestName())).findFirst();
        if (!screenshotResult.isPresent()) {
            return null;
        }
        Map<String, Object> testResult = (Map<String, Object>) screenshotResult.get().getResult();
        if (!testResult.containsKey("info")) {
            return null;
        }
        Map<String, Object> testResultInfo = (Map<String, Object>) testResult.get("info");
        if (!testResultInfo.containsKey("base64img")) {
            return null;
        }
        String base64Screenshot = (String) testResultInfo.get("base64img");
        String base64Data = base64Screenshot.substring(base64Screenshot.indexOf(",") + 1);
        byte[] screenshot = Base64.getDecoder().decode(base64Data);
        return new ByteArrayDataSource(screenshot, "image/png");
    }

    private String generateStartHtmlList(Map<String, Integer> queue) {
        return "<ul class=\"urls\"><li>" + String.join("</li><li>", queue.entrySet().stream().map(this::generateStartHtmlListEntry).collect(toList())) + "</li></ul>";
    }

    private String generateStartHtmlListEntry(Map.Entry<String, Integer> entry) {
        return "<div class=\"header\"><h2 class=\"url-position\">" + entry.getValue() + "</h2><h2><a href=\"" + entry.getKey() + "\">" + entry.getKey() + "</a></h2></div>";
    }

    private String generateResultHtmlList(Map<String, WebifierOverallTesterResult> urlResults) {
        return "<ul class=\"urls\">" + String.join("", urlResults.entrySet().stream().map(this::generateResultHtmlListEntry).collect(toList())) + "</ul>";
    }

    private String generateResultHtmlListEntry(Map.Entry<String, WebifierOverallTesterResult> entry) {
        WebifierTesterResult type = entry.getValue().getResult();
        return "<li class=\"" + getResultBackgroundClass(type) + "\">" +
                "<div class=\"header\">" +
                "<img class=\"url-state\" alt=\"" + getResultText(type) + "\" src=\"" + getResultImageLink(type) + "\">" +
                "<h2><a href=\"" + entry.getKey() + "\">" + entry.getKey() + "</a></h2></div>" +
                generateResultTestHtmlList(entry.getValue().getTestResults()) +
                "</li>";
    }

    private String generateResultTestHtmlList(List<WebifierTestResultData> testResults) {
        return "<ul class=\"tests\">" + String.join("", testResults.stream().filter(t -> !EXCLUDED_TESTS.contains(t.getTestName())).map(this::generateResultTestHtmlListEntry).collect(toList())) + "</ul>";
    }

    private String generateResultTestHtmlListEntry(WebifierTestResultData result) {
        WebifierTesterResult type = valueOf((String) ((Map<String, Object>) result.getResult()).get("result"));
        return "<li class=\"" + getResultBackgroundClass(type) + "\">" +
                "<img class=\"test-state\" src=\"" + getResultImageLink(type) + "\">" +
                "<h5>" + getTestName(result) + "</h5>" +
                "</li>";
    }

    private String getTestName(WebifierTestResultData resultData) {
        switch (resultData.getTestName()) {
            case "VirusScan":
                return "Virenscan der Webseite";
            case "HeaderInspection":
                return "Vergleich in verschiedenen Browsern";
            case "IpScan":
                return "Überprüfung der IP-Nutzung";
            case "CertificateChecker":
                return "Überprüfung des SSL-Zertifikats";
            case "PortScan":
                return "Überprüfung der Port-Nutzung";
            case "LinkChecker":
                return "Prüfung aller verlinkten Seiten";
            case "PhishingDetector":
                return "Erkennung von Phishing";
            default:
                return resultData.getTestName();
        }
    }

    private String getResultImageLink(WebifierTesterResult result) {
        switch (result) {
            case MALICIOUS:
                return "https://www.webifier.de/img/malicious.png";
            case SUSPICIOUS:
                return "https://www.webifier.de/img/warning.png";
            case CLEAN:
                return "https://www.webifier.de/img/clean.png";
            default:
                return "https://www.webifier.de/img/undefined.png";
        }
    }

    private String getResultBackgroundClass(WebifierTesterResult result) {
        switch (result) {
            case MALICIOUS:
                return "bg-danger";
            case SUSPICIOUS:
                return "bg-warning";
            case CLEAN:
                return "bg-success";
            default:
                return "";
        }
    }

    private String getResultText(WebifierTesterResult result) {
        switch (result) {
            case MALICIOUS:
                return "Bedrohlich";
            case SUSPICIOUS:
                return "Verdächtig";
            case CLEAN:
                return "Unbedenklich";
            default:
                return "Fehlerhaft";
        }
    }

    private String getMailPrefix(Map<String, WebifierOverallTesterResult> urlResults) {
        if (urlResults.values().stream().anyMatch(result -> result.getResult() == WebifierTesterResult.MALICIOUS)) {
            return "BEDROHLICH: ";
        }
        if (urlResults.values().stream().anyMatch(result -> result.getResult() == WebifierTesterResult.SUSPICIOUS)) {
            return "VERDÄCHTIG: ";
        }
        if (urlResults.values().stream().anyMatch(result -> result.getResult() == WebifierTesterResult.CLEAN)) {
            return "UNBEDENKLICH: ";
        }
        return "FEHLERHAFT: ";
    }
}