package dev.rinchan.skillrespecpill.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public final class SkillGraph {
    private final Map<String, Integer> costs;
    private final Map<String, Set<String>> prerequisites;
    private final Map<String, Set<String>> dependents;

    private SkillGraph(Map<String, Integer> costs, Map<String, Set<String>> prerequisites) {
        this.costs = costs;
        this.prerequisites = prerequisites;
        this.dependents = reverse(prerequisites, costs.keySet());
        validateAcyclic();
    }

    public static SkillGraph of(
            Map<String, Integer> costs,
            Map<String, ? extends Collection<String>> prerequisites) {
        if (costs == null || costs.isEmpty()) throw new GraphException("skill graph has no nodes");
        var copiedCosts = new TreeMap<String, Integer>();
        costs.forEach((id, cost) -> {
            requireId(id, "node");
            if (cost == null || cost < 0) throw new GraphException("invalid cost for " + id + ": " + cost);
            copiedCosts.put(id, cost);
        });

        var copiedPrerequisites = new TreeMap<String, Set<String>>();
        copiedCosts.keySet().forEach(id -> copiedPrerequisites.put(id, Set.of()));
        if (prerequisites != null) {
            prerequisites.forEach((node, parents) -> {
                requireKnown(copiedCosts, node, "connection target");
                if (parents == null) throw new GraphException("null prerequisites for " + node);
                var copiedParents = new TreeSet<String>();
                for (String parent : parents) {
                    requireKnown(copiedCosts, parent, "connection prerequisite");
                    if (node.equals(parent)) throw new GraphException("self cycle at " + node);
                    copiedParents.add(parent);
                }
                copiedPrerequisites.put(node, Collections.unmodifiableSet(copiedParents));
            });
        }
        return new SkillGraph(Map.copyOf(copiedCosts), Map.copyOf(copiedPrerequisites));
    }

    public Set<String> nodeIds() {
        return costs.keySet();
    }

    public Plan unlockPlan(String target, Set<String> unlocked) {
        requireKnown(costs, target, "unlock target");
        Set<String> current = unlocked == null ? Set.of() : Set.copyOf(unlocked);
        var ordered = new ArrayList<String>();
        collectPrerequisites(target, current, new HashSet<>(), ordered);
        return plan(ordered);
    }

    public Plan refundPlan(String target, Set<String> unlocked) {
        requireKnown(costs, target, "refund target");
        Set<String> current = unlocked == null ? Set.of() : Set.copyOf(unlocked);
        var ordered = new ArrayList<String>();
        collectDependents(target, current, new HashSet<>(), ordered, Set.of());
        return plan(ordered);
    }

    public List<String> resetOrder(Set<String> unlocked, Set<String> protectedNodes) {
        Set<String> current = unlocked == null ? Set.of() : Set.copyOf(unlocked);
        Set<String> protectedCopy = protectedNodes == null ? Set.of() : Set.copyOf(protectedNodes);
        var ordered = new ArrayList<String>();
        var visited = new HashSet<String>();
        for (String node : new TreeSet<>(costs.keySet())) {
            collectDependents(node, current, visited, ordered, protectedCopy);
        }
        return List.copyOf(ordered);
    }

    public int cost(String nodeId) {
        requireKnown(costs, nodeId, "cost node");
        return costs.get(nodeId);
    }

    public Set<String> prerequisites(String nodeId) {
        requireKnown(costs, nodeId, "prerequisite node");
        return prerequisites.get(nodeId);
    }

    private void collectPrerequisites(
            String node,
            Set<String> unlocked,
            Set<String> visited,
            List<String> ordered) {
        if (unlocked.contains(node) || !visited.add(node)) return;
        for (String parent : prerequisites.get(node)) {
            collectPrerequisites(parent, unlocked, visited, ordered);
        }
        ordered.add(node);
    }

    private void collectDependents(
            String node,
            Set<String> unlocked,
            Set<String> visited,
            List<String> ordered,
            Set<String> protectedNodes) {
        if (!unlocked.contains(node) || !visited.add(node)) return;
        for (String child : dependents.get(node)) {
            collectDependents(child, unlocked, visited, ordered, protectedNodes);
        }
        if (!protectedNodes.contains(node)) ordered.add(node);
    }

    private Plan plan(List<String> ordered) {
        int total = 0;
        for (String node : ordered) total = Math.addExact(total, costs.get(node));
        return new Plan(List.copyOf(ordered), total);
    }

    private void validateAcyclic() {
        var visited = new HashSet<String>();
        var visiting = new HashSet<String>();
        for (String node : new TreeSet<>(costs.keySet())) visit(node, visiting, visited);
    }

    private void visit(String node, Set<String> visiting, Set<String> visited) {
        if (visited.contains(node)) return;
        if (!visiting.add(node)) throw new GraphException("cycle in skill graph at " + node);
        for (String parent : prerequisites.get(node)) visit(parent, visiting, visited);
        visiting.remove(node);
        visited.add(node);
    }

    private static Map<String, Set<String>> reverse(
            Map<String, Set<String>> prerequisites,
            Set<String> nodes) {
        var reversed = new TreeMap<String, Set<String>>();
        nodes.forEach(node -> reversed.put(node, new TreeSet<>()));
        prerequisites.forEach((child, parents) ->
                parents.forEach(parent -> reversed.get(parent).add(child)));
        var immutable = new HashMap<String, Set<String>>();
        reversed.forEach((node, children) -> immutable.put(node, Collections.unmodifiableSet(children)));
        return Map.copyOf(immutable);
    }

    private static void requireId(String id, String role) {
        if (id == null || id.isEmpty()) throw new GraphException("invalid " + role + " id");
    }

    private static void requireKnown(Map<String, ?> nodes, String id, String role) {
        requireId(id, role);
        if (!nodes.containsKey(id)) throw new GraphException("unknown " + role + ": " + id);
    }

    public record Plan(List<String> nodeIds, int points) {
    }
}
