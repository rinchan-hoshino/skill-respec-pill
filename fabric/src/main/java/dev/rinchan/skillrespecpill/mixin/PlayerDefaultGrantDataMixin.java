package dev.rinchan.skillrespecpill.mixin;

import dev.rinchan.skillrespecpill.state.FabricDefaultGrantDataHolder;
import dev.rinchan.skillrespecpill.state.PlayerDefaultGrantData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerDefaultGrantDataMixin implements FabricDefaultGrantDataHolder {
    @Unique
    private CompoundTag skillRespecPill$defaultGrantData = new CompoundTag();

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void skillRespecPill$readDefaultGrantData(CompoundTag tag, CallbackInfo callback) {
        if (tag.contains(PlayerDefaultGrantData.ROOT_KEY, Tag.TAG_COMPOUND)) {
            skillRespecPill$defaultGrantData = tag.getCompound(PlayerDefaultGrantData.ROOT_KEY).copy();
        } else {
            skillRespecPill$defaultGrantData = new CompoundTag();
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void skillRespecPill$writeDefaultGrantData(CompoundTag tag, CallbackInfo callback) {
        tag.put(PlayerDefaultGrantData.ROOT_KEY, skillRespecPill$defaultGrantData.copy());
    }

    @Override
    public CompoundTag skillRespecPill$getDefaultGrantData() {
        return skillRespecPill$defaultGrantData;
    }

    @Override
    public void skillRespecPill$setDefaultGrantData(CompoundTag data) {
        skillRespecPill$defaultGrantData = data.copy();
    }
}
