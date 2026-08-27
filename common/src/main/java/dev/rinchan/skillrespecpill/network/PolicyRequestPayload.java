package dev.rinchan.skillrespecpill.network;

import dev.rinchan.skillrespecpill.SkillRespecPill;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PolicyRequestPayload() implements CustomPacketPayload {
    public static final PolicyRequestPayload INSTANCE = new PolicyRequestPayload();
    public static final Type<PolicyRequestPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(SkillRespecPill.MOD_ID, "policy_request"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PolicyRequestPayload> CODEC = new StreamCodec<>() {
        @Override
        public PolicyRequestPayload decode(RegistryFriendlyByteBuf buffer) {
            return INSTANCE;
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, PolicyRequestPayload payload) {
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
