package dev.rinchan.skillrespecpill;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.rinchan.skillrespecpill.graph.SkillGraph;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;

final class SkillGraphPropertyTest {
    @Test
    void generatedDagsProduceCompleteDeterministicTopologicalPlans() {
        var random = new Random(0x534B494C4C475241L);

        for (int example = 0; example < 500; example++) {
            var fixture = generatedDag(random);
            var graph = SkillGraph.of(fixture.costs(), fixture.prerequisites());
            var reorderedGraph = SkillGraph.of(
                    reversed(fixture.costs()),
                    reversed(fixture.prerequisites()));
            String target = fixture.nodeIds().get(random.nextInt(fixture.nodeIds().size()));
            Set<String> unlocked = randomValidUnlockedSet(fixture, random);

            var plan = graph.unlockPlan(target, unlocked);
            var expectedNodes = missingAncestors(target, unlocked, fixture.prerequisites());

            assertEquals(expectedNodes, Set.copyOf(plan.nodeIds()));
            assertTopological(plan.nodeIds(), fixture.prerequisites());
            assertEquals(exactCost(plan.nodeIds(), fixture.costs()), plan.points());
            assertEquals(plan, reorderedGraph.unlockPlan(target, unlocked));

            if (unlocked.contains(target)) {
                var refund = graph.refundPlan(target, unlocked);
                assertEquals(
                        unlockedDescendants(target, unlocked, fixture.prerequisites()),
                        Set.copyOf(refund.nodeIds()));
                assertDescendantsFirst(refund.nodeIds(), fixture.prerequisites());
                assertEquals(exactCost(refund.nodeIds(), fixture.costs()), refund.points());
                assertEquals(refund, reorderedGraph.refundPlan(target, unlocked));
            }
        }
    }

    @Test
    void rejectsPointTotalsThatOverflowInsteadOfWrappingNegative() {
        var graph = SkillGraph.of(
                Map.of("root", Integer.MAX_VALUE, "target", 1),
                Map.of("target", List.of("root")));

        assertThrows(ArithmeticException.class, () -> graph.unlockPlan("target", Set.of()));
    }

    private static DagFixture generatedDag(Random random) {
        int size = 2 + random.nextInt(11);
        var nodeIds = new ArrayList<String>(size);
        var costs = new LinkedHashMap<String, Integer>();
        var prerequisites = new LinkedHashMap<String, List<String>>();
        for (int index = 0; index < size; index++) {
            String node = "n" + index;
            nodeIds.add(node);
            costs.put(node, random.nextInt(101));
            var parents = new ArrayList<String>();
            for (int parent = 0; parent < index; parent++) {
                if (random.nextInt(4) == 0) parents.add("n" + parent);
            }
            Collections.shuffle(parents, random);
            prerequisites.put(node, List.copyOf(parents));
        }
        return new DagFixture(List.copyOf(nodeIds), costs, prerequisites);
    }

    private static Set<String> randomValidUnlockedSet(DagFixture fixture, Random random) {
        var unlocked = new LinkedHashSet<String>();
        for (String node : fixture.nodeIds()) {
            if (random.nextBoolean() && unlocked.containsAll(fixture.prerequisites().get(node))) {
                unlocked.add(node);
            }
        }
        return Set.copyOf(unlocked);
    }

    private static Set<String> missingAncestors(
            String target,
            Set<String> unlocked,
            Map<String, List<String>> prerequisites) {
        var result = new HashSet<String>();
        collectMissingAncestors(target, unlocked, prerequisites, result);
        return result;
    }

    private static void collectMissingAncestors(
            String node,
            Set<String> unlocked,
            Map<String, List<String>> prerequisites,
            Set<String> result) {
        if (unlocked.contains(node) || !result.add(node)) return;
        for (String parent : prerequisites.get(node)) {
            collectMissingAncestors(parent, unlocked, prerequisites, result);
        }
    }

    private static Set<String> unlockedDescendants(
            String target,
            Set<String> unlocked,
            Map<String, List<String>> prerequisites) {
        var result = new HashSet<String>();
        result.add(target);
        boolean changed;
        do {
            changed = false;
            for (var entry : prerequisites.entrySet()) {
                if (unlocked.contains(entry.getKey())
                        && entry.getValue().stream().anyMatch(result::contains)) {
                    changed |= result.add(entry.getKey());
                }
            }
        } while (changed);
        return result;
    }

    private static void assertTopological(
            List<String> plan,
            Map<String, List<String>> prerequisites) {
        var positions = positions(plan);
        for (String node : plan) {
            for (String parent : prerequisites.get(node)) {
                if (positions.containsKey(parent)) {
                    assertTrue(positions.get(parent) < positions.get(node));
                }
            }
        }
    }

    private static void assertDescendantsFirst(
            List<String> plan,
            Map<String, List<String>> prerequisites) {
        var positions = positions(plan);
        prerequisites.forEach((child, parents) -> {
            for (String parent : parents) {
                if (positions.containsKey(parent) && positions.containsKey(child)) {
                    assertTrue(positions.get(child) < positions.get(parent));
                }
            }
        });
    }

    private static Map<String, Integer> positions(List<String> nodes) {
        var positions = new LinkedHashMap<String, Integer>();
        for (int index = 0; index < nodes.size(); index++) positions.put(nodes.get(index), index);
        return positions;
    }

    private static int exactCost(List<String> nodes, Map<String, Integer> costs) {
        int total = 0;
        for (String node : nodes) total = Math.addExact(total, costs.get(node));
        return total;
    }

    private static <V> Map<String, V> reversed(Map<String, V> source) {
        var keys = new ArrayList<>(source.keySet());
        Collections.reverse(keys);
        var result = new LinkedHashMap<String, V>();
        for (String key : keys) result.put(key, source.get(key));
        return result;
    }

    private record DagFixture(
            List<String> nodeIds,
            Map<String, Integer> costs,
            Map<String, List<String>> prerequisites) {
    }
}
