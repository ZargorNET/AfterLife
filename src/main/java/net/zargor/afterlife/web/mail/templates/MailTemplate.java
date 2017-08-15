package net.zargor.afterlife.web.mail.templates;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.text.StrSubstitutor;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

@Data
@AllArgsConstructor
class MailTemplate {

    private final String name;
    private Map<String, String> values;
    private String language;

    String renderTemplate(String name, Map<String, String> values, String language) throws IOException {
        InputStream in = this.getClass().getResourceAsStream(String.format("/mail/templates/%s/%s_%s.mailhtml", name.toLowerCase(), name, language));
        if (in == null)
            in = this.getClass().getResourceAsStream(String.format("/mail/templates/%s/%s_en.mailhtml", name.toLowerCase(), name));
        if (in == null)
            throw new NullPointerException("Mailtemplate couldn't be found!");
        final String[] s = {""};
        IOUtils.readLines(in, Charset.forName("UTF-8")).forEach(line -> s[0] += line);
        StrSubstitutor sub = new StrSubstitutor(values);
        return sub.replace(s[0]);
    }
}
