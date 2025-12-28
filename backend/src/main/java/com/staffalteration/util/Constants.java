package com.staffalteration.util;

public class Constants {
    
    public static final int PERIODS_PER_DAY = 6;
    public static final int DAY_ORDERS = 6;
    public static final int HOURS_BEFORE_COLLEGE = 9;
    
    public static final class AlterationRules {
        public static final int PRIORITY_PRESENT = 1;
        public static final int PRIORITY_SAME_CLASS = 2;
        public static final int PRIORITY_LEAST_HOURS = 3;
        public static final int PRIORITY_NO_CLASH = 4;
        public static final int PRIORITY_SAME_SUBJECT = 5;
        public static final int PRIORITY_WORKLOAD = 6;
    }
    
    public static final class ErrorMessages {
        public static final String STAFF_NOT_FOUND = "Staff not found";
        public static final String USER_NOT_FOUND = "User not found";
        public static final String DEPARTMENT_NOT_FOUND = "Department not found";
        public static final String SUBJECT_NOT_FOUND = "Subject not found";
        public static final String CLASS_NOT_FOUND = "Class not found";
        public static final String TIMETABLE_NOT_FOUND = "Timetable not found";
        public static final String ATTENDANCE_NOT_FOUND = "Attendance not found";
        public static final String ALTERATION_NOT_FOUND = "Alteration not found";
        public static final String NOTIFICATION_NOT_FOUND = "Notification not found";
    }
    
    public static final class RoleNames {
        public static final String STAFF = "STAFF";
        public static final String HOD = "HOD";
        public static final String DEAN = "DEAN";
        public static final String ADMIN = "ADMIN";
    }
}
