package com.library.assistant;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public final class AssistantEngine {
    private static final Path RULES_FILE = Paths.get("assistant", "rules.tsv");
    private static final Path KB_DIR = Paths.get("assistant", "kb");

    private volatile long lastLoadedAtMs = 0L;
    private volatile long lastRulesMtimeMs = -1L;
    private volatile List<AssistantRule> rules = new ArrayList<>();

    private final AssistantKnowledgeBase kb = new AssistantKnowledgeBase(KB_DIR);

    public AssistantReply handle(String userText) {
        String text = userText == null ? "" : userText.trim();
        if (text.isEmpty()) {
            return new AssistantReply("Type something like: low stock, overdue, issue book, return book.", AssistantAction.none());
        }

        if ("/reload".equalsIgnoreCase(text)) {
            reload(true);
            return new AssistantReply("Reloaded rules from ./assistant/rules.tsv.", AssistantAction.none());
        }

        reload(false);

        for (AssistantRule rule : rules) {
            if (!rule.matches(text)) continue;
            return new AssistantReply(rule.getResponse(), rule.toAction());
        }

        return new AssistantReply(
            "I didn’t match that yet. Try: “low stock”, “overdue”, “issue book”, “return book”, “ORA-12514”, “OTP email”.",
            AssistantAction.none()
        );
    }

    public String loadHelp(String topicKey) {
        String content = kb.loadTopic(topicKey);
        if (content != null && !content.trim().isEmpty()) return content;
        return "No help article found for: " + topicKey;
    }

    private void reload(boolean force) {
        long now = System.currentTimeMillis();
        if (!force && (now - lastLoadedAtMs) < 1500) return; // light throttling
        lastLoadedAtMs = now;

        long mtime = fileMtime(RULES_FILE);
        if (!force && mtime == lastRulesMtimeMs) return;
        lastRulesMtimeMs = mtime;

        try {
            List<AssistantRule> loaded = AssistantRuleLoader.loadFromTsv(RULES_FILE);
            if (!loaded.isEmpty()) {
                rules = loaded;
                return;
            }
        } catch (Exception ignored) {
        }

        rules = builtInFallbackRules();
    }

    private long fileMtime(Path p) {
        try {
            if (p == null || !Files.exists(p)) return -1L;
            return Files.getLastModifiedTime(p).toMillis();
        } catch (Exception e) {
            return -1L;
        }
    }

    private List<AssistantRule> builtInFallbackRules() {
        List<AssistantRule> out = new ArrayList<>();
        out.add(new AssistantRule(
            10,
            AssistantRuleMatchType.CONTAINS,
            "low stock",
            "I can generate a low-stock report and export it to Excel.",
            AssistantActionType.REPORT_LOW_STOCK,
            "2",
            null
        ));
        out.add(new AssistantRule(
            20,
            AssistantRuleMatchType.CONTAINS,
            "overdue",
            "I can list overdue issues and export to Excel.",
            AssistantActionType.REPORT_OVERDUE,
            null,
            null
        ));
        out.add(new AssistantRule(
            30,
            AssistantRuleMatchType.CONTAINS,
            "issue",
            "I’ll take you to Circulation → Issue Book.",
            AssistantActionType.NAVIGATE,
            "CIRCULATION",
            "ISSUE"
        ));
        out.add(new AssistantRule(
            31,
            AssistantRuleMatchType.CONTAINS,
            "return",
            "I’ll take you to Circulation → Return Book.",
            AssistantActionType.NAVIGATE,
            "CIRCULATION",
            "RETURN"
        ));
        return out;
    }
}

