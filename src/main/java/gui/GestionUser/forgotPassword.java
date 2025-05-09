package gui.GestionUser;

import java.io.IOException;
import java.net.URL;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.Properties;

import gui.mainController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import services.userService;
import utils.MyConnection;
import javafx.application.Platform;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class forgotPassword {

    @FXML
    private ResourceBundle resources;
    @FXML
    private URL location;
    @FXML
    private TextField email_input;
    @FXML
    private TextField generatedcode_input;
    @FXML
    private Text generatedcode_text;
    @FXML
    private Button verify_button;
    @FXML
    private TextField newpass_input;
    @FXML
    private TextField retypenewpass_input;
    @FXML
    private Text newpassword_text;
    @FXML
    private Text retypenewpassword_text;
    @FXML
    private Button submitpass_button;
    @FXML
    private Button send_code_button;

    private Connection cnx;
    private long codeGenerationTime;
    private static final long CODE_VALIDITY_DURATION = 10 * 60 * 1000; // 10 minutes in milliseconds

    public forgotPassword() {
        cnx = MyConnection.getInstance().getConnection();
    }

    userService userService = new userService();
    private static String generatedToken;

    @FXML
    void send_email(ActionEvent event) throws SQLException {
        String email = email_input.getText().trim();
        if (email.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please enter your email address.");
            return;
        }

        if(userService.isEmailTaken(email)) {
            // Disable the button to prevent multiple clicks
            send_code_button = (Button) event.getSource();
            send_code_button.setDisable(true);
            send_code_button.setText("Sending...");

            // Generate code and store it
            generatedToken = generateRandomCode();
            codeGenerationTime = System.currentTimeMillis();

            // Run email sending in background thread
            new Thread(() -> {
                boolean emailSent = sendVerificationEmailWithJavaMail(email, generatedToken);

                // Update UI on JavaFX thread
                Platform.runLater(() -> {
                    send_code_button.setDisable(false);
                    send_code_button.setText("Resend code");

                    if (emailSent) {
                        // Show verification code fields
                        verify_button.setVisible(true);
                        generatedcode_input.setVisible(true);
                        generatedcode_text.setVisible(true);

                        showAlert(Alert.AlertType.INFORMATION, "Success",
                                "Verification code sent to " + email + ".\n\n" +
                                        "Please check your inbox. If you don't see the email, check your spam folder and mark it as 'Not Spam'.");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error",
                                "Failed to send email. Please check your internet connection or try again later.");
                    }
                });
            }).start();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Email not registered in our system.");
        }
    }

    private boolean sendVerificationEmailWithJavaMail(String toEmail, String verificationCode) {
        // Email configuration for better deliverability
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");

        // DKIM and SPF are handled by the email provider

        // Your email credentials - replace with your actual email and app password
        final String username = "az.backup04@gmail.com"; // Replace with your Gmail
        final String password = "pfgx wzej kilc azga"; // Replace with your app password
        final String senderName = "EVENTURA"; // Replace with your app name

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            // Create email message
            Message message = new MimeMessage(session);

            // Set proper From header with name (better deliverability)
            message.setFrom(new InternetAddress(username, senderName));

            // Set proper Reply-To header (better deliverability)
            message.setReplyTo(new InternetAddress[] { new InternetAddress(username, senderName) });

            // Set recipient
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));

            // Set email subject with recognizable app name
            message.setSubject("[EVENTURA] Password Reset Verification Code");

            // Add recipient's email address to the subject for personalization
            // This helps with deliverability as it appears more personalized
            String recipientName = toEmail.split("@")[0];

            // Format current datetime
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentDateTime = sdf.format(new Date());

            // Create HTML content for better email formatting
            // Using table-based HTML that renders well in all email clients
            String htmlContent =
                    "<!DOCTYPE html>" +
                            "<html><head>" +
                            "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">" +
                            "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                            "<title>Password Reset Verification</title>" +
                            "</head><body style='font-family: Arial, sans-serif; margin: 0; padding: 0; color: #333333;'>" +
                            "<table role='presentation' width='100%' style='width: 100%; border-collapse: collapse;'>" +
                            "<tr><td align='center' style='padding: 20px 0;'>" +
                            "<table role='presentation' style='max-width: 600px; border-collapse: collapse; border: 1px solid #dddddd; border-radius: 3px; background-color: #ffffff;'>" +
                            // Header
                            "<tr><td style='padding: 20px; text-align: center; background-color: #1a73e8; color: white;'>" +
                            "<h1 style='margin: 0; font-size: 24px;'>Password Reset</h1>" +
                            "</td></tr>" +
                            // Body
                            "<tr><td style='padding: 20px;'>" +
                            "<p>Hello " + recipientName + ",</p>" +
                            "<p>We received a request to reset your password. To continue with the password reset process, please use the verification code below:</p>" +
                            "<div style='text-align: center; margin: 30px 0;'>" +
                            "<div style='display: inline-block; padding: 15px 25px; background-color: #f2f2f2; border-radius: 5px; " +
                            "font-size: 24px; letter-spacing: 5px; font-weight: bold;'>" + verificationCode + "</div>" +
                            "</div>" +
                            "<p>This code will expire in 10 minutes for security purposes.</p>" +
                            "<p><strong>Request Date and Time:</strong> " + currentDateTime + "</p>" +
                            "<p>If you didn't request a password reset, please ignore this email or contact support if you have concerns.</p>" +
                            "</td></tr>" +
                            // Footer
                            "<tr><td style='padding: 20px; text-align: center; background-color: #f7f7f7; color: #666666; font-size: 13px;'>" +
                            "<p style='margin: 0 0 10px 0;'>© " + Calendar.getInstance().get(Calendar.YEAR) + " EVENTURA. All rights reserved.</p>" +
                            "<p style='margin: 0;'>This is an automated message. Please do not reply.</p>" +
                            "</td></tr>" +
                            "</table>" +
                            "</td></tr>" +
                            "</table>" +
                            "</body></html>";

            // Create a plain text alternative (important for spam filtering)
            String plainText = "Hello " + recipientName + ",\n\n" +
                    "We received a request to reset your password. To continue with the password reset process, " +
                    "please use the verification code below:\n\n" +
                    verificationCode + "\n\n" +
                    "This code will expire in 10 minutes for security purposes.\n\n" +
                    "Request Date and Time: " + currentDateTime + "\n\n" +
                    "If you didn't request a password reset, please ignore this email or contact support if you have concerns.\n\n" +
                    "© " + Calendar.getInstance().get(Calendar.YEAR) + " EVENTURA. All rights reserved.\n" +
                    "This is an automated message. Please do not reply.";

            // Create multipart message with both HTML and plain text (better for deliverability)
            Multipart multipart = new javax.mail.internet.MimeMultipart("alternative");

            // Add plain text part
            BodyPart textPart = new javax.mail.internet.MimeBodyPart();
            textPart.setText(plainText);
            multipart.addBodyPart(textPart);

            // Add HTML part
            BodyPart htmlPart = new javax.mail.internet.MimeBodyPart();
            htmlPart.setContent(htmlContent, "text/html; charset=utf-8");
            multipart.addBodyPart(htmlPart);

            // Set email content
            message.setContent(multipart);

            // Set crucial headers for better deliverability
            message.setHeader("X-Priority", "1"); // High priority
            message.setHeader("X-MSMail-Priority", "High");
            message.setHeader("Importance", "High");
            message.setSentDate(new Date());

            // Send message
            Transport.send(message);
            System.out.println("Verification email sent successfully to " + toEmail);
            return true;

        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            e.printStackTrace();
            System.out.println("Failed to send email: " + e.getMessage());
            return false;
        }
    }

    @FXML
    void initialize() {
        List<Node> components = Arrays.asList(
                generatedcode_input, newpass_input, retypenewpass_input,
                generatedcode_text, newpassword_text, retypenewpassword_text,
                verify_button, submitpass_button
        );
        components.forEach(component -> component.setVisible(false));
    }

    @FXML
    void verify_button(ActionEvent event) {
        String inputCode = generatedcode_input.getText().trim();
        if (inputCode.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please enter the verification code.");
            return;
        }

        // Check if code has expired (10 minutes)
        if (System.currentTimeMillis() - codeGenerationTime > CODE_VALIDITY_DURATION) {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Verification code has expired. Please request a new code.");
            return;
        }

        if (inputCode.equals(generatedToken)) {
            List<Node> components = Arrays.asList(
                    newpass_input, retypenewpass_input,
                    newpassword_text, retypenewpassword_text,
                    submitpass_button
            );
            components.forEach(component -> component.setVisible(true));
            showAlert(Alert.AlertType.INFORMATION, "Success", "Code verified successfully. Please enter your new password.");
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid verification code. Please try again.");
        }
    }

    @FXML
    void submitpass_button(ActionEvent event) {
        String newpassword = newpass_input.getText();
        String retype_newpassword = retypenewpass_input.getText();

        if (newpassword.isEmpty() || retype_newpassword.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Password fields cannot be empty.");
            return;
        }

        if (!isValidPassword(newpassword)) {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Password must contain at least one uppercase letter, one number, and one special character, and be at least 8 characters long.");
            return;
        }

        if (newpassword.equals(retype_newpassword)) {
            try {
                userService.updateUserPasswordByEmail(email_input.getText(), newpassword);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Password updated successfully! You can now log in with your new password.");
                navigateTo("/login.fxml", event);
            } catch (SQLException | IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update password: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Passwords do not match. Please try again.");
        }
    }

    private void navigateTo(String fxmlPath, ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        // Get the current stage using the event source
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
    // Password validation method
    private boolean isValidPassword(String password) {
        String regex = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!/])(?=\\S+$).{8,}$";
        return password.matches(regex);
    }

    public static String generateRandomCode() {
        // Create a Random object with improved randomness
        Random random = new SecureRandom();

        // Generate a random 6-digit code for better security
        int code = 100000 + random.nextInt(900000);

        // Convert the code to a string
        return String.valueOf(code);
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}