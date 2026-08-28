#!/usr/bin/env python3
import hashlib
import json
from pathlib import Path
from zipfile import ZipFile

ROOT = Path(__file__).resolve().parents[1]
JARS = {
    "fabric": ROOT / "fabric/build/libs/skill_respec_pill-fabric-1.0.1.jar",
    "neoforge": ROOT / "neoforge/build/libs/skill_respec_pill-neoforge-1.0.1.jar",
}
COMMON_ENTRIES = {
    "dev/rinchan/skillrespecpill/api/SkillRespecPillApi.class",
    "dev/rinchan/skillrespecpill/graph/SkillGraph.class",
    "dev/rinchan/skillrespecpill/service/RespecService.class",
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
    digest = hashlib.sha256(jar.read_bytes()).hexdigest()
    print(f"{loader}: {jar.relative_to(ROOT)} sha256={digest}")

print("static JAR contracts: PASS")
