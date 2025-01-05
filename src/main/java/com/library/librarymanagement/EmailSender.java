package com.library.librarymanagement;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import javafx.application.Platform;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Properties;
import java.util.Random;

public class    EmailSender {

    private final String senderEmail = "securelibrarysystem@gmail.com"; // System's email
    private final String appPassword = "dssp dzjq udxp lpms"; // Gmail App Password
    private final String smtpHost = "smtp.gmail.com"; // Gmail SMTP Host
    private final String smtpPort = "587"; // TLS Port for Gmail

    static String DB_URL = "jdbc:mysql://localhost/securelibrary";
    static String USER = "root";
    static String PASS = "";

    private String recipientEmail;

    public void sentWelcomeMessage(String email, EmailCallback callback) {
        // Configure email properties for sending via SMTP
        Properties properties = configureSMTPProperties();

        // Initialize email session with authentication
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, appPassword);
            }
        });

        // Send email in a background thread to avoid blocking the UI
        new Thread(() -> {
            try {
                MimeMessage message = createEmailMessage(session, email);
                Transport.send(message);
                System.out.println("Welcome Email sent to " + email);

                // Notify the success callback
                if (callback != null) {
                    callback.onSuccess();
                }
            } catch (MessagingException e) {
                //e.printStackTrace();

                // Notify the failure callback
                if (callback != null) {
                    callback.onFailure(e);
                }
            }
        }).start();
    }

    public void notifyStudentAboutFine(String email, String dueDate, String amount, String bookTitle){
        // Configure email properties for sending via SMTP
        Properties properties = configureSMTPProperties();

        // Initialize email session with authentication
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, appPassword);
            }
        });

        // Send email in a background thread to avoid blocking the UI
        new Thread(() -> {
            try {
                MimeMessage message = createNotification(session,email,amount,bookTitle,dueDate);
                Transport.send(message);
                System.out.println("Verification code successfully sent to " + email);

                // Optional: Update UI on success
                Platform.runLater(() -> {
                    // Add UI update logic if necessary
                });

            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }).start();



    }

    public EmailSender(){

    }
    public EmailSender(String recipientEmail) {
        sendOTP(recipientEmail);
    }

    /**
     * Generates a 6-digit OTP and sends it to the recipient's email.
     *
     * @param recipientEmail Email address of the recipient.
     */
    private void sendOTP(String recipientEmail) {
        // Generate a random 6-digit verification code
        String verificationCode = generateVerificationCode();

        System.out.println(verificationCode);
        //add the verificationCode to database
        addVerificationCodeToDatabase(verificationCode,recipientEmail);

        // Configure email properties for sending via SMTP
        Properties properties = configureSMTPProperties();

        // Initialize email session with authentication
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, appPassword);
            }
        });

        // Send email in a background thread to avoid blocking the UI
        new Thread(() -> {
            try {
                MimeMessage message = createEmailMessage(session, recipientEmail, verificationCode);
                Transport.send(message);
                System.out.println("Verification code successfully sent to " + recipientEmail);

                // Optional: Update UI on success
                Platform.runLater(() -> {
                    // Add UI update logic if necessary
                });

            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void addVerificationCodeToDatabase(String code, String email){
        try(Connection connection = DriverManager.getConnection(DB_URL, USER,PASS);
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO confirmation VALUES(?,?)")){
            preparedStatement.setString(1, email);
            preparedStatement.setString(2,code);
            preparedStatement.executeUpdate();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Generates a 6-digit random verification code.
     *
     * @return A 6-digit OTP as a String.
     */
    private String generateVerificationCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }

    /**
     * Configures SMTP properties required for sending email via Gmail.
     *
     * @return Configured SMTP properties.
     */
    private Properties configureSMTPProperties() {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", smtpHost);
        properties.put("mail.smtp.port", smtpPort);
        properties.put("mail.smtp.ssl.trust", smtpHost);
        return properties;
    }

    /**
     * Creates the email message with the verification code.
     *
     * @param session           Email session for SMTP communication.
     * @param recipientEmail    Recipient's email address.
     * @param verificationCode  OTP to include in the email body.
     * @return Configured MimeMessage.
     * @throws MessagingException If message creation fails.
     */
    private MimeMessage createEmailMessage(Session session, String recipientEmail, String verificationCode) throws MessagingException {
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(senderEmail));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail));
        message.setSubject("Your Verification Code");

        // HTML body of the email
        String emailBody = "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #ddd; border-radius: 8px; background-color: #f9f9f9;\">"
                + "<h2 style=\"text-align: center; color: #3871c1; margin-bottom: 20px;\">Admin Verification Code</h2>"
                + "<p style=\"font-size: 16px; color: #444;\">Dear Admin,</p>"
                + "<p style=\"font-size: 16px; color: #444;\">Thank you for managing the <strong>SecureLibrarySystem</strong>. To verify your admin access, please use the verification code below:</p>"
                + "<div style=\"text-align: center; margin: 20px 0;\">"
                + "<h1 style=\"color: #6a89cc; font-size: 36px; margin: 0;\">" + verificationCode + "</h1>"
                + "</div>"
                + "<p style=\"font-size: 16px; color: #444;\">Please enter this code in the admin panel to complete your verification process.</p>"
                + "<p style=\"font-size: 16px; color: #444;\">If you didn’t request this, please ignore this email.</p>"
                + "<p style=\"font-size: 16px; color: #444; margin-top: 20px;\">Thank you for managing the <span style=\"color: #6a89cc; font-weight: bold;\">SecureLibrarySystem</span>. We appreciate your hard work and dedication!</p>"
                + "<hr style=\"border: none; border-top: 1px solid #ddd; margin: 20px 0;\">"
                + "<p style=\"font-size: 14px; color: #888; text-align: center;\">&copy; 2025 SecureLibrarySystem. All rights reserved.</p>"
                + "</div>";

        message.setContent(emailBody, "text/html");
        return message;
    }
    private MimeMessage createEmailMessage(Session session, String recipientEmail) throws MessagingException {
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(senderEmail));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail));
        message.setSubject("Welcome to Secure Library System");

        // HTML body of the email
        String emailBody = "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #ddd; border-radius: 8px; background-color: #f9f9f9;\">"
                + "<h2 style=\"text-align: center; color: #3871c1; margin-bottom: 20px;\">Welcome to Secure Library System</h2>"
                + "<p style=\"font-size: 16px; color: #444;\">Dear [Admin/User],</p>"
                + "<p style=\"font-size: 16px; color: #444;\">We are thrilled to have you as part of the <strong>Secure Library System</strong> community! Our goal is to make managing and accessing library resources as seamless and secure as possible.</p>"
                + "<p style=\"font-size: 16px; color: #444;\">Here are a few things you can do with our system:</p>"
                + "<ul style=\"font-size: 16px; color: #444;\">"
                + "<li>Borrow and manage books with ease</li>"
                + "<li>Track your borrowing history</li>"
                + "<li>Stay updated with library notifications</li>"
                + "</ul>"
                + "<p style=\"font-size: 14px; color: #888; text-align: center;\">&copy; 2025 SecureLibrarySystem. All rights reserved.</p>"
                + "</div>";


        message.setContent(emailBody, "text/html");
        return message;
    }
    private MimeMessage createNotification(Session session, String recipientEmail, String fineAmount, String bookTitle, String dueDate) throws MessagingException {
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(senderEmail));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail));
        message.setSubject("Overdue Book Fine Notification");

        // HTML body of the email
        String emailBody = "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #ddd; border-radius: 8px; background-color: #f9f9f9;\">"
                + "<h2 style=\"text-align: center; color: #e74c3c; margin-bottom: 20px;\">Overdue Book Fine Notification</h2>"
                + "<p style=\"font-size: 16px; color: #444;\">Dear Student,</p>"
                + "<p style=\"font-size: 16px; color: #444;\">We hope this message finds you well. This is a reminder that you have an overdue book from our library.</p>"
                + "<p style=\"font-size: 16px; color: #444;\"><strong>Book Title:</strong> " + bookTitle + "</p>"
                + "<p style=\"font-size: 16px; color: #444;\"><strong>Due Date:</strong> " + dueDate + "</p>"
                + "<p style=\"font-size: 16px; color: #e74c3c;\"><strong>Fine Amount:</strong> $" + fineAmount + "</p>"
                + "<p style=\"font-size: 16px; color: #444;\">Please return the book as soon as possible to avoid additional charges. If you have already returned the book, kindly ignore this message.</p>"
                + "<p style=\"font-size: 16px; color: #444;\">Thank you for your cooperation.</p>"
                + "<p style=\"font-size: 14px; color: #888; text-align: center;\">&copy; 2025 SecureLibrarySystem. All rights reserved.</p>"
                + "</div>";

        message.setContent(emailBody, "text/html");
        return message;
    }

}
