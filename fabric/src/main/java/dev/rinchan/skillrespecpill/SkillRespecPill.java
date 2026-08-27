package dev.rinchan.skillrespecpill;

import dev.rinchan.skillrespecpill.fabric.FabricPlayerPolicyLifecycle;
import dev.rinchan.skillrespecpill.fabric.FabricPolicyReload;
import dev.rinchan.skillrespecpill.network.SkillRespecNetworking;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SkillRespecPill implements ModInitializer {
    public static final String MOD_ID = "skill_respec_pill";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        SkillRespecConfig.load();
        SkillRespecNetworking.registerServer();
        FabricPlayerPolicyLifecycle.register();
        FabricPolicyReload.register();
    }
}
