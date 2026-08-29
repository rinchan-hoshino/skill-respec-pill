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
            case DEBIT -> new Display(nodeCost, superscript("(-" + totalPoints + ")"), BadgeColor.RED);
            case CREDIT -> new Display(nodeCost, superscript("(+" + totalPoints + ")"), BadgeColor.GREEN);
            case NONE -> new Display(nodeCost, "", BadgeColor.NONE);
        };
    }

    private static String superscript(String value) {
        StringBuilder result = new StringBuilder(value.length());
        for (int index = 0; index < value.length(); index++) {
            result.append(switch (value.charAt(index)) {
                case '0' -> '⁰';
                case '1' -> '¹';
                case '2' -> '²';
                case '3' -> '³';
                case '4' -> '⁴';
                case '5' -> '⁵';
                case '6' -> '⁶';
                case '7' -> '⁷';
                case '8' -> '⁸';
                case '9' -> '⁹';
                case '+' -> '⁺';
                case '-' -> '⁻';
                case '(' -> '⁽';
                case ')' -> '⁾';
                default -> throw new IllegalArgumentException("Unsupported badge character: " + value.charAt(index));
            });
        }
        return result.toString();
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
