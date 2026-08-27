package dev.rinchan.skillrespecpill.fabric;

import dev.rinchan.skillrespecpill.SkillRespecPill;
import dev.rinchan.skillrespecpill.policy.PolicyRepository;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.ProfilerFiller;

public final class FabricPolicyReload {
    private FabricPolicyReload() {
    }

    public static void register() {
        ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(Listener.ID, new Listener());
    }

    private static final class Listener extends SimplePreparableReloadListener<Unit> {
        private static final Identifier ID = Identifier.fromNamespaceAndPath(
                SkillRespecPill.MOD_ID, "policies");

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
