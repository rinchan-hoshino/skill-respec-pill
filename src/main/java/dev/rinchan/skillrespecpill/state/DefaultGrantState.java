package dev.rinchan.skillrespecpill.state;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public final class DefaultGrantState {
    private final Map<String, Set<String>> grantedByCategory;

    public DefaultGrantState() {
        this.grantedByCategory = new HashMap<>();
    }

    private DefaultGrantState(Map<String, Set<String>> values) {
        this.grantedByCategory = values;
    }

    public boolean wasGranted(String category, String nodeId) {
        return grantedByCategory.getOrDefault(category, Set.of()).contains(nodeId);
    }

    public void markGranted(String category, String nodeId) {
        requireExact(category, "category");
        requireExact(nodeId, "node");
        grantedByCategory.computeIfAbsent(category, ignored -> new TreeSet<>()).add(nodeId);
    }

    public Set<String> pending(String category, Set<String> currentDefaults) {
        requireExact(category, "category");
        var pending = new TreeSet<>(currentDefaults == null ? Set.<String>of() : currentDefaults);
        pending.removeAll(grantedByCategory.getOrDefault(category, Set.of()));
        return Set.copyOf(pending);
    }

    public Map<String, Set<String>> snapshot() {
        var result = new HashMap<String, Set<String>>();
        grantedByCategory.forEach((category, nodes) -> result.put(category, Set.copyOf(nodes)));
        return Map.copyOf(result);
    }

    public DefaultGrantState copy() {
        var copied = new HashMap<String, Set<String>>();
        grantedByCategory.forEach((category, nodes) -> copied.put(category, new TreeSet<>(nodes)));
        return new DefaultGrantState(copied);
    }

    private static void requireExact(String value, String role) {
        if (value == null || value.isEmpty() || !value.equals(value.trim())) {
            throw new IllegalArgumentException("invalid exact " + role + " id: " + value);
        }
    }
}
