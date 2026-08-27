package dev.rinchan.skillrespecpill.state;

import dev.rinchan.skillrespecpill.SkillRespecPill;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.entity.player.Player;

public final class PlayerDefaultGrantData {
    public static final String ROOT_KEY = SkillRespecPill.MOD_ID + ":default_grants";

    private PlayerDefaultGrantData() {
    }

    public static DefaultGrantState read(Player player) {
        var state = new DefaultGrantState();
        CompoundTag root = holder(player).skillRespecPill$getDefaultGrantData();
        for (String category : root.keySet()) {
            ListTag nodes = root.getListOrEmpty(category);
            for (int index = 0; index < nodes.size(); index++) {
                nodes.getString(index).ifPresent(node -> state.markGranted(category, node));
            }
        }
        return state;
    }

    public static void write(Player player, DefaultGrantState state) {
        var root = new CompoundTag();
        state.snapshot().entrySet().stream().sorted(MapEntryComparator.INSTANCE).forEach(entry -> {
            var nodes = new ListTag();
            entry.getValue().stream().sorted().forEach(node -> nodes.add(StringTag.valueOf(node)));
            root.put(entry.getKey(), nodes);
        });
        holder(player).skillRespecPill$setDefaultGrantData(root);
    }

    public static void copy(Player source, Player target) {
        holder(target).skillRespecPill$setDefaultGrantData(
                holder(source).skillRespecPill$getDefaultGrantData().copy());
    }

    private static FabricDefaultGrantDataHolder holder(Player player) {
        return (FabricDefaultGrantDataHolder) player;
    }

    private enum MapEntryComparator implements java.util.Comparator<java.util.Map.Entry<String, java.util.Set<String>>> {
        INSTANCE;

        @Override
        public int compare(
                java.util.Map.Entry<String, java.util.Set<String>> left,
                java.util.Map.Entry<String, java.util.Set<String>> right) {
            return left.getKey().compareTo(right.getKey());
        }
    }
}
