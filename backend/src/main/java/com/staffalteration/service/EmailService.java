package com.staffalteration.service;

import com.staffalteration.entity.Alteration;
import com.staffalteration.entity.Staff;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {
    
    @Autowired(required = false)
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.from:noreply@staffalteration.com}")
    private String fromEmail;
    
    public void sendAlterationNotification(Alteration alteration) {
        try {
            // If mail sender is not configured, log instead of sending
            if (mailSender == null) {
                logEmailNotification(alteration);
                return;
            }
            
            Staff originalStaff = alteration.getOriginalStaff();
            Staff substituteStaff = alteration.getSubstituteStaff();
            
            // Send email to original staff (who is absent)
            sendEmailToOriginalStaff(originalStaff, alteration);
            
            // Send email to substitute staff (who is replacing)
            sendEmailToSubstituteStaff(substituteStaff, alteration);
            
            log.info("Alteration notification emails sent successfully");
        } catch (Exception e) {
            log.error("Error sending alteration notification emails: {}", e.getMessage(), e);
            // Don't throw exception - alteration should still be created even if email fails
        }
    }
    
    private void sendEmailToOriginalStaff(Staff staff, Alteration alteration) {
        if (staff == null || staff.getEmail() == null) {
            log.warn("Cannot send email to original staff - email not available");
            return;
        }
        
        String subject = "Class Alteration Notification - Absence Recorded";
        String body = String.format(
            "Dear %s,\n\n" +
            "This is to notify you that your absence has been recorded for:\n\n" +
            "Class: %s\n" +
            "Subject: %s\n" +
            "Period: %d\n" +
            "Day: %s\n" +
            "Date: %s\n\n" +
            "Replacement Staff: %s (%s)\n\n" +
            "A substitute teacher has been assigned to cover your class.\n" +
            "If you have any questions, please contact your HOD.\n\n" +
            "Best regards,\n" +
            "Staff Alteration System",
            staff.getFirstName(),
            alteration.getTimetable().getClassRoom().getClassName(),
            alteration.getTimetable().getSubject().getSubjectName(),
            alteration.getTimetable().getPeriodNumber(),
            getDayName(alteration.getTimetable().getDayOrder()),
            alteration.getAlterationDate(),
            alteration.getSubstituteStaff().getFirstName(),
            alteration.getSubstituteStaff().getStaffId()
        );
        
        sendEmail(staff.getEmail(), subject, body);
    }
    
    private void sendEmailToSubstituteStaff(Staff staff, Alteration alteration) {
        if (staff == null || staff.getEmail() == null) {
            log.warn("Cannot send email to substitute staff - email not available");
            return;
        }
        
        String subject = "Class Assignment Notification - You Have Been Assigned";
        String body = String.format(
            "Dear %s,\n\n" +
            "You have been assigned to teach the following class:\n\n" +
            "Class: %s\n" +
            "Subject: %s\n" +
            "Period: %d\n" +
            "Day: %s\n" +
            "Date: %s\n\n" +
            "Original Teacher: %s (%s)\n" +
            "Reason: Teacher Absence\n\n" +
            "Please ensure you are prepared to teach this class. " +
            "You can view more details in the Staff Alteration System.\n\n" +
            "Best regards,\n" +
            "Staff Alteration System",
            staff.getFirstName(),
            alteration.getTimetable().getClassRoom().getClassName(),
            alteration.getTimetable().getSubject().getSubjectName(),
            alteration.getTimetable().getPeriodNumber(),
            getDayName(alteration.getTimetable().getDayOrder()),
            alteration.getAlterationDate(),
            alteration.getOriginalStaff().getFirstName(),
            alteration.getOriginalStaff().getStaffId()
        );
        
        sendEmail(staff.getEmail(), subject, body);
    }
    
    private void sendEmail(String to, String subject, String body) {
        try {
            if (mailSender == null) {
                log.info("Mail service not configured. Would send email to: {} with subject: {}", to, subject);
                return;
            }
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            
            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
    
    private void logEmailNotification(Alteration alteration) {
        log.info("═══════════════════════════════════════════════════════════");
        log.info("EMAIL NOTIFICATION (Mail Service Not Configured)");
        log.info("═══════════════════════════════════════════════════════════");
        log.info("📧 To: {} ({})", alteration.getOriginalStaff().getEmail(), alteration.getOriginalStaff().getStaffId());
        log.info("📧 To: {} ({})", alteration.getSubstituteStaff().getEmail(), alteration.getSubstituteStaff().getStaffId());
        log.info("Subject: Class Alteration Notification");
        log.info("Content: Absence recorded and substitute assigned");
        log.info("Alteration Details: {} -> {} on {}", alteration.getOriginalStaff().getStaffId(), 
                 alteration.getSubstituteStaff().getStaffId(), alteration.getAlterationDate());
        log.info("═══════════════════════════════════════════════════════════");
    }
    
    private String getDayName(int dayOrder) {
        String[] days = {"", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        return dayOrder > 0 && dayOrder < days.length ? days[dayOrder] : "Day " + dayOrder;
    }
}
