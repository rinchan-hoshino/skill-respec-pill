package dev.rinchan.skillrespecpill.platform;

import dev.rinchan.skillrespecpill.SkillRespecPill;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public final class ClientNetworking {
    private ClientNetworking() {
    }

    public static void send(CustomPacketPayload payload) {
        try {
            ClientPlayNetworking.send(payload);
        } catch (RuntimeException exception) {
            SkillRespecPill.LOGGER.error("Client packet {} failed to send", payload.type().id(), exception);
        }
    }
}
