# 技能后悔药

`skill-respec-pill` 是 Minecraft 1.21.1 NeoForge 上的 Puffish Skills 附属模组。1.0.0 提供：

- 点击任意未解锁节点时，一次购买其完整缺失前置闭包；
- 可配置的后代级联退还（默认开启）；
- SkillsScreen 原生按钮触发的当前页重置；
- 数据包声明的默认节点与不可退还强制节点；
- 面向其他模组的命名空间授权门禁 API。

客户端和服务端都必须安装 Puffish Skills 0.18.3、RinLib 1.0.0 和本模组。

## 数据包策略

策略资源放在任意数据包命名空间的：

```text
data/<namespace>/skill_respec_pill/policies/<name>.json
```

每个分类最多一个有效策略，只接受三个字段：

```json
{
  "category": "example:tree",
  "default_enabled": ["starter"],
  "forced_enabled": ["root"]
}
```

节点图、连接和成本始终读取 Puffish Skills 已加载的分类配置。缺失节点、缺失定义、无效连接或环会记录错误并拒绝操作。

默认节点按“分类 ID + 精确节点 ID”逐项记录。新加入的默认节点会在下次登录授予；玩家主动退掉的旧默认节点不会在普通登录时恢复。整页重置会恢复当前策略中的全部默认与强制节点。

## 配置

服务端配置仅有：

```toml
cascade_refund_enabled = true
```

关闭后，已解锁节点点击交还 Puffish Skills 原行为；批量前置购买与可见性始终启用。

## 扩展门禁

门禁按命名空间 ID 排序执行，并且全部允许时操作才可继续。批量购买不经过门禁。

```java
SkillRespecPillApi.registerGate(
    SkillRespecPillApi.Action.PAGE_RESET,
    ResourceLocation.fromNamespaceAndPath("example", "permission"),
    context -> allowed
        ? SkillRespecPillApi.Authorization.allow()
        : SkillRespecPillApi.Authorization.deny(
            Component.translatable("message.example.denied")));
```

支持的动作是 `CASCADE_REFUND` 与 `PAGE_RESET`。拒绝理由是可选的原生 `Component`。

## 许可证

GPL-3.0。详见 [LICENSE](LICENSE)。
