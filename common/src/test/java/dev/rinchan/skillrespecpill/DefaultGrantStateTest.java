package dev.rinchan.skillrespecpill;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.rinchan.skillrespecpill.state.DefaultGrantState;
import java.util.Set;
import org.junit.jupiter.api.Test;

final class DefaultGrantStateTest {
    @Test
    void remembersEachExactCategoryAndNodeWithoutCoarseInitialization() {
        var state = new DefaultGrantState();
        state.markGranted("example:tree", "old_default");

        assertEquals(Set.of("new_default"), state.pending(
                "example:tree", Set.of("old_default", "new_default")));
        assertTrue(state.wasGranted("example:tree", "old_default"));
        assertFalse(state.wasGranted("other:tree", "old_default"));
        assertFalse(state.wasGranted("example:tree", "old_default_typo"));
    }

    @Test
    void refundedDefaultsStayRememberedButNewDefaultsRemainPendingAndCopyIsIndependent() {
        var state = new DefaultGrantState();
        state.markGranted("example:tree", "voluntarily_refunded");
        var clone = state.copy();
        clone.markGranted("example:tree", "new_default");

        assertEquals(Set.of("new_default"), state.pending(
                "example:tree", Set.of("voluntarily_refunded", "new_default")));
        assertEquals(Set.of(), clone.pending(
                "example:tree", Set.of("voluntarily_refunded", "new_default")));
    }
}
