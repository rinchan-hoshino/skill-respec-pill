package dev.rinchan.skillrespecpill.network;

import dev.rinchan.skillrespecpill.SkillRespecPill;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ResetPagePayload(ResourceLocation categoryId) implements CustomPacketPayload {
    public static final Type<ResetPagePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(SkillRespecPill.MOD_ID, "reset_page"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ResetPagePayload> CODEC = new StreamCodec<>() {
        @Override
        public ResetPagePayload decode(RegistryFriendlyByteBuf buffer) {
            return new ResetPagePayload(buffer.readResourceLocation());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, ResetPagePayload payload) {
            buffer.writeResourceLocation(payload.categoryId());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
