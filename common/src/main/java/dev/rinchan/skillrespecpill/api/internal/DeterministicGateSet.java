package dev.rinchan.skillrespecpill.api.internal;

import java.util.Optional;
import java.util.TreeMap;

public final class DeterministicGateSet<C, R> {
    private final TreeMap<String, Gate<C, R>> gates = new TreeMap<>();

    public synchronized void register(String namespacedId, Gate<C, R> gate) {
        if (namespacedId == null || !namespacedId.matches("[a-z0-9_.-]+:[a-z0-9/._-]+")) {
            throw new IllegalArgumentException("gate id must be namespaced: " + namespacedId);
        }
        if (gate == null) throw new IllegalArgumentException("gate must not be null");
        if (gates.putIfAbsent(namespacedId, gate) != null) {
            throw new IllegalArgumentException("duplicate gate id: " + namespacedId);
        }
    }

    public synchronized Decision<R> evaluate(C context) {
        for (var entry : gates.entrySet()) {
            Decision<R> decision = entry.getValue().evaluate(context);
            if (decision == null) throw new IllegalStateException("gate returned null: " + entry.getKey());
            if (!decision.allowed()) {
                return new Decision<>(false, decision.denialReason(), Optional.of(entry.getKey()));
            }
        }
        return Decision.allow();
    }

    @FunctionalInterface
    public interface Gate<C, R> {
        Decision<R> evaluate(C context);
    }

    public record Decision<R>(boolean allowed, Optional<R> denialReason, Optional<String> denyingGateId) {
        public Decision {
            denialReason = denialReason == null ? Optional.empty() : denialReason;
            denyingGateId = denyingGateId == null ? Optional.empty() : denyingGateId;
            if (allowed && (denialReason.isPresent() || denyingGateId.isPresent())) {
                throw new IllegalArgumentException("allowed decision cannot carry denial data");
            }
        }

        public static <R> Decision<R> allow() {
            return new Decision<>(true, Optional.empty(), Optional.empty());
        }

        public static <R> Decision<R> deny(Optional<R> reason) {
            return new Decision<>(false, reason, Optional.empty());
        }
    }
}
