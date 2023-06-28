package io.github.vssavin.umlib.email;

/**
 * @author vssavin on 13.01.22
 */
public interface EmailService {
    void sendSimpleMessage(String destinationEmail, String subject, String text);
}
