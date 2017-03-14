package de.securitysquad.webifier.core.mail;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;

/**
 * Created by samuel on 09.03.17.
 */
public class MailData {
    private String subject;
    private String sendTo;


    public static MailData from(Message message) {
        String subject = null;
        try {
            subject = message.getSubject();
        } catch (MessagingException e) {
            // no subject
        }
        String sendTo = null;
        try {
            Address[] reply = message.getReplyTo();
            if (reply.length > 0) {
                sendTo = reply[0].toString();
            } else {
                Address[] from = message.getFrom();
                if (from.length > 0) {
                    sendTo = from[0].toString();
                }
            }
        } catch (MessagingException e) {
            // no sendTo
        }
        return new MailData(subject, sendTo);
    }

    private MailData(String subject, String sendTo) {
        this.subject = subject;
        this.sendTo = sendTo;
    }

    public String getSubject() {
        return subject;
    }

    public String getSendTo() {
        return sendTo;
    }
}