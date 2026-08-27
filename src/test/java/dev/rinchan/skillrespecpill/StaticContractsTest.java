package dev.rinchan.skillrespecpill;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import org.junit.jupiter.api.Test;

final class StaticContractsTest {
    @Test
    void mixinsOwnAuthoritativeClickForcedProtectionVisibilityTooltipAndNativeButtonHooks() throws IOException {
        String server = read("src/main/java/dev/rinchan/skillrespecpill/mixin/SkillsModMixin.java");
        String screen = read("src/main/java/dev/rinchan/skillrespecpill/mixin/SkillsScreenMixin.java");
        String mixins = read("src/main/resources/skill_respec_pill.mixins.json");

        assertTrue(server.contains("method = \"tryUnlockSkill\""));
        assertTrue(server.contains("boolean force"));
        assertTrue(server.contains("method = \"lockSkill\""));
        assertTrue(server.contains("require = 1"));
        assertTrue(screen.contains("lambda$drawContentWithCategory$22"));
        assertTrue(screen.contains("Skill.State.AVAILABLE"));
        assertTrue(screen.contains("lambda$drawContentWithCategory$21"));
        assertTrue(screen.contains("BatchPreview"));
        assertTrue(screen.contains("Button.builder"));
        assertTrue(screen.contains("ResetPagePayload"));
        assertTrue(screen.contains("method = \"render\""));
        assertTrue(screen.contains("at = @At(\"HEAD\")"));
        assertTrue(screen.contains("addRenderableWidget"));
        assertFalse(screen.contains("resetButton.render"));
        assertFalse(screen.contains("method = \"mouseClicked\""));
        assertTrue(mixins.contains("SkillsModAccessor"));
    }

    @Test
    void insufficientPointsIsSilentAndMutationHappensOnlyAfterServerPreflight() throws IOException {
        String service = read("src/main/java/dev/rinchan/skillrespecpill/service/RespecService.java");
        int check = service.indexOf("pointsLeft < plan.points()");
        int mutation = service.indexOf("unlockTopologically");

        assertTrue(check >= 0 && mutation > check);
        String insufficientBranch = service.substring(check, mutation);
        assertFalse(insufficientBranch.contains("displayClientMessage"));
        assertFalse(insufficientBranch.contains("sendSystemMessage"));
        assertFalse(insufficientBranch.contains("toast"));
        assertFalse(insufficientBranch.contains("actionbar"));
    }

    @Test
    void metadataRequiresBothRuntimeDependenciesOnBothSidesAndOneConfigBoolean() throws IOException {
        String mods = read("src/main/resources/META-INF/neoforge.mods.toml");
        assertTrue(mods.contains("displayName=\"技能后悔药\""));
        assertTrue(mods.contains("license=\"GPL-3.0\""));
        for (String dependency : Set.of("rinlib", "puffish_skills")) {
            assertTrue(mods.contains("modId=\"" + dependency + "\""));
        }
        assertEquals(2, count(mods, "side=\"BOTH\" # mandatory-runtime"));
        assertTrue(read("build.gradle").contains("LICENSE_${mod_id}"));

        String config = read("src/main/java/dev/rinchan/skillrespecpill/SkillRespecConfig.java");
        assertEquals(1, count(config, ".define("));
        assertTrue(config.contains("cascade_refund_enabled"));
    }

    @Test
    void resourcesAndSourceContainNoForbiddenProductScope() throws IOException {
        String all = Files.walk(root().resolve("src/main"))
                .filter(Files::isRegularFile)
                .map(path -> {
                    try { return Files.readString(path); }
                    catch (IOException exception) { throw new RuntimeException(exception); }
                })
                .reduce("", (left, right) -> left + "\n" + right).toLowerCase();

        for (String forbidden : Set.of(
                "default_point_subsidy", "tree_revision", "migration", "respawn_bed",
                "spawn_check", "registercommands", "respec_item", "recipe", "gametest",
                "wmf_skills:")) {
            assertFalse(all.contains(forbidden), forbidden);
        }
        var zh = JsonParser.parseString(read(
                "src/main/resources/assets/skill_respec_pill/lang/zh_cn.json")).getAsJsonObject();
        assertEquals("重置本页", zh.get("screen.skill_respec_pill.reset_page").getAsString());
    }

    private static int count(String value, String needle) {
        int count = 0;
        for (int index = 0; (index = value.indexOf(needle, index)) >= 0; index += needle.length()) count++;
        return count;
    }

    private static String read(String relative) throws IOException {
        return Files.readString(root().resolve(relative));
    }

    private static Path root() {
        Path current = Path.of("").toAbsolutePath();
        while (current != null && !Files.isRegularFile(current.resolve("settings.gradle"))) {
            current = current.getParent();
        }
        if (current == null) throw new IllegalStateException("Cannot locate project root");
        return current;
    }
}
