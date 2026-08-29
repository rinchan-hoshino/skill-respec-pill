package dev.rinchan.skillrespecpill;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import dev.rinchan.skillrespecpill.client.NodeCostTooltip;
import org.junit.jupiter.api.Test;

final class NodeCostTooltipTest {
    @Test
    void unlockShowsLocalCostAndACompactRedTotalDebit() {
        var display = NodeCostTooltip.describe(30, 80, NodeCostTooltip.Flow.DEBIT);

        assertEquals(30, display.nodeCost());
        assertEquals("-80", display.totalBadge());
        assertEquals(NodeCostTooltip.BadgeColor.RED, display.badgeColor());
    }

    @Test
    void refundShowsLocalCostAndACompactGreenTotalCredit() {
        var display = NodeCostTooltip.describe(50, 130, NodeCostTooltip.Flow.CREDIT);

        assertEquals(50, display.nodeCost());
        assertEquals("+130", display.totalBadge());
        assertEquals(NodeCostTooltip.BadgeColor.GREEN, display.badgeColor());
    }

    @Test
    void nonInteractiveNodesStillExposeTheirOwnCostWithoutInventingATotal() {
        var display = NodeCostTooltip.describe(0, 0, NodeCostTooltip.Flow.NONE);

        assertEquals(0, display.nodeCost());
        assertFalse(display.hasTotalBadge());
    }
}
