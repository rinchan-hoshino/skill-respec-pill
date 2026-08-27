package dev.rinchan.skillrespecpill.fabric;

import net.fabricmc.api.ClientModInitializer;

public final class SkillRespecPillFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FabricClientNetworking.register();
    }
}
