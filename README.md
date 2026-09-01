# Skill Respec Pill

<!-- PROJECT_PAGE_START -->
A configurable server-side respec system for SkillTree: buy prerequisite chains, refund dependent skills safely, or reset a whole page under datapack-controlled policy.


## Player actions

- **Respec Pill:** buys a target skill and any missing prerequisites.
- **Cascade Pill:** refunds a skill together with learned dependents.
- **Oblivion Pill:** resets a whole skill page and refunds spent points.

## Server rules

All mutations are server-authoritative. Datapacks can control allowed pages, costs, refunds, binding requirements and transaction limits. A failed transaction changes nothing.

## Compatibility

Available for the supported Fabric and NeoForge versions listed on the platform Versions page. SkillTree and RinLib are required; Fabric builds also require Fabric API.

## Download and support

Use the platform Versions page for exact game-version and loader files. Report reproducible problems through the project GitHub Issues page.
<!-- PROJECT_PAGE_END -->

---

## Additional technical details

## 架构

- `common/`：唯一的公开 API、纯图/状态/策略代码、Puffish Skills 语义服务、共享 Mixin 与资源。
- `fabric/`：Fabric 入口、配置、资源重载、登录/重生事件、玩家 NBT 持久化与 Fabric Networking。
- `neoforge/`：NeoForge 入口、`ModConfigSpec`、玩家事件/持久化与 NeoForge payload 注册。
- 根项目：只编译和测试不依赖 Minecraft 的纯代码；两个加载器分别编译同一份 common 源码。

公开 API 保持在 `dev.rinchan.skillrespecpill.api.SkillRespecPillApi`。

## 运行依赖

两个加载器均强制要求：

- Minecraft 1.21.1
- Puffish Skills 0.18.3
  - Fabric Curse file `8547653`
  - NeoForge Curse file `8547654`
- RinLib 1.0.0 对应 1.21.1 的精确加载器产物
- Fabric 额外强制要求 Fabric API

## 配置

唯一开关为 `cascade_refund_enabled`，默认 `true`：

- Fabric：`config/skill_respec_pill.properties`
- NeoForge：`config/skill_respec_pill-server.toml`

策略数据包路径自1.0起保持稳定：

```text
data/<namespace>/skill_respec_pill/policies/*.json
```

示例：

```json
{
  "category": "example:combat",
  "starting_points": 3,
  "default_enabled": ["root", "starter"],
  "forced_enabled": ["root"]
}
```

## 允许的本地验证

所有 Gradle 命令通过 `rin-gradle ./gradlew ...` 执行：

```bash
rin-gradle ./gradlew quickCompile
rin-gradle ./gradlew :test --tests 'dev.rinchan.skillrespecpill.*'
rin-gradle ./gradlew dualBuild
python3 scripts/inspect-jars.py
```

这些检查分别覆盖快速双端编译、聚焦纯单元/源码契约、双加载器 JAR 构建和静态 JAR 契约；不启动 Minecraft、客户端、服务端或 GameTest。
