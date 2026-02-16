package com.staffalteration.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * SMS Notification Service
 * Currently configured for Twilio integration (can be replaced with AWS SNS or other providers)
 */
@Service
@Slf4j
public class SMSService {

    @Value("${sms.provider:twilio}")
    private String provider;

    @Value("${sms.enabled:false}")
    private boolean smsEnabled;

    @Value("${twilio.account-sid:}")
    private String twilioAccountSid;

    @Value("${twilio.auth-token:}")
    private String twilioAuthToken;

    @Value("${twilio.phone-number:}")
    private String twilioPhoneNumber;

    /**
     * Send SMS notification for alteration creation
     */
    public void notifyAlterationCreated(String recipientPhone, String originalStaffName, 
                                        String classCode, String subjectName, String alterationDate) {
        if (!smsEnabled) {
            log.debug("SMS notifications are disabled");
            return;
        }

        try {
            String message = String.format(
                "Alteration Alert: Staff %s is absent on %s. Class: %s (%s). Substitute assigned. Contact HOD for details.",
                originalStaffName, alterationDate, classCode, subjectName
            );
            
            sendSMS(recipientPhone, message);
            log.info("Alteration creation SMS sent to {}", recipientPhone);
        } catch (Exception e) {
            log.error("Failed to send alteration creation SMS to {}: {}", recipientPhone, e.getMessage());
        }
    }

    /**
     * Send SMS notification for substitute assignment
     */
    public void notifySubstituteAssigned(String substitutePhone, String originalStaffName,
                                        String classCode, String alterationDate) {
        if (!smsEnabled) {
            log.debug("SMS notifications are disabled");
            return;
        }

        try {
            String message = String.format(
                "Class Assignment: You have been assigned to substitute %s's class %s on %s. Please acknowledge in the system.",
                originalStaffName, classCode, alterationDate
            );
            
            sendSMS(substitutePhone, message);
            log.info("Substitute assignment SMS sent to {}", substitutePhone);
        } catch (Exception e) {
            log.error("Failed to send substitute assignment SMS to {}: {}", substitutePhone, e.getMessage());
        }
    }

    /**
     * Send SMS notification for alteration rejection
     */
    public void notifyAlterationRejected(String recipientPhone, String classCode,
                                        String alterationDate, String newSubstituteName) {
        if (!smsEnabled) {
            log.debug("SMS notifications are disabled");
            return;
        }

        try {
            String message = String.format(
                "Alteration Update: Substitute for class %s on %s has been rejected. New substitute: %s",
                classCode, alterationDate, newSubstituteName
            );
            
            sendSMS(recipientPhone, message);
            log.info("Alteration rejection SMS sent to {}", recipientPhone);
        } catch (Exception e) {
            log.error("Failed to send alteration rejection SMS to {}: {}", recipientPhone, e.getMessage());
        }
    }

    /**
     * Send SMS notification to HOD about inability to alter class
     */
    public void notifyHodUnableToAlter(String hodPhone, String classCode, String alterationDate) {
        if (!smsEnabled) {
            log.debug("SMS notifications are disabled");
            return;
        }

        try {
            String message = String.format(
                "Alert: Unable to find substitute for class %s on %s. Please manually assign or take action.",
                classCode, alterationDate
            );
            
            sendSMS(hodPhone, message);
            log.info("HOD alert SMS sent to {}", hodPhone);
        } catch (Exception e) {
            log.error("Failed to send HOD alert SMS to {}: {}", hodPhone, e.getMessage());
        }
    }

    /**
     * Core SMS sending method
     */
    private void sendSMS(String phoneNumber, String message) {
        if (!smsEnabled) {
            log.debug("SMS disabled. Would send to {}: {}", phoneNumber, message);
            return;
        }

        if (phoneNumber == null || phoneNumber.isEmpty()) {
            log.warn("Phone number is empty. Cannot send SMS.");
            return;
        }

        try {
            if ("twilio".equalsIgnoreCase(provider)) {
                sendViaTwilio(phoneNumber, message);
            } else if ("aws-sns".equalsIgnoreCase(provider)) {
                sendViaAwsSNS(phoneNumber, message);
            } else {
                log.warn("Unknown SMS provider: {}. SMS not sent.", provider);
            }
        } catch (Exception e) {
            log.error("Error sending SMS to {}: {}", phoneNumber, e.getMessage(), e);
        }
    }

    /**
     * Send SMS via Twilio
     * Note: Requires twilio-java library in build.gradle
     * Currently logs as placeholder - uncomment when Twilio is configured
     */
    private void sendViaTwilio(String phoneNumber, String message) {
        // For production, uncomment and configure Twilio:
        /*
        try {
            Twilio.init(twilioAccountSid, twilioAuthToken);
            Message messageSent = Message.creator(
                new PhoneNumber(twilioPhoneNumber),  // From number
                new PhoneNumber(phoneNumber)          // To number
            ).setBody(message).create();

            log.info("SMS sent via Twilio: {}", messageSent.getSid());
        } catch (Exception e) {
            log.error("Twilio SMS failed: {}", e.getMessage());
            throw new RuntimeException("SMS delivery failed", e);
        }
        */
        log.info("[TWILIO SMS] To: {} | Message: {}", phoneNumber, message);
    }

    /**
     * Send SMS via AWS SNS
     * Note: Requires AWS SDK in build.gradle
     * Currently logs as placeholder - uncomment when AWS is configured
     */
    private void sendViaAwsSNS(String phoneNumber, String message) {
        // For production, uncomment and configure AWS SNS:
        /*
        try {
            SnsClient snsClient = SnsClient.builder().build();
            PublishRequest request = PublishRequest.builder()
                .message(message)
                .phoneNumber(phoneNumber)
                .build();

            PublishResponse result = snsClient.publish(request);
            snsClient.close();

            log.info("SMS sent via AWS SNS: {}", result.messageId());
        } catch (Exception e) {
            log.error("AWS SNS SMS failed: {}", e.getMessage());
            throw new RuntimeException("SMS delivery failed", e);
        }
        */
        log.info("[AWS SNS SMS] To: {} | Message: {}", phoneNumber, message);
    }

    public boolean isSmsEnabled() {
        return smsEnabled;
    }
}
