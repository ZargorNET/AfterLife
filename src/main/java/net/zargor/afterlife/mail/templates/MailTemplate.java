package net.zargor.afterlife.mail.templates;

import lombok.Data;
import net.zargor.afterlife.MimeTypes;
import net.zargor.afterlife.mail.Mail;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.text.StrSubstitutor;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import javax.mail.Address;

@Data
class MailTemplate extends Mail {

    private final String templateName;
    private Map<String, String> values;
    private String language;

    public MailTemplate(String templateName, Map<String, String> values, String language, String subject, Address[] primaryRecipients, Address[] carbonCopyRecipients, Address[] blindCarbonCopyRecipients, String content, MimeTypes contentType) throws UnsupportedEncodingException {
        super(subject, primaryRecipients, carbonCopyRecipients, blindCarbonCopyRecipients, content, contentType);
        this.templateName = templateName;
        this.values = values;
        this.language = language;
    }

    public String renderTemplate() throws IOException {
        InputStream in = this.getClass().getResourceAsStream(String.format("/mail/templates/%s/%s_%s.mailhtml", templateName.toLowerCase(), templateName, language));
        if (in == null)
            in = this.getClass().getResourceAsStream(String.format("/mail/templates/%s/%s_en.mailhtml", templateName.toLowerCase(), templateName));
        if (in == null)
            throw new NullPointerException("Mailtemplate couldn't be found!");
        final String[] s = {""};
        IOUtils.readLines(in, Charset.forName("UTF-8")).forEach(line -> s[0] += line);
        StrSubstitutor sub = new StrSubstitutor(values);
        return sub.replace(s[0]);
    }
}
