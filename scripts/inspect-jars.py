#!/usr/bin/env python3
import hashlib
import json
import re
import subprocess
from pathlib import Path
from zipfile import ZipFile

ROOT = Path(__file__).resolve().parents[1]
PROPERTIES = dict(
    line.split("=", 1)
    for line in (ROOT / "gradle.properties").read_text().splitlines()
    if line and not line.startswith("#") and "=" in line
)
VERSION = PROPERTIES["mod_version"]
JARS = {
    "fabric": ROOT / f"fabric/build/libs/skill_respec_pill-fabric-{VERSION}.jar",
    "neoforge": ROOT / f"neoforge/build/libs/skill_respec_pill-neoforge-{VERSION}.jar",
}
COMMON_ENTRIES = {
    "dev/rinchan/skillrespecpill/api/SkillRespecPillApi.class",
    "dev/rinchan/skillrespecpill/graph/SkillGraph.class",
    "dev/rinchan/skillrespecpill/client/NodeCostTooltip.class",
    "dev/rinchan/skillrespecpill/client/NodeCostTooltipComponent.class",
    "dev/rinchan/skillrespecpill/client/NodeCostTooltipSequence.class",
    "dev/rinchan/skillrespecpill/service/RespecService.class",
    "dev/rinchan/skillrespecpill/mixin/ClientTextTooltipAccessor.class",
    "dev/rinchan/skillrespecpill/mixin/GuiGraphicsMixin.class",
    "dev/rinchan/skillrespecpill/mixin/SkillsModMixin.class",
    "dev/rinchan/skillrespecpill/mixin/SkillsScreenMixin.class",
    "assets/skill_respec_pill/lang/zh_cn.json",
    "skill_respec_pill.mixins.json",
}

def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)

for loader, jar in JARS.items():
    require(jar.is_file(), f"missing {loader} JAR: {jar}")
    with ZipFile(jar) as archive:
        names = set(archive.namelist())
        missing = COMMON_ENTRIES - names
        require(not missing, f"{loader} JAR missing common entries: {sorted(missing)}")
        require(not any(name.startswith("wmf") or "/wmf" in name for name in names),
                f"{loader} JAR contains forbidden WMF scope")
        zh = json.loads(archive.read("assets/skill_respec_pill/lang/zh_cn.json"))
        en = json.loads(archive.read("assets/skill_respec_pill/lang/en_us.json"))
        for stale_key in (
                "tooltip.skill_respec_pill.node_cost",
                "tooltip.skill_respec_pill.batch_unlock",
                "tooltip.skill_respec_pill.cascade_refund"):
            require(stale_key not in zh and stale_key not in en,
                    f"{loader} keeps textual cost copy {stale_key}")
        screen_bytes = archive.read("dev/rinchan/skillrespecpill/mixin/SkillsScreenMixin.class")
        require(b"NodeCostTooltipSequence" in screen_bytes,
                f"{loader} screen misses the custom cost sequence")
        require(b"tooltip.skill_respec_pill.node_cost" not in screen_bytes,
                f"{loader} screen still renders a textual cost line")
        component_bytes = archive.read(
            "dev/rinchan/skillrespecpill/client/NodeCostTooltipComponent.class")
        for marker in (b"BADGE_SCALE", b"BADGE_Y_OFFSET", b"LIGHT_GRAY", b"RED", b"GREEN"):
            require(marker in component_bytes, f"{loader} custom renderer misses {marker!r}")
        for unicode_badge in "⁰¹²³⁴⁵⁶⁷⁸⁹⁺⁻⁽⁾":
            require(unicode_badge.encode() not in component_bytes + screen_bytes,
                    f"{loader} still encodes a fake superscript badge")
        for stale_key in (
                "tooltip.skill_respec_pill.forced",
                "tooltip.skill_respec_pill.cascade_disabled",
                "tooltip.skill_respec_pill.invalid_graph"):
            require(stale_key not in zh and stale_key not in en,
                    f"{loader} keeps transient error tooltip {stale_key}")
        service_bytes = archive.read("dev/rinchan/skillrespecpill/service/RespecService.class")
        for message_key in (
                b"message.skill_respec_pill.insufficient_points",
                b"message.skill_respec_pill.forced_skill",
                b"message.skill_respec_pill.cascade_disabled",
                b"message.skill_respec_pill.action_denied",
                b"message.skill_respec_pill.action_failed"):
            require(message_key in service_bytes, f"{loader} service misses chat error {message_key!r}")
        if loader == "fabric":
            require("fabric.mod.json" in names, "Fabric metadata missing")
            require("skill_respec_pill.fabric.mixins.json" in names, "Fabric persistence mixin metadata missing")
            require("META-INF/neoforge.mods.toml" not in names, "Fabric JAR contains NeoForge metadata")
            metadata = json.loads(archive.read("fabric.mod.json"))
            require(metadata["id"] == "skill_respec_pill", "Fabric mod id changed")
            require(metadata["name"] == "Skill Respec Pill", "Fabric display identity changed")
            require(metadata["environment"] == "*", "Fabric is not client+server")
            require(metadata["depends"]["fabric-api"] == "*", "Fabric API is not mandatory")
            require(metadata["depends"]["puffish_skills"] == "=0.18.3", "Fabric Puffish version is not exact")
            require(metadata["depends"]["rinlib"] == "=1.0.0+1.21.1", "Fabric RinLib version is not exact")
        else:
            require("META-INF/neoforge.mods.toml" in names, "NeoForge metadata missing")
            require("fabric.mod.json" not in names, "NeoForge JAR contains Fabric metadata")
            metadata = archive.read("META-INF/neoforge.mods.toml").decode()
            require('modId="skill_respec_pill"' in metadata, "NeoForge mod id changed")
            require('displayName="Skill Respec Pill"' in metadata, "NeoForge display identity changed")
            require('modId="puffish_skills"' in metadata and 'versionRange="[0.18.3]"' in metadata,
                    "NeoForge Puffish dependency is not exact")
            require('modId="rinlib"' in metadata and 'versionRange="[1.0.0]"' in metadata,
                    "NeoForge RinLib runtime version is not exact")
    bytecode = subprocess.run(
        ["javap", "-p", "-c", "-classpath", str(jar),
         "dev.rinchan.skillrespecpill.service.RespecService"],
        check=True, capture_output=True, text=True).stdout
    if loader == "fabric" and PROPERTIES["minecraft_version"] == "1.21.1":
        match = re.search(
            r"private static void sendFailure\(net.minecraft.class_3222, net.minecraft.class_2561\);"
            r"\s+Code:(.*?)(?=\n  (?:public|private|protected)|\Z)",
            bytecode, re.DOTALL)
        require(match is not None, f"{loader} JAR lacks the remapped chat delivery helper")
        delivery = match.group(1)
        require("iconst_0" in delivery and "(Lnet/minecraft/class_2561;Z)V" in delivery,
                f"{loader} 1.21.1 errors are not persistent chat messages")
    else:
        match = re.search(
            r"private static void sendFailure\([^;]+Component\);\s+Code:(.*?)(?=\n  (?:public|private|protected)|\Z)",
            bytecode, re.DOTALL)
        require(match is not None, f"{loader} JAR lacks the centralized chat delivery helper")
        delivery = match.group(1)
        if PROPERTIES["minecraft_version"] == "1.21.1":
            require("displayClientMessage" in delivery and "iconst_0" in delivery,
                    f"{loader} 1.21.1 errors are not persistent chat messages")
        else:
            require("sendSystemMessage" in delivery,
                    f"{loader} modern errors are not persistent chat messages")
    digest = hashlib.sha256(jar.read_bytes()).hexdigest()
    print(f"{loader}: {jar.relative_to(ROOT)} sha256={digest}")

print("static JAR contracts: PASS")
