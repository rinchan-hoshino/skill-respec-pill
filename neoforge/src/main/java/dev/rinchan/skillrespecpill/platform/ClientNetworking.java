package dev.rinchan.skillrespecpill.platform;

import dev.rinchan.skillrespecpill.SkillRespecPill;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;

public final class ClientNetworking {
    private ClientNetworking() {
    }

    public static void send(CustomPacketPayload payload) {
        try {
            PacketDistributor.sendToServer(payload);
        } catch (RuntimeException exception) {
            SkillRespecPill.LOGGER.error("Client packet {} failed to send", payload.type().id(), exception);
        }
    }
}
