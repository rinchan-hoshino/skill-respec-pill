package dev.rinchan.skillrespecpill;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.rinchan.skillrespecpill.graph.GraphException;
import dev.rinchan.skillrespecpill.graph.SkillGraph;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

final class SkillGraphTest {
    @Test
    void plansCompleteMissingPrerequisitesTopologicallyAndSumsNetCost() {
        var graph = SkillGraph.of(
                Map.of("root", 0, "a", 2, "b", 3, "target", 5),
                Map.of("a", List.of("root"), "b", List.of("root"), "target", List.of("b", "a")));

        var plan = graph.unlockPlan("target", Set.of("root", "a"));

        assertEquals(List.of("b", "target"), plan.nodeIds());
        assertEquals(8, plan.points());
    }

    @Test
    void plansUnlockedDependentsDescendantsFirst() {
        var graph = SkillGraph.of(
                Map.of("root", 0, "a", 2, "b", 3, "leaf", 5),
                Map.of("a", List.of("root"), "b", List.of("a"), "leaf", List.of("b")));

        var plan = graph.refundPlan("a", Set.of("root", "a", "b", "leaf"));

        assertEquals(List.of("leaf", "b", "a"), plan.nodeIds());
        assertEquals(10, plan.points());
        assertEquals(List.of("leaf", "b", "a"), graph.resetOrder(
                Set.of("root", "a", "b", "leaf"), Set.of("root")));
    }

    @Test
    void rejectsMissingCostsUnknownEdgesNegativeCostsAndCyclesAtConstruction() {
        assertThrows(GraphException.class, () -> SkillGraph.of(
                Map.of("a", 1), Map.of("a", List.of("missing"))));
        assertThrows(GraphException.class, () -> SkillGraph.of(
                Map.of("a", -1), Map.of()));
        assertThrows(GraphException.class, () -> SkillGraph.of(
                Map.of("a", 1, "b", 1), Map.of("a", List.of("b"), "b", List.of("a"))));
    }

    @Test
    void outputIsDeterministicRegardlessOfInputCollectionOrder() {
        var graph = SkillGraph.of(
                Map.of("root", 0, "a", 1, "z", 1, "target", 1),
                Map.of("target", Set.of("z", "a"), "a", Set.of("root"), "z", Set.of("root")));

        assertEquals(List.of("a", "z", "target"), graph.unlockPlan("target", Set.of("root")).nodeIds());
    }
}
