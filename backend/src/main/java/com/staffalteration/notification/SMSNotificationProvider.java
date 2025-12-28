package com.staffalteration.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SMSNotificationProvider {
    
    private static final boolean MOCK_SMS = true;
    
    public boolean sendSMS(String phoneNumber, String message) {
        if (MOCK_SMS) {
            log.info("MOCK SMS: Sending to {} - {}", phoneNumber, message);
            return true;
        }
        
        // Integration with actual SMS provider (e.g., Twilio, AWS SNS)
        try {
            log.info("Sending SMS to: {}, Message: {}", phoneNumber, message);
            // Actual implementation would go here
            return true;
        } catch (Exception e) {
            log.error("Failed to send SMS: {}", e.getMessage());
            return false;
        }
    }
    
    public String createAlterationMessage(String substituteStaffName, String className, 
                                         Integer period, Integer dayOrder, String originalStaffName) {
        return String.format("Hi %s, You have been assigned to teach %s (Period %d, Day %d) on behalf of %s. Please confirm receipt.",
                substituteStaffName, className, period, dayOrder, originalStaffName);
    }
}
