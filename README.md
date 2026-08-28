# Skill Respec Pill

中文名：技能后悔药

面向 Minecraft 26.1.2、Java 25 的 Fabric + NeoForge 多加载器模组，为 Puffish Skills 0.18.3 提供服务端权威的批量前置购买、级联退还和整页重置。

## 架构

- `common/`：唯一的公开 API、纯图/状态/策略代码、Puffish Skills 语义服务、共享 Mixin 与资源。
- `fabric/`：Fabric 入口、配置、资源重载、登录/重生事件、玩家 NBT 持久化与 Fabric Networking。
- `neoforge/`：NeoForge 入口、`ModConfigSpec`、玩家事件/持久化与 NeoForge payload 注册。
- 根项目：只编译和测试不依赖 Minecraft 的纯代码；两个加载器分别编译同一份 common 源码。

公开 API 保持在 `dev.rinchan.skillrespecpill.api.SkillRespecPillApi`。

## 运行依赖

两个加载器均强制要求：

- Minecraft 26.1.2
- Java 25
- Puffish Skills 0.18.3
  - Fabric Curse file `8547681`
  - NeoForge Curse file `8547682`
- RinLib `1.0.0+26.1.2-fabric` / `1.0.0+26.1.2-neoforge` 精确加载器产物
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
