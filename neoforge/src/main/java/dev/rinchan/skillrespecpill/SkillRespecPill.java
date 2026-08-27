package dev.rinchan.skillrespecpill;

import dev.rinchan.skillrespecpill.network.SkillRespecNetworking;
import dev.rinchan.skillrespecpill.service.PlayerPolicyLifecycle;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(SkillRespecPill.MOD_ID)
public final class SkillRespecPill {
    public static final String MOD_ID = "skill_respec_pill";
    public static final Logger LOGGER = LoggerFactory.getLogger("技能后悔药");

    public SkillRespecPill(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.SERVER, SkillRespecConfig.SPEC);
        modBus.addListener(SkillRespecNetworking::register);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, PlayerPolicyLifecycle::onLogin);
        NeoForge.EVENT_BUS.addListener(PlayerPolicyLifecycle::onClone);
    }
}
