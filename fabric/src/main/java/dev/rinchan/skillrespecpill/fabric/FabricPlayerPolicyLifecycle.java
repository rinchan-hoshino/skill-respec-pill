package dev.rinchan.skillrespecpill.fabric;

import dev.rinchan.skillrespecpill.service.PlayerPolicyService;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public final class FabricPlayerPolicyLifecycle {
    private FabricPlayerPolicyLifecycle() {
    }

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                PlayerPolicyService.reconcileAndSync(handler.getPlayer()));
        ServerPlayerEvents.COPY_FROM.register((source, target, alive) ->
                PlayerPolicyService.copyDefaultGrantState(source, target));
        ServerPlayerEvents.AFTER_RESPAWN.register((source, target, alive) ->
                PlayerPolicyService.reconcileAndSync(target));
    }
}
