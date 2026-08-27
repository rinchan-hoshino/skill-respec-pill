package dev.rinchan.skillrespecpill.network;

import dev.rinchan.skillrespecpill.SkillRespecPill;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record PolicySyncPayload(
        Map<Identifier, Set<String>> forcedByCategory,
        boolean cascadeRefundEnabled) implements CustomPacketPayload {
    public static final Type<PolicySyncPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(SkillRespecPill.MOD_ID, "policy_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PolicySyncPayload> CODEC = new StreamCodec<>() {
        @Override
        public PolicySyncPayload decode(RegistryFriendlyByteBuf buffer) {
            int categories = buffer.readVarInt();
            var values = new HashMap<Identifier, Set<String>>();
            for (int index = 0; index < categories; index++) {
                Identifier category = buffer.readIdentifier();
                int nodes = buffer.readVarInt();
                var forced = new TreeSet<String>();
                for (int node = 0; node < nodes; node++) forced.add(buffer.readUtf());
                values.put(category, Set.copyOf(forced));
            }
            return new PolicySyncPayload(Map.copyOf(values), buffer.readBoolean());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, PolicySyncPayload payload) {
            var categories = new TreeSet<>(payload.forcedByCategory().keySet());
            buffer.writeVarInt(categories.size());
            for (Identifier category : categories) {
                buffer.writeIdentifier(category);
                var nodes = new TreeSet<>(payload.forcedByCategory().get(category));
                buffer.writeVarInt(nodes.size());
                nodes.forEach(buffer::writeUtf);
            }
            buffer.writeBoolean(payload.cascadeRefundEnabled());
        }
    };

    public PolicySyncPayload {
        var copied = new HashMap<Identifier, Set<String>>();
        forcedByCategory.forEach((category, nodes) -> copied.put(category, Set.copyOf(nodes)));
        forcedByCategory = Map.copyOf(copied);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
