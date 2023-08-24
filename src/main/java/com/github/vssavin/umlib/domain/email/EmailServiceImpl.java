package com.github.vssavin.umlib.domain.email;

import com.github.vssavin.umlib.config.EmailConfig;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Default implementation of {@link EmailService} interface.
 *
 * @author vssavin on 13.01.22
 */
@Service
class EmailServiceImpl implements EmailService {

    private final JavaMailSender emailSender;
    private final EmailConfig emailConfig;

    public EmailServiceImpl(JavaMailSender emailSender, EmailConfig emailConfig) {
        this.emailSender = emailSender;
        this.emailConfig = emailConfig;
    }

    @Override
    public void sendSimpleMessage(String destinationEmail, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(emailConfig.getUserName());
        message.setTo(destinationEmail);
        message.setSubject(subject);
        message.setText(text);
        emailSender.send(message);
    }
}
