package net.zargor.afterlife.mail.templates;

import net.zargor.afterlife.MimeTypes;

import java.io.*;
import java.util.*;
import javax.mail.Address;

public class RegisterConfirmationEmail extends MailTemplate {

    public RegisterConfirmationEmail(String language, String username, String code, String siteUrl, String subject, Address[] primaryRecipients, Address[] carbonCopyRecipients, Address[] blindCarbonCopyRecipients) throws UnsupportedEncodingException {
        super("registerconfirmation", new HashMap<String, String>() {{
            put("name", username);
            put("code", code);
            put("siteurl", siteUrl);
        }}, language, subject, primaryRecipients, carbonCopyRecipients, blindCarbonCopyRecipients, "", MimeTypes.HTML);
        try {
            this.setContent(renderTemplate());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
