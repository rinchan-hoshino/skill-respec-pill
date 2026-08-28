package dev.rinchan.skillrespecpill;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

final class StaticContractsTest {
    @Test
    void sharedMixinsOwnAuthoritativeSemanticsAndNativeButtonHooks() throws IOException {
        String server = read("common/src/main/java/dev/rinchan/skillrespecpill/mixin/SkillsModMixin.java");
        String screen = read("common/src/main/java/dev/rinchan/skillrespecpill/mixin/SkillsScreenMixin.java");
        String mixins = read("common/src/main/resources/skill_respec_pill.mixins.json");

        assertTrue(server.contains("method = \"watchNewPoints\""));
        assertTrue(server.contains("PointMutationContext.isRefunding"));
        assertTrue(server.contains("priority = 1100"));
        assertTrue(server.contains("method = \"tryUnlockSkill\""));
        assertTrue(server.contains("boolean force"));
        assertTrue(server.contains("method = \"lockSkill\""));
        assertTrue(server.contains("require = 1"));
        assertTrue(screen.contains("lambda$drawContentWithCategory$5"));
        assertTrue(screen.contains("state == Skill.State.LOCKED ? Skill.State.AVAILABLE : state"));
        assertTrue(screen.contains("lambda$drawContentWithCategory$3"));
        assertTrue(screen.contains("BatchPreview"));
        assertTrue(screen.contains("Button.builder"));
        assertTrue(screen.contains("ClientNetworking.send"));
        assertTrue(screen.contains("method = \"extractRenderState\""));
        assertTrue(screen.contains("GuiGraphicsExtractor"));
        assertTrue(screen.contains("setTooltipForNextFrame"));
        assertTrue(screen.contains("at = @At(\"TAIL\")"));
        assertTrue(screen.contains("addRenderableWidget"));
        assertTrue(screen.contains("resetButton.extractRenderState"));
        assertTrue(screen.contains("method = \"mouseClicked\""));
        assertTrue(screen.contains("cancellable = true"));
        assertTrue(screen.contains("resetButton.mouseClicked"));
        assertFalse(screen.contains("net.neoforged"));
        assertFalse(screen.contains("net.fabricmc"));
        assertTrue(mixins.contains("SkillsModAccessor"));
    }

    @Test
    void insufficientPointsIsSilentAndMutationHappensOnlyAfterServerPreflight() throws IOException {
        String service = read("common/src/main/java/dev/rinchan/skillrespecpill/service/RespecService.java");
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
    void bothLoadersUseExactRequiredArtifactsAndMetadata() throws IOException {
        String properties = read("gradle.properties");
        assertTrue(properties.contains("minecraft_version=26.1.2"));
        assertTrue(properties.contains("java_version=25"));
        assertTrue(properties.contains("puffish_skills_fabric_file=8547681"));
        assertTrue(properties.contains("puffish_skills_neoforge_file=8547682"));
        assertTrue(properties.contains("rinlib_fabric_version=1.0.0+26.1.2-fabric"));
        assertTrue(properties.contains("rinlib_neoforge_version=1.0.0+26.1.2-neoforge"));
        assertTrue(properties.contains("rinlib_runtime_version=1.0.0+26.1.2"));

        String fabricBuild = read("fabric/build.gradle");
        String neoBuild = read("neoforge/build.gradle");
        assertTrue(fabricBuild.contains("puffish_skills_fabric_file"));
        assertTrue(fabricBuild.contains("rinlib_fabric_version"));
        assertTrue(neoBuild.contains("puffish_skills_neoforge_file"));
        assertTrue(neoBuild.contains("rinlib_neoforge_version"));

        var fabric = JsonParser.parseString(read("fabric/src/main/resources/fabric.mod.json")).getAsJsonObject();
        assertEquals("${mod_id}", fabric.get("id").getAsString());
        assertEquals("${mod_name}", fabric.get("name").getAsString());
        assertEquals("*", fabric.get("environment").getAsString());
        assertEquals(">=25", fabric.getAsJsonObject("depends").get("java").getAsString());
        var depends = fabric.getAsJsonObject("depends");
        for (String dependency : List.of("fabric-api", "puffish_skills", "rinlib")) {
            assertTrue(depends.has(dependency));
        }
        assertEquals("=${puffish_skills_version}", depends.get("puffish_skills").getAsString());
        assertEquals("=${rinlib_runtime_version}", depends.get("rinlib").getAsString());

        String mods = read("neoforge/src/main/templates/META-INF/neoforge.mods.toml");
        assertTrue(mods.contains("displayName=\"${mod_name}\""));
        assertTrue(mods.contains("license=\"${mod_license}\""));
        assertTrue(mods.contains("versionRange=\"[${rinlib_runtime_version}]\""));
        assertTrue(mods.contains("versionRange=\"[${puffish_skills_version}]\""));
        assertEquals(2, count(mods, "side=\"BOTH\" # mandatory-runtime"));
    }

    @Test
    void fabricOwnsConfigReloadLifecyclePersistenceAndMandatoryNetworking() throws IOException {
        String config = read("fabric/src/main/java/dev/rinchan/skillrespecpill/SkillRespecConfig.java");
        assertTrue(config.contains("cascadeRefundEnabled = true"));
        assertTrue(config.contains("cascade_refund_enabled"));

        String reload = read("fabric/src/main/java/dev/rinchan/skillrespecpill/fabric/FabricPolicyReload.java");
        assertTrue(reload.contains("PackType.SERVER_DATA"));
        assertTrue(reload.contains("ResourceLoader.get(PackType.SERVER_DATA)"));
        assertTrue(reload.contains("registerReloadListener(Listener.ID"));
        assertTrue(reload.contains("PolicyRepository.loadAll(resourceManager)"));

        String lifecycle = read("fabric/src/main/java/dev/rinchan/skillrespecpill/fabric/FabricPlayerPolicyLifecycle.java");
        assertTrue(lifecycle.contains("ServerPlayConnectionEvents.JOIN"));
        assertTrue(lifecycle.contains("ServerPlayerEvents.COPY_FROM"));
        assertTrue(lifecycle.contains("ServerPlayerEvents.AFTER_RESPAWN"));

        String persistence = read("fabric/src/main/java/dev/rinchan/skillrespecpill/mixin/PlayerDefaultGrantDataMixin.java");
        assertTrue(persistence.contains("readAdditionalSaveData"));
        assertTrue(persistence.contains("addAdditionalSaveData"));
        assertTrue(persistence.contains("ValueInput"));
        assertTrue(persistence.contains("ValueOutput"));
        assertTrue(persistence.contains("CompoundTag.CODEC"));

        String server = read("fabric/src/main/java/dev/rinchan/skillrespecpill/network/SkillRespecNetworking.java");
        String client = read("fabric/src/main/java/dev/rinchan/skillrespecpill/fabric/FabricClientNetworking.java");
        assertTrue(server.contains("PayloadTypeRegistry.serverboundPlay"));
        assertTrue(server.contains("PayloadTypeRegistry.clientboundPlay"));
        assertTrue(server.contains("Server reset packet failed"));
        assertTrue(server.contains("Server policy-request packet failed"));
        assertTrue(server.contains("Server policy-sync packet failed"));
        assertTrue(client.contains("Client policy-sync packet failed"));
    }

    @Test
    void neoConfigAndPacketContractsRemainPresent() throws IOException {
        String config = read("neoforge/src/main/java/dev/rinchan/skillrespecpill/SkillRespecConfig.java");
        assertEquals(1, count(config, ".define("));
        assertTrue(config.contains("cascade_refund_enabled"));
        String network = read("neoforge/src/main/java/dev/rinchan/skillrespecpill/network/SkillRespecNetworking.java");
        assertTrue(network.contains("playToServer(ResetPagePayload.TYPE"));
        assertTrue(network.contains("playToServer(PolicyRequestPayload.TYPE"));
        assertTrue(network.contains("playToClient(PolicySyncPayload.TYPE"));
        assertTrue(network.contains("Server reset packet failed"));
        assertTrue(network.contains("Client policy-sync packet failed"));
        String client = read("neoforge/src/main/java/dev/rinchan/skillrespecpill/platform/ClientNetworking.java");
        assertTrue(client.contains("ClientPacketDistributor.sendToServer"));
    }

    @Test
    void schemaStateAndServerSafeguardsRemainExact() throws IOException {
        String policy = read("common/src/main/java/dev/rinchan/skillrespecpill/policy/SkillPolicy.java");
        assertTrue(policy.contains("Set.of(\"category\", \"default_enabled\", \"forced_enabled\")"));
        assertFalse(policy.contains("starting_points"));

        String state = read("common/src/main/java/dev/rinchan/skillrespecpill/state/DefaultGrantState.java");
        assertTrue(state.contains("grantedByCategory"));
        assertTrue(state.contains("getOrDefault(category"));

        String service = read("common/src/main/java/dev/rinchan/skillrespecpill/service/RespecService.java");
        assertTrue(service.contains("skill.getState(player) == Skill.State.EXCLUDED"));
        assertTrue(service.contains("cascadeRefundEnabled"));
        assertTrue(service.contains("plan.nodeIds().stream().anyMatch(forced::contains)"));
        assertTrue(service.contains("restoreDeclaredBaseline"));
        assertTrue(service.contains("Action.CASCADE_REFUND"));
        assertTrue(service.contains("Action.PAGE_RESET"));
        assertTrue(service.contains("sendSystemMessage"));
    }

    @Test
    void publicApiAndProductScopeHaveOneCommonOwner() throws IOException {
        assertTrue(Files.isRegularFile(root().resolve(
                "common/src/main/java/dev/rinchan/skillrespecpill/api/SkillRespecPillApi.java")));
        assertFalse(Files.exists(root().resolve(
                "fabric/src/main/java/dev/rinchan/skillrespecpill/api/SkillRespecPillApi.java")));
        assertFalse(Files.exists(root().resolve(
                "neoforge/src/main/java/dev/rinchan/skillrespecpill/api/SkillRespecPillApi.java")));

        StringBuilder all = new StringBuilder();
        for (String sourceRoot : List.of("common/src/main", "fabric/src/main", "neoforge/src/main")) {
            try (var paths = Files.walk(root().resolve(sourceRoot))) {
                paths.filter(Files::isRegularFile).forEach(path -> {
                    try {
                        all.append('\n').append(Files.readString(path));
                    } catch (IOException exception) {
                        throw new RuntimeException(exception);
                    }
                });
            }
        }
        String lower = all.toString().toLowerCase();
        for (String forbidden : Set.of(
                "default_point_subsidy", "tree_revision", "migration", "respawn_bed",
                "spawn_check", "registercommands", "respec_item", "recipe", "gametest",
                "wmf_skills:")) {
            assertFalse(lower.contains(forbidden), forbidden);
        }
        var zh = JsonParser.parseString(read(
                "common/src/main/resources/assets/skill_respec_pill/lang/zh_cn.json")).getAsJsonObject();
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
