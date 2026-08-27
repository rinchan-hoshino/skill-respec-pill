package dev.rinchan.skillrespecpill.network;

import dev.rinchan.skillrespecpill.SkillRespecConfig;
import dev.rinchan.skillrespecpill.SkillRespecPill;
import dev.rinchan.skillrespecpill.policy.PolicyRepository;
import dev.rinchan.skillrespecpill.policy.SkillPolicy;
import dev.rinchan.skillrespecpill.service.RespecService;
import java.util.HashMap;
import java.util.Map;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public final class SkillRespecNetworking {
    private SkillRespecNetworking() {
    }

    public static void registerServer() {
        PayloadTypeRegistry.serverboundPlay().register(ResetPagePayload.TYPE, ResetPagePayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(PolicyRequestPayload.TYPE, PolicyRequestPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(PolicySyncPayload.TYPE, PolicySyncPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ResetPagePayload.TYPE, (payload, context) ->
                context.server().execute(() -> {
                    try {
                        RespecService.resetPage(context.player(), payload.categoryId());
                    } catch (RuntimeException exception) {
                        SkillRespecPill.LOGGER.error("Server reset packet failed for category {}",
                                payload.categoryId(), exception);
                    }
                }));
        ServerPlayNetworking.registerGlobalReceiver(PolicyRequestPayload.TYPE, (payload, context) ->
                context.server().execute(() -> {
                    try {
                        syncCurrentPolicies(context.player());
                    } catch (RuntimeException exception) {
                        SkillRespecPill.LOGGER.error("Server policy-request packet failed", exception);
                    }
                }));
    }

    private static void syncCurrentPolicies(ServerPlayer player) {
        try {
            syncPolicies(player, PolicyRepository.loadAll(player.level().getServer()));
        } catch (Exception exception) {
            SkillRespecPill.LOGGER.error(
                    "Policy preview sync failed closed for player {}",
                    player.getGameProfile().name(), exception);
        }
    }

    public static void syncPolicies(
            ServerPlayer player,
            Map<Identifier, SkillPolicy> policies) {
        var forced = new HashMap<Identifier, java.util.Set<String>>();
        policies.forEach((category, policy) -> forced.put(category, policy.forcedEnabled()));
        try {
            ServerPlayNetworking.send(player,
                    new PolicySyncPayload(forced, SkillRespecConfig.cascadeRefundEnabled()));
        } catch (RuntimeException exception) {
            SkillRespecPill.LOGGER.error("Server policy-sync packet failed for player {}",
                    player.getGameProfile().name(), exception);
        }
    }
}
