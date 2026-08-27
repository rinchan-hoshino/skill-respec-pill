package dev.rinchan.skillrespecpill;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.rinchan.skillrespecpill.api.internal.DeterministicGateSet;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.jupiter.api.Test;

final class DeterministicGateSetTest {
    @Test
    void evaluatesNamespacedGatesInStableOrderAndRequiresEveryGateToAllow() {
        var calls = new ArrayList<String>();
        var gates = new DeterministicGateSet<String, String>();
        gates.register("z:last", context -> {
            calls.add("z:last");
            return DeterministicGateSet.Decision.allow();
        });
        gates.register("a:first", context -> {
            calls.add("a:first");
            return DeterministicGateSet.Decision.deny(Optional.of("native reason"));
        });

        var result = gates.evaluate("context");

        assertFalse(result.allowed());
        assertEquals(Optional.of("native reason"), result.denialReason());
        assertEquals(Optional.of("a:first"), result.denyingGateId());
        assertEquals(java.util.List.of("a:first"), calls);
    }

    @Test
    void supportsSilentDenialAndRejectsDuplicateOrNonNamespacedIds() {
        var gates = new DeterministicGateSet<Void, String>();
        gates.register("example:gate", ignored -> DeterministicGateSet.Decision.deny(Optional.empty()));

        assertTrue(gates.evaluate(null).denialReason().isEmpty());
        assertThrows(IllegalArgumentException.class,
                () -> gates.register("example:gate", ignored -> DeterministicGateSet.Decision.allow()));
        assertThrows(IllegalArgumentException.class,
                () -> gates.register("not_namespaced", ignored -> DeterministicGateSet.Decision.allow()));
    }
}
