package com.library.model;

import java.sql.Timestamp;

public class AuditLogEntry {
    private final long logId;
    private final String userId;
    private final String module;
    private final String actionDescription;
    private final Timestamp timestamp;

    public AuditLogEntry(long logId, String userId, String module, String actionDescription, Timestamp timestamp) {
        this.logId = logId;
        this.userId = userId;
        this.module = module;
        this.actionDescription = actionDescription;
        this.timestamp = timestamp;
    }

    public long getLogId() { return logId; }
    public String getUserId() { return userId; }
    public String getModule() { return module; }
    public String getActionDescription() { return actionDescription; }
    public Timestamp getTimestamp() { return timestamp; }
}
