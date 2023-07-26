package com.github.vssavin.umlib.email;

/**
 * Main interface to send simple email message.
 *
 * @author vssavin on 13.01.22
 */
public interface EmailService {
    void sendSimpleMessage(String destinationEmail, String subject, String text);
}
