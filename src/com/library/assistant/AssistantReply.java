package com.library.assistant;

public final class AssistantReply {
    private final String message;
    private final AssistantAction action;

    public AssistantReply(String message, AssistantAction action) {
        this.message = message == null ? "" : message;
        this.action = action == null ? AssistantAction.none() : action;
    }

    public String getMessage() {
        return message;
    }

    public AssistantAction getAction() {
        return action;
    }
}

