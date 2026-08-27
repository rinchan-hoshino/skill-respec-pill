package dev.rinchan.skillrespecpill.mixin;

import dev.rinchan.skillrespecpill.service.PointMutationContext;
import dev.rinchan.skillrespecpill.service.RespecService;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.config.CategoryConfig;
import net.puffish.skillsmod.server.data.CategoryData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SkillsMod.class, remap = false, priority = 1100)
public abstract class SkillsModMixin {
    @Inject(method = "watchNewPoints", at = @At("HEAD"), cancellable = true, remap = false, require = 1)
    private void skillRespecPill$suppressRefundPointAnnouncement(
            ServerPlayer player,
            CategoryConfig category,
            CategoryData categoryData,
            boolean silent,
            Runnable operation,
            CallbackInfo callback) {
        if (PointMutationContext.isRefunding(player.getUUID(), category.id().toString())) {
            operation.run();
            callback.cancel();
        }
    }

    @Inject(method = "tryUnlockSkill", at = @At("HEAD"), cancellable = true, remap = false, require = 1)
    private void skillRespecPill$handleNodeClick(
            ServerPlayer player,
            ResourceLocation categoryId,
            String nodeId,
            boolean force,
            CallbackInfo callback) {
        if (!force && RespecService.handleNodeClick(player, categoryId, nodeId)) callback.cancel();
    }

    @Inject(method = "lockSkill", at = @At("HEAD"), cancellable = true, remap = false, require = 1)
    private void skillRespecPill$protectForcedNode(
            ServerPlayer player,
            ResourceLocation categoryId,
            String nodeId,
            CallbackInfo callback) {
        if (RespecService.isForced(player, categoryId, nodeId)) callback.cancel();
    }

    @Inject(method = "resetSkills", at = @At("HEAD"), cancellable = true, remap = false, require = 1)
    private void skillRespecPill$replaceResetWithProtectedReset(
            ServerPlayer player,
            ResourceLocation categoryId,
            CallbackInfo callback) {
        RespecService.resetPage(player, categoryId);
        callback.cancel();
    }
}
