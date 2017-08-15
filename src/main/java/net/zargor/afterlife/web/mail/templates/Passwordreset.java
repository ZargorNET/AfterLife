package net.zargor.afterlife.web.mail.templates;

import java.io.*;
import java.util.*;

public class Passwordreset extends MailTemplate {

    public Passwordreset(String language, String username, String code, String siteurl) {
        super("passwordreset", new HashMap<String, String>() {{
            put("name", username);
            put("code", code);
            put("siteurl", siteurl);
        }}, language);
    }

    public String render() throws IOException {
        return renderTemplate(getName(), getValues(), getLanguage());
    }
}
