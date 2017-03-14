package de.securitysquad.webifier.core.mail;

import de.securitysquad.webifier.core.tester.WebifierOverallTesterResult;
import de.securitysquad.webifier.core.tester.WebifierTestResultData;
import de.securitysquad.webifier.core.tester.WebifierTesterResult;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static java.lang.ClassLoader.getSystemClassLoader;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

/**
 * Created by samuel on 08.03.17.
 */
@Component
public class MailSendService {
    private final JavaMailSender mailSender;
    private final String from;

    @Autowired
    public MailSendService(JavaMailSender mailSender, @Value("${mail.outgoing.from}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    public void sendStartMail(MailData mailData, List<String> urls) {
        // TODO send start mail
    }

    public void sendResultMail(MailData mailData, Map<String, WebifierOverallTesterResult> urlResults) {
        urlResults.forEach((s, webifierOverallTesterResult) -> System.out.println(s + ": " + webifierOverallTesterResult.getResult()));
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mailSender.createMimeMessage(), true, "utf-8");
            String message = String.join("\n", urlResults.entrySet().stream().map(entry -> entry.getKey() + " -> " + getResultText(entry.getValue().getResult())).collect(toList()));
            String htmlMessage = IOUtils.toString(ClassLoader.getSystemResource("mail.html"), "utf-8").replace("_result_", generateHtmlList(urlResults));
            helper.setText(message, htmlMessage);
            helper.setTo(mailData.getSendTo());
            helper.setSubject(getMailPrefix(urlResults) + mailData.getSubject());
            helper.setFrom(from);
            stream(WebifierTesterResult.values()).forEach(r -> {
                try {
                    helper.addInline(r.name(), new ClassPathResource(getResultImage(r), getSystemClassLoader()));
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            });
            mailSender.send(helper.getMimeMessage());
            System.out.println("MAIL SEND SUCCESSFULLY");
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        }
    }

    private String generateHtmlList(Map<String, WebifierOverallTesterResult> urlResults) {
        return "<ul class=\"urls\"><li>" + String.join("</li><li>", urlResults.entrySet().stream().map(this::generateHtmlListEntry).collect(toList())) + "</li></ul>";
    }

    private String generateHtmlListEntry(Map.Entry<String, WebifierOverallTesterResult> entry) {
        return "<div class=\"header\">" +
                "<img class=\"url-state\" alt=\"" + getResultText(entry.getValue().getResult()) + "\" src=\"cid:" + entry.getValue().getResult().name() + "\">" +
                "<h2><a href=\"" + entry.getKey() + "\">" + entry.getKey() + "</a></h2></div>" +
                generateTestHtmlList(entry.getValue().getTestResults());
    }

    private String generateTestHtmlList(List<WebifierTestResultData> testResults) {
        return "<ul class=\"tests\"><li>" + String.join("</li><li>", testResults.stream().map(this::generateTestHtmlListEntry).collect(toList())) + "</li></ul>";
    }

    private String generateTestHtmlListEntry(WebifierTestResultData result) {
        return "<img class=\"test-state\" src=\"cid:" + ((Map<String, Object>) result.getResult()).get("result") + "\">" +
                "<h5>" + getTestName(result) + "</h5>";
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

    private String getResultImage(WebifierTesterResult result) {
        switch (result) {
            case MALICIOUS:
                return "img/error.png";
            case SUSPICIOUS:
                return "img/warning.png";
            case CLEAN:
                return "img/success.png";
            default:
                return "img/undefined.png";
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
