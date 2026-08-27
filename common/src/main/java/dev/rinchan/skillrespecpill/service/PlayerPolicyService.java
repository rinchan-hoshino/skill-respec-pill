package dev.rinchan.skillrespecpill.service;

import dev.rinchan.skillrespecpill.SkillRespecPill;
import dev.rinchan.skillrespecpill.network.SkillRespecNetworking;
import dev.rinchan.skillrespecpill.policy.PolicyRepository;
import dev.rinchan.skillrespecpill.state.PlayerDefaultGrantData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.puffish.skillsmod.api.SkillsAPI;

public final class PlayerPolicyService {
    private PlayerPolicyService() {
    }

    public static void reconcileAndSync(ServerPlayer player) {
        try {
            var policies = PolicyRepository.loadAll(player.level().getServer());
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
                    "Policy reconciliation failed closed for player {}",
                    player.getGameProfile().name(), exception);
        }
    }

    public static void copyDefaultGrantState(Player source, Player target) {
        PlayerDefaultGrantData.copy(source, target);
    }
}
