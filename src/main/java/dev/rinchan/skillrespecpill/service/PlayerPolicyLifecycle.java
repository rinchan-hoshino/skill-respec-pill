package dev.rinchan.skillrespecpill.service;

import dev.rinchan.skillrespecpill.SkillRespecPill;
import dev.rinchan.skillrespecpill.network.SkillRespecNetworking;
import dev.rinchan.skillrespecpill.policy.PolicyRepository;
import dev.rinchan.skillrespecpill.state.PlayerDefaultGrantData;
import net.minecraft.server.level.ServerPlayer;
import net.puffish.skillsmod.api.SkillsAPI;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public final class PlayerPolicyLifecycle {
    private PlayerPolicyLifecycle() {
    }

    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        try {
            var policies = PolicyRepository.loadAll(player.getServer());
            var state = PlayerDefaultGrantData.read(player);
            for (var entry : policies.entrySet()) {
                SkillsAPI.getCategory(entry.getKey()).ifPresentOrElse(
                        category -> RespecService.reconcileLogin(player, category, entry.getValue(), state),
                        () -> SkillRespecPill.LOGGER.error(
                                "Policy category {} is missing from Puffish Skills; baseline failed closed",
                                entry.getKey()));
            }
            PlayerDefaultGrantData.write(player, state);
            SkillRespecNetworking.syncPolicies(player, policies);
        } catch (Exception exception) {
            SkillRespecPill.LOGGER.error(
                    "Login policy reconciliation failed closed for player {}",
                    player.getGameProfile().getName(), exception);
        }
    }

    public static void onClone(PlayerEvent.Clone event) {
        PlayerDefaultGrantData.copy(event.getOriginal(), event.getEntity());
    }
}
