package com.library.assistant;

public final class AssistantAction {
    private final AssistantActionType type;
    private final String arg1;
    private final String arg2;

    public AssistantAction(AssistantActionType type, String arg1, String arg2) {
        this.type = type == null ? AssistantActionType.NONE : type;
        this.arg1 = arg1;
        this.arg2 = arg2;
    }

    public static AssistantAction none() {
        return new AssistantAction(AssistantActionType.NONE, null, null);
    }

    public AssistantActionType getType() {
        return type;
    }

    public String getArg1() {
        return arg1;
    }

    public String getArg2() {
        return arg2;
    }
}

