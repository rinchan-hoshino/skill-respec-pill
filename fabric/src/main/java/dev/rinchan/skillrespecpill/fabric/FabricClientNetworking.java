package dev.rinchan.skillrespecpill.fabric;

import dev.rinchan.skillrespecpill.SkillRespecPill;
import dev.rinchan.skillrespecpill.client.ClientPolicyState;
import dev.rinchan.skillrespecpill.network.PolicySyncPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class FabricClientNetworking {
    private FabricClientNetworking() {
    }

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(PolicySyncPayload.TYPE, (payload, context) ->
                context.client().execute(() -> {
                    try {
                        ClientPolicyState.accept(payload);
                    } catch (RuntimeException exception) {
                        SkillRespecPill.LOGGER.error("Client policy-sync packet failed", exception);
                    }
                }));
    }
}
