package com.library.assistant;

import java.util.Locale;
import java.util.regex.Pattern;

public final class AssistantRule {
    private final int priority;
    private final AssistantRuleMatchType matchType;
    private final String patternText;
    private final Pattern regex;
    private final String response;
    private final AssistantActionType actionType;
    private final String arg1;
    private final String arg2;

    public AssistantRule(
        int priority,
        AssistantRuleMatchType matchType,
        String patternText,
        String response,
        AssistantActionType actionType,
        String arg1,
        String arg2
    ) {
        this.priority = priority;
        this.matchType = matchType == null ? AssistantRuleMatchType.CONTAINS : matchType;
        this.patternText = patternText == null ? "" : patternText;
        this.regex = this.matchType == AssistantRuleMatchType.REGEX
            ? Pattern.compile(this.patternText, Pattern.CASE_INSENSITIVE)
            : null;
        this.response = response == null ? "" : response;
        this.actionType = actionType == null ? AssistantActionType.NONE : actionType;
        this.arg1 = (arg1 == null || arg1.trim().isEmpty()) ? null : arg1.trim();
        this.arg2 = (arg2 == null || arg2.trim().isEmpty()) ? null : arg2.trim();
    }

    public int getPriority() {
        return priority;
    }

    public String getResponse() {
        return response;
    }

    public AssistantAction toAction() {
        return new AssistantAction(actionType, arg1, arg2);
    }

    public boolean matches(String input) {
        if (input == null) return false;
        String text = input.trim();
        if (text.isEmpty()) return false;

        if (matchType == AssistantRuleMatchType.REGEX) {
            return regex != null && regex.matcher(text).find();
        }

        String hay = text.toLowerCase(Locale.ENGLISH);
        String needle = patternText.toLowerCase(Locale.ENGLISH).trim();
        if (needle.isEmpty()) return false;
        return hay.contains(needle);
    }
}

