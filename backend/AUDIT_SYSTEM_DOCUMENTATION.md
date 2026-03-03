# Alteration Audit & Recording System Documentation

## Overview

The Alteration Audit & Recording System tracks all alteration and substitution records, providing complete audit trails for staff absences and substitute assignments. This system enables HOD (Head of Department) and Admin to view, analyze, and export alteration records for compliance and reporting purposes.

## Architecture

### Database Schema

The `alteration_audit` table stores comprehensive records with the following structure:

**Original Staff Information:**
- `original_staff_id` - ID of the staff member who is absent
- `original_staff_name` - Full name of original staff
- `original_staff_email` - Email of original staff

**Absence Details:**
- `absence_date` - Date of absence
- `absence_type` - Type of absence (AN=Morning, AF=Afternoon, FN=Full Day)
- `class_name` - Class/Section code
- `subject` - Subject taught
- `period_number` - Period number (1-6)

**First Substitute Assignment:**
- `first_substitute_id` - ID of first assigned substitute
- `first_substitute_name` - Full name
- `first_substitute_email` - Email
- `first_substitute_status` - Status (PENDING, ACCEPTED, REJECTED)
- `first_substitute_response_time` - When they responded

**Second Substitute Assignment (if first rejects):**
- `second_substitute_id` - ID of second assigned substitute
- `second_substitute_name` - Full name
- `second_substitute_email` - Email
- `second_substitute_status` - Status (PENDING, ACCEPTED, REJECTED, NOT_ASSIGNED)
- `second_substitute_response_time` - When they responded

**Audit Trail:**
- `created_at` - When audit record was created
- `last_updated_at` - Last modification timestamp
- `final_status` - Final outcome (FULFILLED, PENDING, CANCELLED)
- `remarks` - Notes and comments

## Features

### 1. Automatic Audit Logging

When an alteration is created:
```
processAlteration() → Creates Alteration record → logAlterationAudit() → AlterationAudit record created
```

### 2. Multi-Stage Substitution Tracking

The system tracks the complete substitution workflow:

**Stage 1: First Substitute Assignment**
- Initial substitute is assigned based on priority algorithm
- Status: PENDING
- Audit record created with first substitute details

**Stage 2: First Substitute Response**
- If accepted: Status → ACCEPTED, Final Status → FULFILLED
- If rejected: Move to Stage 3

**Stage 3: Auto-Reassignment to Second Substitute**
- System automatically finds and assigns alternative substitute
- Previous substitute status: REJECTED
- New substitute status: PENDING
- Audit record updated with both substitute details

**Stage 4: Second Substitute Response**
- If accepted: Status → ACCEPTED, Final Status → FULFILLED
- If rejected: Status → REJECTED, Final Status → PENDING

### 3. Export Capabilities

#### API Endpoints

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/alteration/audit/export` | GET | Get all audit records as JSON |
| `/alteration/audit/staff/{staffId}` | GET | Get audit records for specific staff |
| `/alteration/audit/unfulfilled` | GET | Get all pending/unfulfilled alterations |
| `/alteration/audit/export-csv` | GET | Download audit records as CSV file |

#### CSV Export Format

The exported CSV includes:
- Absence Date
- Original Staff Name & Email
- Absence Type
- Class, Subject, Period
- First Substitute (Name, Email, Status, Response Time)
- Second Substitute (Name, Email, Status, Response Time)
- Final Status
- Created & Updated Timestamps
- Remarks

Example CSV headers:
```
Absence Date,Original Staff,Email,Absence Type,Class,Subject,Period,First Substitute,Email,First Status,Response Time,Second Substitute,Email,Second Status,Response Time,Final Status,Created At,Updated At,Remarks
```

## Usage Flow

### For Staff Members

1. **Mark Attendance**
   - Staff marks their absence status (LEAVE, ABSENT, ONDUTY, etc.)
   - System automatically creates alteration record
   - Audit record created with original staff and substitute info

2. **Receive Notification**
   - Substitute receives notification of assignment
   - Can accept or reject the alteration
   - Acceptance records response time

3. **If Rejected**
   - System automatically finds second substitute
   - Audit record updated with second substitute
   - New substitute receives notification

### For HOD/Admin

1. **View Audit Records**
   ```
   GET /alteration/audit/export → JSON list of all audit records
   ```

2. **Filter by Staff**
   ```
   GET /alteration/audit/staff/{staffId} → Audit history for specific staff
   ```

3. **View Unfulfilled Alterations**
   ```
   GET /alteration/audit/unfulfilled → Pending alterations requiring intervention
   ```

4. **Export for Reporting**
   ```
   GET /alteration/audit/export-csv → Download as CSV for Excel/Analysis
   ```

## Database Migration

The migration file `V5__Create_Alteration_Audit_Table.sql` creates:
- `alteration_audit` table with all required columns
- Foreign key constraints for data integrity
- Indexes on commonly queried columns for performance

**Indexes Created:**
- `original_staff_id` - For filtering by staff
- `absence_date` - For date range queries
- `final_status` - For status-based filtering
- `created_at` - For sorting by creation time
- `first_substitute_id` & `second_substitute_id` - For substitute tracking

## Integration Points

### AlterationService

**Methods Added:**
- `logAlterationAudit(Alteration)` - Creates audit record
- `updateAuditOnFirstSubstituteResponse(Alteration, String)` - Updates first substitute response
- `updateAuditOnSecondSubstituteAssignment(Alteration, Staff)` - Records substitute change

**Call Sequence:**
```
processAlteration()
  ↓
  → Create Alteration
  → Save to alterationRepository
  → Call logAlterationAudit()
  → Broadcast via WebSocket
```

### AlterationController

**New Endpoints:**
- `GET /alteration/audit/export` - JSON export
- `GET /alteration/audit/staff/{staffId}` - Staff-specific records
- `GET /alteration/audit/unfulfilled` - Pending alterations
- `GET /alteration/audit/export-csv` - CSV download

## Error Handling

The audit logging is designed with fail-safe behavior:
- If audit logging fails, it does NOT block alteration creation
- Errors are logged with detailed stack traces
- System continues to function with or without audit records
- Administrator can retry audit logging if needed

```java
try {
    // Audit logging
} catch (Exception e) {
    log.error("Error creating audit record", e);
    // Don't throw - audit failure shouldn't block alteration
}
```

## Performance Considerations

### Indexing Strategy
- Optimal indexes for common queries (staff, date, status)
- Foreign key constraints for referential integrity
- Composite indexes can be added for complex queries

### Scalability
- Table design supports high-volume queries
- Pagination recommended for large result sets in future
- CSV export optimized for bulk data retrieval

## Security & Access Control

Currently, audit endpoints are accessible via REST API. In production, implement:

1. **Role-Based Access Control**
   - Only HOD/ADMIN roles can access audit data
   - Restrict export capabilities to authorized users

2. **Data Privacy**
   - Audit records contain staff email and personal info
   - Implement rate limiting on export endpoints
   - Consider audit logs for sensitive data access

3. **Recommended Annotations**
   ```java
   @PreAuthorize("hasRole('HOD') or hasRole('ADMIN')")
   public ResponseEntity<?> exportAuditAsCSV()
   ```

## Sample Query Patterns

### Get All Alterations for a Staff Member
```
GET /alteration/audit/staff/5 → Returns all audit records for staff ID 5
```

### Get Pending Alterations Requiring HOD Action
```
GET /alteration/audit/unfulfilled → Returns alterations not yet fulfilled
```

### Export for Monthly Reporting
```
GET /alteration/audit/export-csv → Downloads CSV file with date stamp
```

### Analyze Substitution Success Rate
Query by final_status:
- FULFILLED: Successfully substituted
- PENDING: Awaiting response
- CANCELLED: Not assigned

## Future Enhancements

1. **Advanced Filtering**
   - Date range queries (already supported in repository)
   - Department-based filtering
   - Status-based analytics

2. **Reporting Features**
   - Staff substitution statistics
   - Repeated absence patterns
   - Substitute load analysis

3. **Analytics Dashboard**
   - Fulfillment rate charts
   - Average response times
   - Staff availability metrics

4. **Audit Compliance**
   - Immutable audit logs
   - Administrative approval workflows
   - Digital signatures for exports

## Testing

### Unit Tests (Recommended)
```java
@Test
void testAlterationAuditCreation() {
    // Create alteration
    // Verify audit record exists
    // Check all fields populated correctly
}

@Test
void testSecondSubstituteAuditUpdate() {
    // Create alteration with first substitute
    // Simulate rejection
    // Verify audit record updated with second substitute
}
```

### Integration Tests
- Test complete flow from attendance marking to audit record
- Verify CSV export contains correct data
- Test pagination and filtering

## Troubleshooting

### Audit Records Not Created
1. Check if alteration_audit table exists: `SELECT count(*) FROM alteration_audit;`
2. Verify AlterationAuditRepository is autowired
3. Check application logs for exceptions
4. Ensure migration script ran successfully

### CSV Export Issues
1. Verify no special characters in staff names
2. Check for NULL values in optional fields
3. Ensure sufficient disk space for large exports
4. Check browser download settings

## Support & Maintenance

- Monitor table size periodically
- Archive old records (> 1 year) if needed
- Review and update indexes based on query patterns
- Document any custom queries or reports
