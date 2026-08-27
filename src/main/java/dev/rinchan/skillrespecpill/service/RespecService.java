package dev.rinchan.skillrespecpill.service;

import dev.rinchan.skillrespecpill.SkillRespecConfig;
import dev.rinchan.skillrespecpill.SkillRespecPill;
import dev.rinchan.skillrespecpill.api.SkillRespecPillApi;
import dev.rinchan.skillrespecpill.api.SkillRespecPillApi.Action;
import dev.rinchan.skillrespecpill.api.SkillRespecPillApi.GateContext;
import dev.rinchan.skillrespecpill.graph.PuffishGraphSource;
import dev.rinchan.skillrespecpill.graph.SkillGraph;
import dev.rinchan.skillrespecpill.policy.PolicyRepository;
import dev.rinchan.skillrespecpill.policy.SkillPolicy;
import dev.rinchan.skillrespecpill.state.DefaultGrantState;
import dev.rinchan.skillrespecpill.state.PlayerDefaultGrantData;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.puffish.skillsmod.api.Category;
import net.puffish.skillsmod.api.Skill;
import net.puffish.skillsmod.api.SkillsAPI;

public final class RespecService {
    private RespecService() {
    }

    public static boolean handleNodeClick(
            ServerPlayer player,
            ResourceLocation categoryId,
            String nodeId) {
        if (player.isSpectator()) return true;
        try {
            Category category = SkillsAPI.getCategory(categoryId).orElse(null);
            if (category == null) return false;
            Skill skill = category.getSkill(nodeId).orElse(null);
            if (skill == null) return false;
            Optional<SkillPolicy> policy = PolicyRepository.find(player.getServer(), categoryId);

            if (skill.getState(player) == Skill.State.UNLOCKED) {
                if (policy.map(value -> value.forcedEnabled().contains(nodeId)).orElse(false)) return true;
                if (!SkillRespecConfig.cascadeRefundEnabled()) return false;
                SkillGraph graph = validatedGraph(categoryId, policy);
                if (!authorize(player, Action.CASCADE_REFUND, categoryId, Optional.of(nodeId))) return true;
                SkillGraph.Plan plan = graph.refundPlan(nodeId, unlockedIds(category, player));
                Set<String> forced = policy.map(SkillPolicy::forcedEnabled).orElse(Set.of());
                if (plan.nodeIds().stream().anyMatch(forced::contains)) {
                    SkillRespecPill.LOGGER.error(
                            "Cascade refund for {}:{} includes a forced node and was rejected",
                            categoryId, nodeId);
                    return true;
                }
                lockDescendantsFirst(player, category, plan.nodeIds());
                return true;
            }

            if (skill.getState(player) == Skill.State.EXCLUDED) return false;
            SkillGraph graph = validatedGraph(categoryId, policy);
            SkillGraph.Plan plan = graph.unlockPlan(nodeId, unlockedIds(category, player));
            int pointsLeft = category.getPointsLeft(player);
            if (pointsLeft < plan.points()) {
                return true;
            }
            unlockTopologically(player, category, plan.nodeIds());
            return true;
        } catch (Exception exception) {
            SkillRespecPill.LOGGER.error(
                    "Node action failed closed for category {} node {} player {}",
                    categoryId, nodeId, player.getGameProfile().getName(), exception);
            return true;
        }
    }

    public static void resetPage(ServerPlayer player, ResourceLocation categoryId) {
        if (player.isSpectator()) return;
        try {
            Category category = SkillsAPI.getCategory(categoryId).orElseThrow(() ->
                    new IllegalStateException("missing Puffish category " + categoryId));
            Optional<SkillPolicy> policy = PolicyRepository.find(player.getServer(), categoryId);
            SkillGraph graph = validatedGraph(categoryId, policy);
            if (!authorize(player, Action.PAGE_RESET, categoryId, Optional.empty())) return;

            Set<String> forced = policy.map(SkillPolicy::forcedEnabled).orElse(Set.of());
            List<String> resetOrder = graph.resetOrder(unlockedIds(category, player), forced);
            lockDescendantsFirst(player, category, resetOrder);

            var state = PlayerDefaultGrantData.read(player);
            if (policy.isPresent()) {
                restoreDeclaredBaseline(player, category, policy.get(), state, true);
                PlayerDefaultGrantData.write(player, state);
            }
            SkillRespecPill.LOGGER.info(
                    "Reset category {} for player {}; refunded {} ordinary nodes",
                    categoryId, player.getGameProfile().getName(), resetOrder.size());
        } catch (Exception exception) {
            SkillRespecPill.LOGGER.error(
                    "Page reset failed closed for category {} player {}",
                    categoryId, player.getGameProfile().getName(), exception);
        }
    }

    public static boolean isForced(
            ServerPlayer player,
            ResourceLocation categoryId,
            String nodeId) {
        try {
            return PolicyRepository.find(player.getServer(), categoryId)
                    .map(policy -> policy.forcedEnabled().contains(nodeId))
                    .orElse(false);
        } catch (Exception exception) {
            SkillRespecPill.LOGGER.error(
                    "Forced-node policy lookup failed closed for {}:{}", categoryId, nodeId, exception);
            return true;
        }
    }

    public static void reconcileLogin(
            ServerPlayer player,
            Category category,
            SkillPolicy policy,
            DefaultGrantState state) {
        validatedGraph(category.getId(), Optional.of(policy));
        restoreDeclaredBaseline(player, category, policy, state, false);
    }

    public static SkillGraph validatedGraph(
            ResourceLocation categoryId,
            Optional<SkillPolicy> policy) {
        SkillGraph graph = PuffishGraphSource.extract(PuffishGraphSource.categoryConfig(categoryId));
        if (policy.isPresent()) {
            var unknown = new TreeSet<String>();
            unknown.addAll(policy.get().defaultEnabled());
            unknown.addAll(policy.get().forcedEnabled());
            unknown.removeAll(graph.nodeIds());
            if (!unknown.isEmpty()) {
                throw new IllegalStateException("policy references missing exact node IDs: " + unknown);
            }
        }
        return graph;
    }

    private static void restoreDeclaredBaseline(
            ServerPlayer player,
            Category category,
            SkillPolicy policy,
            DefaultGrantState state,
            boolean includeAllDefaults) {
        unlockExactIds(player, category, policy.forcedEnabled());
        Set<String> defaults = includeAllDefaults
                ? policy.defaultEnabled()
                : state.pending(category.getId().toString(), policy.defaultEnabled());
        for (String nodeId : new TreeSet<>(defaults)) {
            Skill skill = category.getSkill(nodeId).orElseThrow(() ->
                    new IllegalStateException("missing default node " + nodeId));
            if (skill.getState(player) != Skill.State.UNLOCKED) skill.unlock(player);
            if (skill.getState(player) != Skill.State.UNLOCKED) {
                throw new IllegalStateException("Puffish did not grant default node " + nodeId);
            }
            state.markGranted(category.getId().toString(), nodeId);
        }
    }

    private static void unlockExactIds(
            ServerPlayer player,
            Category category,
            Set<String> nodeIds) {
        for (String nodeId : new TreeSet<>(nodeIds)) {
            Skill skill = category.getSkill(nodeId).orElseThrow(() ->
                    new IllegalStateException("missing forced node " + nodeId));
            if (skill.getState(player) != Skill.State.UNLOCKED) skill.unlock(player);
            if (skill.getState(player) != Skill.State.UNLOCKED) {
                throw new IllegalStateException("Puffish did not grant forced node " + nodeId);
            }
        }
    }

    private static boolean authorize(
            ServerPlayer player,
            Action action,
            ResourceLocation categoryId,
            Optional<String> nodeId) {
        try {
            var authorization = SkillRespecPillApi.evaluate(
                    new GateContext(player, action, categoryId, nodeId));
            if (!authorization.allowed()) {
                authorization.denialReason().ifPresent(reason -> player.displayClientMessage(reason, false));
                return false;
            }
            return true;
        } catch (Exception exception) {
            SkillRespecPill.LOGGER.error(
                    "Authorization gate failed closed for {} in category {} player {}",
                    action, categoryId, player.getGameProfile().getName(), exception);
            return false;
        }
    }

    private static Set<String> unlockedIds(Category category, ServerPlayer player) {
        var unlocked = new TreeSet<String>();
        category.streamUnlockedSkills(player).map(Skill::getId).forEach(unlocked::add);
        return Set.copyOf(unlocked);
    }

    private static void unlockTopologically(
            ServerPlayer player,
            Category category,
            List<String> nodeIds) {
        var prepared = new ArrayList<Skill>(nodeIds.size());
        for (String nodeId : nodeIds) {
            prepared.add(category.getSkill(nodeId).orElseThrow(() ->
                    new IllegalStateException("missing planned unlock node " + nodeId)));
        }
        var changed = new ArrayList<Skill>();
        try {
            for (Skill skill : prepared) {
                if (skill.getState(player) == Skill.State.UNLOCKED) continue;
                skill.unlock(player);
                if (skill.getState(player) != Skill.State.UNLOCKED) {
                    throw new IllegalStateException("Puffish rejected planned unlock " + skill.getId());
                }
                changed.add(skill);
            }
        } catch (Exception exception) {
            for (int index = changed.size() - 1; index >= 0; index--) {
                Skill changedSkill = changed.get(index);
                if (changedSkill.getState(player) == Skill.State.UNLOCKED) changedSkill.lock(player);
            }
            throw new IllegalStateException("atomic batch unlock rolled back", exception);
        }
    }

    private static void lockDescendantsFirst(
            ServerPlayer player,
            Category category,
            List<String> nodeIds) {
        var prepared = new ArrayList<Skill>(nodeIds.size());
        for (String nodeId : nodeIds) {
            prepared.add(category.getSkill(nodeId).orElseThrow(() ->
                    new IllegalStateException("missing planned refund node " + nodeId)));
        }
        var changed = new ArrayList<Skill>();
        try {
            PointMutationContext.runRefund(player.getUUID(), category.getId().toString(), () -> {
                for (Skill skill : prepared) {
                    if (skill.getState(player) != Skill.State.UNLOCKED) continue;
                    skill.lock(player);
                    if (skill.getState(player) == Skill.State.UNLOCKED) {
                        throw new IllegalStateException("Puffish rejected planned refund " + skill.getId());
                    }
                    changed.add(skill);
                }
            });
        } catch (Exception exception) {
            for (int index = changed.size() - 1; index >= 0; index--) {
                Skill changedSkill = changed.get(index);
                if (changedSkill.getState(player) != Skill.State.UNLOCKED) changedSkill.unlock(player);
            }
            throw new IllegalStateException("atomic cascade refund rolled back", exception);
        }
    }
}
