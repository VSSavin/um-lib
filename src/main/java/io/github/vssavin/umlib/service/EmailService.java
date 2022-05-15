package io.github.vssavin.umlib.service;

/**
 * @author vssavin on 13.01.22
 */
public interface EmailService {
    void sendSimpleMessage(String destinationEmail, String subject, String text);
}
