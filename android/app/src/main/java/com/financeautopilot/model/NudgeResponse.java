package com.financeautopilot.model;

public class NudgeResponse {
    private boolean hasNudge;
    private String message;
    private String nudgeType;

    public NudgeResponse() {}

    public boolean isHasNudge() { return hasNudge; }
    public String getMessage() { return message; }
    public String getNudgeType() { return nudgeType; }
}
