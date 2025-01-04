package com.library.librarymanagement;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import javafx.application.Platform;

import java.util.Properties;
import java.util.Random;

public class EmailSender {

    private String sender = "securelibrarysystem@gmail.com"; // custom email sa system
    private String appPassword = "dssp dzjq udxp lpms";// app password sa Gmail

    private String host = "smtp.gmail.com";//host na e access which ang gmail
    private String port = "587";
    private String username = sender;


    //mag send ug OTP
    public void sendOTP(String recipientEmail){

        // Generate a random 6-digit code
        Random random = new Random();
        String verificationCode = String.format("%06d", random.nextInt(1000000));


        /*
        Mao ni sya ang mag configure sa mga properties para mag send ug email gamit ang Java mail API over the SMTP (Simple Mail Transfer Protocol)
        */
        Properties properties = System.getProperties();
        properties.put("mail.smtp.auth", "true");//Enables SMTP authentication. It specifies that the email client must authenticate (provide a username and password) to the SMTP server to send emails.
        properties.put("mail.smtp.starttls.enable", "true");//Enables STARTTLS, which upgrades the connection to a secure (encrypted) connection using TLS/SSL. It ensures that the email is sent over a secure channel.
        properties.put("mail.smtp.host", host);//Specifies the SMTP server host address.
        properties.put("mail.smtp.port", port);//Specifies the SMTP server port.
        properties.put("mail.smtp.ssl.trust", host);//Trusts the SMTP server’s SSL certificate without verifying its validity.

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, appPassword);
            }
        });

        // Send email in a background thread
        new Thread(() -> {
            try {
                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress(sender));
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail));
                message.setSubject("Your Verification Code");


                //Email Body
                String emailBody = "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #ddd; border-radius: 8px; background-color: #f9f9f9;\">"
                        + "<h2 style=\"text-align: center; color: #3871c1; margin-bottom: 20px;\">Email Verification</h2>"
                        + "<p style=\"font-size: 16px; color: #444;\">Dear User,</p>"
                        + "<p style=\"font-size: 16px; color: #444;\">Thank you for signing up with <strong>AirJourney</strong>. To verify your email address, please use the verification code below:</p>"
                        + "<div style=\"text-align: center; margin: 20px 0;\">"
                        + "    <h1 style=\"color: #6a89cc; font-size: 36px; margin: 0;\">" + verificationCode + "</h1>"
                        + "</div>"
                        + "<p style=\"font-size: 16px; color: #444;\">Please enter this code in the app to complete your email verification process.</p>"
                        + "<p style=\"font-size: 16px; color: #444;\">If you didn’t request this, please ignore this email.</p>"
                        + "<p style=\"font-size: 16px; color: #444; margin-top: 20px;\">Thank you for choosing <span style=\"color: #6a89cc; font-weight: bold;\">AirJourney</span>. We’re excited to have you on board!</p>"
                        + "<hr style=\"border: none; border-top: 1px solid #ddd; margin: 20px 0;\">"
                        + "<p style=\"font-size: 14px; color: #888; text-align: center;\">Need help? Contact us at <a href=\"mailto:support@airjourney.com\" style=\"color: #3871c1;\">support@airjourney.com</a></p>"
                        + "<p style=\"font-size: 14px; color: #888; text-align: center;\">&copy; 2025 AirJourney. All rights reserved.</p>"
                        + "</div>";


                message.setContent(emailBody, "text/html");
                Transport.send(message);//mag send sa email
                System.out.println("Verification code successfully sent!");

                // Update UI on success
                Platform.runLater(() -> {

                });

            } catch (MessagingException mex) {
                mex.printStackTrace();
            }
        }).start();
    }

}
