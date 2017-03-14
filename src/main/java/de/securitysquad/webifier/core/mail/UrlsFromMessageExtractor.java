package de.securitysquad.webifier.core.mail;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

/**
 * Created by samuel on 08.03.17.
 */
public class UrlsFromMessageExtractor {


    public List<String> extractUrls(Message message) throws IOException, MessagingException {
        List<String> content = getContent(message);
        return getUrls(content);
    }

    private List<String> getUrls(List<String> content) {
        List<String> urls = new ArrayList<>();
        content.forEach(c -> urls.addAll(getUrls(c)));
        return urls.stream().distinct().collect(Collectors.toList());
    }

    private List<String> getUrls(String content) {
        List<String> urls = new ArrayList<>();
        Pattern pattern = Pattern.compile("https?://([\\w+?.\\w+])+([a-zA-Z0-9~!@#$%\\^&amp;*()_\\-=+\\\\/?.:;',]*)?");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            String url = matcher.group();
            if (!urls.contains(url))
                urls.add(url);
        }
        return urls;
    }

    private List<String> getContent(Part message) throws MessagingException, IOException {
        if (message.isMimeType("multipart/*")) {
            List<String> list = new ArrayList<>();
            Multipart content = (Multipart) message.getContent();
            for (int i = 0; i < content.getCount(); i++) {
                list.addAll(getContent(content.getBodyPart(i)));
            }
            return list;
        }
        if (message.isMimeType("text/plain;") || message.isMimeType("text/html;")) {
            return singletonList((String) message.getContent());
        }
        return emptyList();
    }
}
