package dev.rinchan.skillrespecpill.service;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public final class PlayerPolicyLifecycle {
    private PlayerPolicyLifecycle() {
    }

    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerPolicyService.reconcileAndSync(player);
        }
    }

    public static void onClone(PlayerEvent.Clone event) {
        PlayerPolicyService.copyDefaultGrantState(event.getOriginal(), event.getEntity());
    }
}
