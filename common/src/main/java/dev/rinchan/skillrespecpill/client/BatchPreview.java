package dev.rinchan.skillrespecpill.client;

import dev.rinchan.skillrespecpill.graph.SkillGraph;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import net.puffish.skillsmod.api.Skill;
import net.puffish.skillsmod.client.config.ClientCategoryConfig;
import net.puffish.skillsmod.client.config.skill.ClientSkillConfig;
import net.puffish.skillsmod.client.data.ClientCategoryData;

public final class BatchPreview {
    private BatchPreview() {
    }

    public static Preview forNode(
            ClientCategoryConfig config,
            ClientCategoryData data,
            ClientSkillConfig target) {
        try {
            if (!ClientPolicyState.ready()) return new Preview(Action.INVALID_GRAPH, 0);
            SkillGraph graph = graph(config);
            if (data.getSkillState(target) == Skill.State.UNLOCKED) {
                if (ClientPolicyState.isForced(config.id(), target.id())) {
                    return new Preview(Action.FORCED, 0);
                }
                if (!ClientPolicyState.cascadeRefundEnabled()) {
                    return new Preview(Action.CASCADE_DISABLED, 0);
                }
                SkillGraph.Plan plan = graph.refundPlan(target.id(), unlocked(config, data));
                return new Preview(Action.REFUND, plan.points());
            }
            SkillGraph.Plan plan = graph.unlockPlan(target.id(), unlocked(config, data));
            return new Preview(Action.UNLOCK, plan.points());
        } catch (RuntimeException exception) {
            return new Preview(Action.INVALID_GRAPH, 0);
        }
    }

    private static SkillGraph graph(ClientCategoryConfig config) {
        var costs = new HashMap<String, Integer>();
        for (ClientSkillConfig skill : config.skills().values()) {
            var definition = config.getDefinitionById(skill.definitionId()).orElseThrow();
            costs.put(skill.id(), definition.cost());
        }
        Map<String, java.util.Collection<String>> prerequisites = new HashMap<>();
        config.skillNormalNeighborsReversed().forEach(
                (node, parents) -> prerequisites.put(node, java.util.List.copyOf(parents)));
        return SkillGraph.of(costs, prerequisites);
    }

    private static java.util.Set<String> unlocked(
            ClientCategoryConfig config,
            ClientCategoryData data) {
        var unlocked = new HashSet<String>();
        config.skills().values().stream()
                .filter(skill -> data.getSkillState(skill) == Skill.State.UNLOCKED)
                .map(ClientSkillConfig::id)
                .forEach(unlocked::add);
        return java.util.Set.copyOf(unlocked);
    }

    public enum Action {
        UNLOCK,
        REFUND,
        FORCED,
        CASCADE_DISABLED,
        INVALID_GRAPH
    }

    public record Preview(Action action, int points) {
    }
}
