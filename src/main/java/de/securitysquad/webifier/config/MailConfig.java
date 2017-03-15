package de.securitysquad.webifier.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.mail.ImapMailReceiver;
import org.springframework.integration.mail.MailReceiver;
import org.springframework.integration.mail.MailReceivingMessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.messaging.MessageChannel;

import java.util.Properties;

/**
 * Created by samuel on 07.03.17.
 */
@Configuration
@EnableIntegration
public class MailConfig {
    @Bean
    @InboundChannelAdapter(value = "emailChannel", poller = @Poller(fixedDelay = "10000"))
    public MailReceivingMessageSource mailMessageSource(MailReceiver imapMailReceiver) {
        return new MailReceivingMessageSource(imapMailReceiver);
    }

    @Bean
    public MailReceiver imapMailReceiver(@Value("imaps://${mail.receive.username}:${mail.receive.password}@${mail.receive.host}:${mail.receive.port}/inbox") String url) {
        Properties properties = new Properties();
        properties.setProperty("mail.transport.protocol", "imaps");
        properties.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.setProperty("mail.imap.ssl.enable", "true");
        properties.setProperty("mail.imap.auth", "true");
        properties.setProperty("mail.debug", "false");
        ImapMailReceiver imapMailReceiver = new ImapMailReceiver(url);
        imapMailReceiver.setJavaMailProperties(properties);
        imapMailReceiver.setShouldMarkMessagesAsRead(true);
        imapMailReceiver.setShouldDeleteMessages(false);
        return imapMailReceiver;
    }

    @Bean
    public MessageChannel emailChannel() {
        return new DirectChannel();
    }

    @Bean
    public JavaMailSender mailSender(@Value("${mail.send.host}") String host, @Value("${mail.send.port}") int port, @Value("${mail.send.username}") String username, @Value("${mail.send.password}") String password) {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        Properties properties = new Properties();
        properties.setProperty("mail.transport.protocol", "smtps");
        properties.setProperty("mail.smtp.host", host);
        properties.setProperty("mail.smtp.port", String.valueOf(port));
        properties.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.setProperty("mail.smtp.socketFactory.port", String.valueOf(port));
        properties.setProperty("mail.smtp.ssl.enable", "true");
        properties.setProperty("mail.smtp.auth", "true");
        properties.setProperty("mail.debug", "false");
        properties.setProperty("mail.user", username);
        properties.setProperty("mail.password", password);
        javaMailSender.setJavaMailProperties(properties);
        javaMailSender.setProtocol("smtps");
        javaMailSender.setHost(host);
        javaMailSender.setPort(port);
        javaMailSender.setUsername(username);
        javaMailSender.setPassword(password);
        return javaMailSender;
    }
}