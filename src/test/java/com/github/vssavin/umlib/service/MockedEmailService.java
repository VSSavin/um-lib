package com.github.vssavin.umlib.service;

import com.github.vssavin.umlib.email.EmailService;

import java.util.List;

/**
 * Created by vssavin on 12.07.2022.
 */
public interface MockedEmailService extends EmailService {

    List<EmailMessage> getEmailMessages();
    EmailMessage getLastEmailMessage();

    class EmailMessage {
        private final String destination;
        private final String subject;
        private final String text;

        public EmailMessage(String destination, String subject, String text) {
            this.destination = destination;
            this.subject = subject;
            this.text = text;
        }

        public String getDestination() {
            return destination;
        }

        public String getSubject() {
            return subject;
        }

        public String getText() {
            return text;
        }

        @Override
        public String toString() {
            return "EmailMessage{" +
                    "destination='" + destination + '\'' +
                    ", subject='" + subject + '\'' +
                    ", text='" + text + '\'' +
                    '}';
        }
    }
}
