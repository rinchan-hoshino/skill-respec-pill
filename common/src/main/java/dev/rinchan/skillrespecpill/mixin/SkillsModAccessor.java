package dev.rinchan.skillrespecpill.mixin;

import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.config.CategoryConfig;
import net.puffish.skillsmod.util.ChangeListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = SkillsMod.class, remap = false)
public interface SkillsModAccessor {
    @Accessor(value = "categories", remap = false)
    ChangeListener<Optional<Map<ResourceLocation, CategoryConfig>>> skillRespecPill$getCategories();
}
