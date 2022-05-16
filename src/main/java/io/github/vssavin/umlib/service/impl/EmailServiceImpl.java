package io.github.vssavin.umlib.service.impl;

import io.github.vssavin.umlib.config.EmailConfig;
import io.github.vssavin.umlib.service.EmailService;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * @author vssavin on 13.01.22
 */
@Service
public class EmailServiceImpl implements EmailService {

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
