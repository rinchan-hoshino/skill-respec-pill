package dev.rinchan.skillrespecpill.policy;

import dev.rinchan.skillrespecpill.SkillRespecPill;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;

public final class PolicyRepository {
    private static final String ROOT = "skill_respec_pill/policies";

    private PolicyRepository() {
    }

    public static Map<Identifier, SkillPolicy> loadAll(MinecraftServer server) {
        return loadAll(server.getResourceManager());
    }

    public static Map<Identifier, SkillPolicy> loadAll(ResourceManager resourceManager) {
        var byCategory = new TreeMap<Identifier, SkillPolicy>();
        var resources = new TreeMap<>(resourceManager.listResources(
                ROOT, id -> id.getPath().endsWith(".json")));
        for (var entry : resources.entrySet()) {
            try (var reader = new InputStreamReader(entry.getValue().open(), StandardCharsets.UTF_8)) {
                SkillPolicy policy = SkillPolicy.parse(reader);
                Identifier category = Identifier.parse(policy.category());
                if (byCategory.putIfAbsent(category, policy) != null) {
                    throw new IllegalArgumentException("duplicate policy for category " + category);
                }
            } catch (Exception exception) {
                SkillRespecPill.LOGGER.error("Failed to load skill policy {}; policies fail closed", entry.getKey(), exception);
                throw new IllegalStateException("invalid skill policy " + entry.getKey(), exception);
            }
        }
        return Map.copyOf(byCategory);
    }

    public static Optional<SkillPolicy> find(MinecraftServer server, Identifier categoryId) {
        return Optional.ofNullable(loadAll(server).get(categoryId));
    }
}
