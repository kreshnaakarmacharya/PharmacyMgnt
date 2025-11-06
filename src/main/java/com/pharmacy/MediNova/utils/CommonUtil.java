package com.pharmacy.MediNova.utils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
@Component
public class CommonUtil {
    @Autowired
    private  JavaMailSender mailSender;

    public Boolean sendMail(String link, String recipientEmail) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("itsmekrishnaa10@gmail.com", "E-Pharmacy Vault");
            helper.setTo(recipientEmail);
            helper.setSubject("Password Reset Request");

            String content = "<p>Hello,</p>"
                    + "<p>You have requested to reset your password.</p>"
                    + "<p>Click the link below to change your password:</p>"
                    + "<p><a href=\"" + link + "\">Change my password</a></p>"
                    + "<br>"
                    + "<p>If you didn’t request this, please ignore this email.</p>"
                    + "<p>Thank you,<br>Your Support Team</p>";

            helper.setText(content, true);

            mailSender.send(message);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String generateUrl(HttpServletRequest request) {
        return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
    }

}
