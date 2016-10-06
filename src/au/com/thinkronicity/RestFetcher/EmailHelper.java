package au.com.thinkronicity.RestFetcher;

import au.com.thinkronicity.RestFetcher.Utility;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleemail.model.RawMessage;
import com.amazonaws.services.simpleemail.model.SendRawEmailRequest;
import com.amazonaws.services.simpleemail.model.SendRawEmailResult;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Properties;
import java.util.UUID;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.URLDataSource;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * @author Ian Hogan
 *
 */
public class EmailHelper {
    /**
     * A simple class to send emails via AWS SES.
     * @param credentials		- Credentials for sending email.
     * @param sender			- A SES verified email address to send from. 
     * @param replyTo			- The reply-to address - if null then sender is used.
     * @param attachmentURIs	- A semicolon delimited list of attachment URIs.
     * @param subject			- Email subject.
     * @param body				- Email message
     * @param bodyType			- Type of message content.
     * @param recipients		- Email to addresses.
     * @param awsRegion			- AWS Region for which the sender email is verified.
     * @param traceLog			- True if trace messages should be output.
     * @throws AddressException
     * @throws MessagingException
     * @throws IOException
     */
    public static void sendEmailViaSES(
    		 AWSCredentials credentials
    		,String sender
    		,String replyTo
    		,String attachmentURIs
    		,String subject
    		,String body
    		,String bodyType
    		,String recipients
    		,String recipientsCC
    		,String recipientsBCC
    		,Regions awsRegion
    		,Boolean traceLog
    	) 
    		throws 
    		 AddressException
    		,MessagingException
    		,IOException 
    {
    	// Create a new mail session.
        Session session = Session.getDefaultInstance((Properties)new Properties());
    	// Create a new message for the session.
        MimeMessage message = new MimeMessage(session);
        
        // Set the subject and address details.
        message.setSubject(subject, "UTF-8");
        message.setFrom((Address)new InternetAddress(sender));
        Address[] arraddress = new Address[1];
        arraddress[0] = new InternetAddress(replyTo == null || replyTo.isEmpty() ? sender : replyTo);
        message.setReplyTo(arraddress);
        message.setRecipients(Message.RecipientType.TO, (Address[])InternetAddress.parse((String)recipients));
        if (recipientsCC != null && !recipientsCC.isEmpty()) {
            message.setRecipients(Message.RecipientType.CC, (Address[])InternetAddress.parse((String)recipientsCC));
        }
        if (recipientsBCC != null && !recipientsBCC.isEmpty()) {
            message.setRecipients(Message.RecipientType.BCC, (Address[])InternetAddress.parse((String)recipientsBCC));
        }

        
        // Initialise the email body
        MimeBodyPart wrap = new MimeBodyPart();
        MimeMultipart cover = new MimeMultipart("alternative");
        MimeBodyPart html = new MimeBodyPart();
        cover.addBodyPart((BodyPart)html);
        wrap.setContent((Multipart)cover);
        MimeMultipart content = new MimeMultipart("related");
        message.setContent((Multipart)content);
        content.addBodyPart((BodyPart)wrap);
        
        // Add attachments if they are specified
        if (attachmentURIs != null && !attachmentURIs.isEmpty()) {
            String[] attachmentsFiles = attachmentURIs.split(";");
            StringBuilder sb = new StringBuilder();
            
            for (int a = 0; a < attachmentsFiles.length; a++) {
                String id = UUID.randomUUID().toString();
                sb.append("<img src=\"cid:");
                sb.append(id);
                sb.append("\" alt=\"ATTACHMENT\"/>\n");
                MimeBodyPart attachment = new MimeBodyPart();
                DataSource fds = null;
                
                if ((new File(attachmentsFiles[a])).exists()) {
                	fds = new FileDataSource(attachmentsFiles[a]);
                }
                else {
                	fds = new URLDataSource(new URL(attachmentsFiles[a]));
                }
                
                String attachmentName = fds.getName().replaceFirst("^([^/]*/)*", "").replaceAll("\\?.*$", "");
                attachment.setDataHandler(new DataHandler(fds));
                attachment.setHeader("Content-ID", "<" + id + ">");
                attachment.setHeader("Content-Type", fds.getContentType());
                attachment.setFileName(attachmentName);
                content.addBodyPart((BodyPart)attachment);
            }
        }
        
        // Add the body content.
        html.setContent((Object)body, bodyType);
        
        // Try to send the message.
        try {
            if (traceLog) {
                Utility.LogMessage("Attempting to send an email through Amazon SES by using the AWS SDK for Java...");
            }
            
            AmazonSimpleEmailServiceClient client = new AmazonSimpleEmailServiceClient(credentials);
            Region REGION = Region.getRegion((Regions)awsRegion);
            client.setRegion(REGION);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            message.writeTo((OutputStream)out);
            
            if (traceLog) {
                Utility.LogMessage(out.toString());
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            message.writeTo((OutputStream)outputStream);
            RawMessage rawMessage = new RawMessage(ByteBuffer.wrap(outputStream.toByteArray()));
            SendRawEmailRequest rawEmailRequest = new SendRawEmailRequest(rawMessage);
            SendRawEmailResult result = client.sendRawEmail(rawEmailRequest);
            
            if (traceLog) {
                Utility.LogMessage("Email sent - message id is "+result.getMessageId());
            }
        }
        catch (Exception ex) {
            Utility.LogMessage("Email Failed");
            Utility.LogMessage("Error message: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
