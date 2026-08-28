package dev.rinchan.skillrespecpill;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import net.fabricmc.loader.api.FabricLoader;

public final class SkillRespecConfig {
    private static final String CASCADE_REFUND_KEY = "cascade_refund_enabled";
    private static volatile boolean cascadeRefundEnabled = true;

    private SkillRespecConfig() {
    }

    public static void load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve("skill_respec_pill.properties");
        var properties = new Properties();
        try {
            if (Files.notExists(path)) {
                Files.createDirectories(path.getParent());
                properties.setProperty(CASCADE_REFUND_KEY, "true");
                try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                    properties.store(writer, "Skill Respec Pill server configuration");
                }
            }
            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                properties.load(reader);
            }
            String value = properties.getProperty(CASCADE_REFUND_KEY, "true").trim();
            if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
                throw new IllegalArgumentException(CASCADE_REFUND_KEY + " must be true or false");
            }
            cascadeRefundEnabled = Boolean.parseBoolean(value);
        } catch (IOException | IllegalArgumentException exception) {
            SkillRespecPill.LOGGER.error("Failed to load Fabric configuration {}", path, exception);
            throw new IllegalStateException("invalid skill_respec_pill Fabric configuration", exception);
        }
    }

    public static boolean cascadeRefundEnabled() {
        return cascadeRefundEnabled;
    }
}
