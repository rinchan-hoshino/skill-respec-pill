package dev.rinchan.skillrespecpill.network;

import dev.rinchan.skillrespecpill.SkillRespecConfig;
import dev.rinchan.skillrespecpill.SkillRespecPill;
import dev.rinchan.skillrespecpill.client.ClientPolicyState;
import dev.rinchan.skillrespecpill.policy.PolicyRepository;
import dev.rinchan.skillrespecpill.policy.SkillPolicy;
import dev.rinchan.skillrespecpill.service.RespecService;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
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
                    if (context.player() instanceof ServerPlayer player) {
                        RespecService.resetPage(player, payload.categoryId());
                    }
                }));
        registrar.playToServer(PolicyRequestPayload.TYPE, PolicyRequestPayload.CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) syncCurrentPolicies(player);
                }));
        registrar.playToClient(PolicySyncPayload.TYPE, PolicySyncPayload.CODEC,
                (payload, context) -> context.enqueueWork(() -> ClientPolicyState.accept(payload)));
    }

    private static void syncCurrentPolicies(ServerPlayer player) {
        try {
            syncPolicies(player, PolicyRepository.loadAll(player.getServer()));
        } catch (Exception exception) {
            SkillRespecPill.LOGGER.error(
                    "Policy preview sync failed closed for player {}",
                    player.getGameProfile().getName(), exception);
        }
    }

    public static void syncPolicies(
            ServerPlayer player,
            Map<ResourceLocation, SkillPolicy> policies) {
        var forced = new HashMap<ResourceLocation, java.util.Set<String>>();
        policies.forEach((category, policy) -> forced.put(category, policy.forcedEnabled()));
        PacketDistributor.sendToPlayer(player,
                new PolicySyncPayload(forced, SkillRespecConfig.cascadeRefundEnabled()));
    }
}
