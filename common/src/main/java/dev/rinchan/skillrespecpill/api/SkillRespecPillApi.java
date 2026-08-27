package dev.rinchan.skillrespecpill.api;

import dev.rinchan.skillrespecpill.api.internal.DeterministicGateSet;
import java.util.EnumMap;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class SkillRespecPillApi {
    private static final EnumMap<Action, DeterministicGateSet<GateContext, Component>> GATES =
            new EnumMap<>(Action.class);

    static {
        for (Action action : Action.values()) GATES.put(action, new DeterministicGateSet<>());
    }

    private SkillRespecPillApi() {
    }

    public static void registerGate(Action action, ResourceLocation gateId, AuthorizationGate gate) {
        if (action == null || gateId == null || gate == null) {
            throw new IllegalArgumentException("action, gate id, and gate are required");
        }
        GATES.get(action).register(gateId.toString(), context -> {
            Authorization decision = gate.authorize(context);
            if (decision == null) throw new IllegalStateException("gate returned null: " + gateId);
            return decision.allowed()
                    ? DeterministicGateSet.Decision.allow()
                    : DeterministicGateSet.Decision.deny(decision.denialReason());
        });
    }

    public static Authorization evaluate(GateContext context) {
        var result = GATES.get(context.action()).evaluate(context);
        return new Authorization(result.allowed(), result.denialReason());
    }

    public enum Action {
        CASCADE_REFUND,
        PAGE_RESET
    }

    public record GateContext(
            ServerPlayer player,
            Action action,
            ResourceLocation categoryId,
            Optional<String> nodeId) {
        public GateContext {
            if (player == null || action == null || categoryId == null || nodeId == null) {
                throw new IllegalArgumentException("gate context fields are required");
            }
        }
    }

    public record Authorization(boolean allowed, Optional<Component> denialReason) {
        public Authorization {
            denialReason = denialReason == null ? Optional.empty() : denialReason;
            if (allowed && denialReason.isPresent()) {
                throw new IllegalArgumentException("an allowed decision cannot have a denial reason");
            }
        }

        public static Authorization allow() {
            return new Authorization(true, Optional.empty());
        }

        public static Authorization deny() {
            return new Authorization(false, Optional.empty());
        }

        public static Authorization deny(Component reason) {
            return new Authorization(false, Optional.ofNullable(reason));
        }
    }

    @FunctionalInterface
    public interface AuthorizationGate {
        Authorization authorize(GateContext context);
    }
}
