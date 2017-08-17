package net.zargor.afterlife.mail;

import com.sun.mail.util.MailSSLSocketFactory;
import net.zargor.afterlife.WebServer;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.*;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

public class MailManagement {

    private final Properties properties;
    private Session session;
    private boolean authentication_required;

    private String username = WebServer.getInstance().getConfig().getValue("smtp_username");
    private String password = WebServer.getInstance().getConfig().getValue("smtp_password");

    public MailManagement() {
        authentication_required = WebServer.getInstance().getConfig().getValue("smtp_authentication_required");
        String host = WebServer.getInstance().getConfig().getValue("smtp_host");
        String port = WebServer.getInstance().getConfig().getValue("smtp_port").toString();
        String secure = WebServer.getInstance().getConfig().getValue("smtp_secure").toString();

        properties = new Properties();

        properties.setProperty("mail.transport.protocol", "smtp");
        properties.put("mail.smtp.starttls.enable", secure);
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.auth", authentication_required);
        if (Boolean.parseBoolean(secure)) {
            try {
                MailSSLSocketFactory sf = new MailSSLSocketFactory();
                sf.setTrustAllHosts(true);
                properties.put("mail.smtp.ssl.trust", "*");
                properties.put("mail.smtp.ssl.socketFactory", sf);
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
            }
            properties.put("mail.smtp.socketFactory.port", port);
            properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            properties.put("mail.smtp.socketFactory.fallback", "false");
        }
        session = Session.getDefaultInstance(properties);
    }

    public void sendMail(Mail mail) throws MessagingException, UnsupportedEncodingException {
        Transport transport = session.getTransport();

        MimeMessage message = new MimeMessage(session);
        message.setSubject(mail.getSubject());
        message.setFrom(mail.getFrom());

        if (mail.getPrimaryRecipients() == null || mail.getPrimaryRecipients().length == 0)
            throw new NullPointerException("Needed at least one primary mail");
        message.addRecipients(Message.RecipientType.TO, mail.getPrimaryRecipients());

        if (mail.getCarbonCopyRecipients() != null && mail.getCarbonCopyRecipients().length != 0)
            message.addRecipients(Message.RecipientType.CC, mail.getCarbonCopyRecipients());

        if (mail.getBlindCarbonCopyRecipients() != null && mail.getBlindCarbonCopyRecipients().length != 0)
            message.addRecipients(Message.RecipientType.BCC, mail.getBlindCarbonCopyRecipients());

        message.setContent(mail.getContent(), mail.getContentType().getMimeText());
        message.saveChanges();
        if (authentication_required)
            transport.connect(username, password);
        else
            transport.connect();
        transport.sendMessage(message, message.getAllRecipients());
        transport.close();
    }
}
