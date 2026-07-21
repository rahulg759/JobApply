package com.naukri.automation.mail;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.io.File;
import java.util.Properties;


public class MailSender {

    public static void sendMail(String htmlBody, String buildStatus) {

        // 🔥 Jenkins se runtime pe aayega
        String from = System.getenv("SMTP_USER");
        String appPassword = System.getenv("SMTP_PASS");

        if (from == null || appPassword == null) {
            throw new RuntimeException(
                    "SMTP credentials not found. Check Jenkins credentials binding."
            );
        }

        String to = "automationtechiesindia@gmail.com"; // receiver safe hota hai

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(from, appPassword);
                    }
                });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));

            String subject = "Jenkins | Build Status : " + buildStatus;
            message.setSubject(subject);

            Multipart multipart = new MimeMultipart();

            MimeBodyPart bodyPart = new MimeBodyPart();
            bodyPart.setContent(htmlBody, "text/html");
            multipart.addBodyPart(bodyPart);

            File reportZip = new File("target/cucumber-reports/cucumber-html-reports.zip");
            if (reportZip.exists()) {
                MimeBodyPart attachment = new MimeBodyPart();
                attachment.attachFile(reportZip);
                multipart.addBodyPart(attachment);
            }

            message.setContent(multipart);
            Transport.send(message);

            System.out.println("MAIL SENT SUCCESSFULLY");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}