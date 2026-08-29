# Changelog

## 1.0.3

- Restore compact signed batch previews: `(-points)` to unlock and `(+points)` to refund, with no duplicate prose.
- Send insufficient-points, forced-node, disabled-refund, denied-action and failed-action feedback to persistent chat instead of transient or hidden UI text.

## 1.0.2

- Restore the visible, clickable **Reset Page** button on Puffish Skills 0.18.3 screens, whose custom render and mouse-input methods bypass vanilla screen widgets.
- Keep page-reset authorization, server-side policy enforcement and network behavior unchanged.

## 1.0.1

- Correct the public English display name to **Skill Respec Pill**; `技能后悔药` remains the Simplified Chinese localized name.
- Match the NeoForge RinLib dependency to its internal `1.0.0` mod version instead of its platform-qualified artifact version.
- Keep every gameplay, policy, persistence and networking contract unchanged from 1.0.0, including the `1.0.0` NeoForge network protocol.

## 1.0.0

- Add server-authoritative batch prerequisite purchasing with exact aggregate cost checks.
- Keep ordinary locked nodes visible and clickable while preserving excluded-node state.
- Add configurable cascade refunds with descendant-first rollback and silent point notifications.
- Add a native full-page reset button and deterministic authorization-gate API.
- Add data-driven, per-node default grants and permanently protected forced nodes.
- Support Minecraft 1.21.1, 26.1.2 and 26.2 on Fabric and NeoForge through required RinLib and Puffish Skills dependencies.
