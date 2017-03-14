package de.securitysquad.webifier.core.mail;

import de.securitysquad.webifier.core.tester.WebifierTesterLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Component;

import javax.mail.Message;
import javax.mail.MessagingException;
import java.io.IOException;
import java.util.List;

@Component
@MessageEndpoint
public class MailReceiverService {
    private final MailSendService sendService;
    private final WebifierTesterLauncher launcher;

    @Autowired
    public MailReceiverService(MailSendService sendService, WebifierTesterLauncher launcher) {
        this.sendService = sendService;
        this.launcher = launcher;
    }

    @ServiceActivator(inputChannel = "emailChannel")
    public void handleMessage(Message message) throws MessagingException, IOException {
        UrlsFromMessageExtractor extractor = new UrlsFromMessageExtractor();
        List<String> urls = extractor.extractUrls(message);
        MailRequest request = new MailRequest(MailData.from(message), sendService);
        urls.forEach(url -> launcher.launch(url, request));
    }
}