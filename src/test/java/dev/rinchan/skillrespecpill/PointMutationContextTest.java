package dev.rinchan.skillrespecpill;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.rinchan.skillrespecpill.service.PointMutationContext;
import java.util.UUID;
import org.junit.jupiter.api.Test;

final class PointMutationContextTest {
    @Test
    void scopeMatchesExactPlayerAndCategoryAndAlwaysClears() {
        UUID player = UUID.randomUUID();
        String category = "example:tree";
        assertFalse(PointMutationContext.isRefunding(player, category));
        assertThrows(IllegalStateException.class, () -> PointMutationContext.runRefund(player, category, () -> {
            assertTrue(PointMutationContext.isRefunding(player, category));
            assertFalse(PointMutationContext.isRefunding(UUID.randomUUID(), category));
            throw new IllegalStateException("probe");
        }));
        assertFalse(PointMutationContext.isRefunding(player, category));
    }
}
