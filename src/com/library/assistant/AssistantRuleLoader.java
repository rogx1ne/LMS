package com.library.assistant;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class AssistantRuleLoader {
    private AssistantRuleLoader() {}

    public static List<AssistantRule> loadFromTsv(Path file) throws IOException {
        List<AssistantRule> rules = new ArrayList<>();
        if (file == null || !Files.exists(file)) return rules;

        try (BufferedReader br = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            int lineNo = 0;
            while ((line = br.readLine()) != null) {
                lineNo++;
                String raw = line.trim();
                if (raw.isEmpty()) continue;
                if (raw.startsWith("#")) continue;

                String[] parts = raw.split("\\t", -1);
                if (parts.length < 5) continue;
                if ("priority".equalsIgnoreCase(parts[0].trim())) continue; // header row

                int priority = parseInt(parts[0].trim(), 1000);
                AssistantRuleMatchType matchType = parseMatchType(parts[1].trim());
                String pattern = parts[2];
                String response = parts[3];
                AssistantActionType actionType = parseActionType(parts[4].trim());
                String arg1 = parts.length >= 6 ? parts[5] : null;
                String arg2 = parts.length >= 7 ? parts[6] : null;

                try {
                    rules.add(new AssistantRule(priority, matchType, pattern, response, actionType, arg1, arg2));
                } catch (RuntimeException ignored) {
                    // Skip invalid rule (e.g., bad regex).
                }
            }
        }

        rules.sort(Comparator.comparingInt(AssistantRule::getPriority));
        return rules;
    }

    private static int parseInt(String raw, int fallback) {
        try {
            return Integer.parseInt(raw);
        } catch (Exception e) {
            return fallback;
        }
    }

    private static AssistantRuleMatchType parseMatchType(String raw) {
        if ("REGEX".equalsIgnoreCase(raw)) return AssistantRuleMatchType.REGEX;
        return AssistantRuleMatchType.CONTAINS;
    }

    private static AssistantActionType parseActionType(String raw) {
        for (AssistantActionType t : AssistantActionType.values()) {
            if (t.name().equalsIgnoreCase(raw)) return t;
        }
        return AssistantActionType.NONE;
    }
}

