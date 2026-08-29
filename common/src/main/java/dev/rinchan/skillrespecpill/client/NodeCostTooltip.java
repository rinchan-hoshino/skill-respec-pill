package dev.rinchan.skillrespecpill.client;

/** Owns the compact local-cost and batch-total presentation for a skill node tooltip. */
public final class NodeCostTooltip {
    private NodeCostTooltip() {
    }

    public static Display describe(int nodeCost, int totalPoints, Flow flow) {
        if (nodeCost < 0 || totalPoints < 0) {
            throw new IllegalArgumentException("Skill point costs must be non-negative");
        }
        return switch (flow) {
            case DEBIT -> new Display(nodeCost, "-" + totalPoints, BadgeColor.RED);
            case CREDIT -> new Display(nodeCost, "+" + totalPoints, BadgeColor.GREEN);
            case NONE -> new Display(nodeCost, "", BadgeColor.NONE);
        };
    }

    public enum Flow {
        DEBIT,
        CREDIT,
        NONE
    }

    public enum BadgeColor {
        RED,
        GREEN,
        NONE
    }

    public record Display(int nodeCost, String totalBadge, BadgeColor badgeColor) {
        public boolean hasTotalBadge() {
            return !totalBadge.isEmpty();
        }
    }
}
