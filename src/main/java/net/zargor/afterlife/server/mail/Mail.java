package net.zargor.afterlife.server.mail;

import java.io.UnsupportedEncodingException;
import javax.mail.Address;
import javax.mail.internet.InternetAddress;
import lombok.Data;
import net.zargor.afterlife.server.MimeTypes;
import net.zargor.afterlife.server.WebServer;

@Data
public class Mail {

	private String subject;
	private final Address from;
	private Address[] primaryRecipients;
	private Address[] carbonCopyRecipients;
	private Address[] blindCarbonCopyRecipients;
	private String content;
	private MimeTypes contentType;

	public Mail(String subject, Address[] primaryRecipients, Address[] carbonCopyRecipients, Address[] blindCarbonCopyRecipients, String content, MimeTypes contentType) throws UnsupportedEncodingException {
		this.subject = subject;
		this.from = new InternetAddress(WebServer.getInstance().getConfig().getValue("mail_from"), WebServer.getInstance().getConfig().getValue("mail_name"));
		this.primaryRecipients = primaryRecipients;
		this.carbonCopyRecipients = carbonCopyRecipients;
		this.blindCarbonCopyRecipients = blindCarbonCopyRecipients;
		this.content = content;
		this.contentType = contentType;
	}
}
