package com.library.assistant;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class AssistantKnowledgeBase {
    private final Path kbDir;

    public AssistantKnowledgeBase(Path kbDir) {
        this.kbDir = kbDir;
    }

    public String loadTopic(String key) {
        if (key == null || key.trim().isEmpty()) return "";
        if (kbDir == null) return "";

        Path file = kbDir.resolve(key.trim() + ".txt");
        if (!Files.exists(file)) return "";

        try {
            return Files.readString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "";
        }
    }
}

