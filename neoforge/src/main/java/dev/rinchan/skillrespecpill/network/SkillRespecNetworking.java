package dev.rinchan.skillrespecpill.network;

import dev.rinchan.skillrespecpill.SkillRespecConfig;
import dev.rinchan.skillrespecpill.SkillRespecPill;
import dev.rinchan.skillrespecpill.client.ClientPolicyState;
import dev.rinchan.skillrespecpill.policy.PolicyRepository;
import dev.rinchan.skillrespecpill.policy.SkillPolicy;
import dev.rinchan.skillrespecpill.service.RespecService;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class SkillRespecNetworking {
    private SkillRespecNetworking() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1.0.0");
        registrar.playToServer(ResetPagePayload.TYPE, ResetPagePayload.CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    try {
                        if (!(context.player() instanceof ServerPlayer player)) {
                            SkillRespecPill.LOGGER.error("Server reset packet had no server player");
                            return;
                        }
                        RespecService.resetPage(player, payload.categoryId());
                    } catch (RuntimeException exception) {
                        SkillRespecPill.LOGGER.error("Server reset packet failed for category {}",
                                payload.categoryId(), exception);
                    }
                }));
        registrar.playToServer(PolicyRequestPayload.TYPE, PolicyRequestPayload.CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    try {
                        if (!(context.player() instanceof ServerPlayer player)) {
                            SkillRespecPill.LOGGER.error("Server policy-request packet had no server player");
                            return;
                        }
                        syncCurrentPolicies(player);
                    } catch (RuntimeException exception) {
                        SkillRespecPill.LOGGER.error("Server policy-request packet failed", exception);
                    }
                }));
        registrar.playToClient(PolicySyncPayload.TYPE, PolicySyncPayload.CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    try {
                        ClientPolicyState.accept(payload);
                    } catch (RuntimeException exception) {
                        SkillRespecPill.LOGGER.error("Client policy-sync packet failed", exception);
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
            PacketDistributor.sendToPlayer(player,
                    new PolicySyncPayload(forced, SkillRespecConfig.cascadeRefundEnabled()));
        } catch (RuntimeException exception) {
            SkillRespecPill.LOGGER.error("Server policy-sync packet failed for player {}",
                    player.getGameProfile().name(), exception);
        }
    }
}
