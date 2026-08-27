package dev.rinchan.skillrespecpill.mixin;

import dev.rinchan.skillrespecpill.state.FabricDefaultGrantDataHolder;
import dev.rinchan.skillrespecpill.state.PlayerDefaultGrantData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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
    private void skillRespecPill$readDefaultGrantData(ValueInput input, CallbackInfo callback) {
        skillRespecPill$defaultGrantData = input.read(
                        PlayerDefaultGrantData.ROOT_KEY,
                        CompoundTag.CODEC)
                .map(CompoundTag::copy)
                .orElseGet(CompoundTag::new);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void skillRespecPill$writeDefaultGrantData(ValueOutput output, CallbackInfo callback) {
        output.store(
                PlayerDefaultGrantData.ROOT_KEY,
                CompoundTag.CODEC,
                skillRespecPill$defaultGrantData.copy());
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
