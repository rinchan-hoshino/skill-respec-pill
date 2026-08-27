package dev.rinchan.skillrespecpill.policy;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.Reader;
import java.util.Set;
import java.util.TreeSet;

public record SkillPolicy(String category, Set<String> defaultEnabled, Set<String> forcedEnabled) {
    private static final Set<String> KEYS = Set.of("category", "default_enabled", "forced_enabled");

    public SkillPolicy {
        requireNamespaced(category);
        defaultEnabled = immutableIds(defaultEnabled, "default_enabled");
        forcedEnabled = immutableIds(forcedEnabled, "forced_enabled");
    }

    public static SkillPolicy parse(Reader reader) {
        JsonElement root = JsonParser.parseReader(reader);
        if (!root.isJsonObject()) throw new IllegalArgumentException("skill policy must be an object");
        JsonObject object = root.getAsJsonObject();
        for (String key : object.keySet()) {
            if (!KEYS.contains(key)) throw new IllegalArgumentException("unsupported skill policy field: " + key);
        }
        JsonElement category = object.get("category");
        if (category == null || !category.isJsonPrimitive() || !category.getAsJsonPrimitive().isString()) {
            throw new IllegalArgumentException("skill policy requires string category");
        }
        return new SkillPolicy(
                category.getAsString(),
                readIds(object, "default_enabled"),
                readIds(object, "forced_enabled"));
    }

    private static Set<String> readIds(JsonObject object, String key) {
        JsonElement value = object.get(key);
        if (value == null) return Set.of();
        if (!value.isJsonArray()) throw new IllegalArgumentException(key + " must be an array");
        JsonArray array = value.getAsJsonArray();
        var ids = new TreeSet<String>();
        for (JsonElement element : array) {
            if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
                throw new IllegalArgumentException(key + " entries must be strings");
            }
            String id = element.getAsString();
            requireNodeId(id, key);
            if (!ids.add(id)) throw new IllegalArgumentException("duplicate " + key + " id: " + id);
        }
        return Set.copyOf(ids);
    }

    private static Set<String> immutableIds(Set<String> ids, String key) {
        if (ids == null) return Set.of();
        var copy = new TreeSet<String>();
        for (String id : ids) {
            requireNodeId(id, key);
            copy.add(id);
        }
        return Set.copyOf(copy);
    }

    private static void requireNamespaced(String id) {
        if (id == null || !id.matches("[a-z0-9_.-]+:[a-z0-9/._-]+")) {
            throw new IllegalArgumentException("invalid namespaced category: " + id);
        }
    }

    private static void requireNodeId(String id, String key) {
        if (id == null || id.isEmpty() || !id.equals(id.trim())) {
            throw new IllegalArgumentException("invalid exact node id in " + key + ": " + id);
        }
    }
}
