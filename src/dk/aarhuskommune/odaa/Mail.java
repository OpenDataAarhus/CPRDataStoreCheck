package dk.aarhuskommune.odaa;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Mail {
	
	private String from="";	
	private String host="";
	
	
	
	public Mail(String from, String host) {
		super();
		this.from = from;
		this.host = host;
	} //Mail

	public void sendMail(String to,String subject,String message) {
	      Properties properties = System.getProperties();
	      properties.setProperty("mail.smtp.host", host);
	      Session session = Session.getDefaultInstance(properties);

	      try{
	         // Create a default MimeMessage object.
	         MimeMessage mimeMessage = new MimeMessage(session);

	         // Set From: header field of the header.
	         mimeMessage.setFrom(new InternetAddress(from));

	         
	         String[] aTo=to.split(",");
	         for (int i=0;i<aTo.length;i++) {
	        	 mimeMessage.addRecipient(Message.RecipientType.TO,
	        			 new InternetAddress(aTo[i]));
	         }

	         // Set Subject: header field
	         mimeMessage.setSubject(subject);

	         // Now set the actual message
	         mimeMessage.setText(message);

	         // Send message
	         Transport.send(mimeMessage);	         
	      }catch (MessagingException mex) {
	         mex.printStackTrace();
	      }
	} //sendMail

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}      
}
