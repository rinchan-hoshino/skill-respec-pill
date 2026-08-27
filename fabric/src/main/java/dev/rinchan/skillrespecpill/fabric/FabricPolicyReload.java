package dev.rinchan.skillrespecpill.fabric;

import dev.rinchan.skillrespecpill.SkillRespecPill;
import dev.rinchan.skillrespecpill.policy.PolicyRepository;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.ProfilerFiller;

public final class FabricPolicyReload {
    private FabricPolicyReload() {
    }

    public static void register() {
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new Listener());
    }

    private static final class Listener extends SimplePreparableReloadListener<Unit>
            implements IdentifiableResourceReloadListener {
        private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
                SkillRespecPill.MOD_ID, "policies");

        @Override
        public ResourceLocation getFabricId() {
            return ID;
        }

        @Override
        protected Unit prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
            PolicyRepository.loadAll(resourceManager);
            return Unit.INSTANCE;
        }

        @Override
        protected void apply(Unit prepared, ResourceManager resourceManager, ProfilerFiller profiler) {
            SkillRespecPill.LOGGER.info("Reloaded skill respec policies");
        }
    }
}
