package dev.rinchan.skillrespecpill.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix4f;

/** Renders a normal local cost with a genuinely scaled batch-total badge at its upper right. */
public final class NodeCostTooltipComponent implements ClientTooltipComponent {
    static final float BADGE_SCALE = 0.65F;
    static final float BADGE_Y_OFFSET = -2.0F;
    static final int BADGE_GAP = 1;
    static final int LIGHT_GRAY = 0xFFAAAAAA;
    static final int RED = 0xFFFF5555;
    static final int GREEN = 0xFF55FF55;
    private static final int FULL_BRIGHT = 15728880;

    private final NodeCostTooltip.Display display;
    private final String nodeCost;

    public NodeCostTooltipComponent(NodeCostTooltip.Display display) {
        this.display = display;
        this.nodeCost = Integer.toString(display.nodeCost());
    }

    @Override
    public int getHeight() {
        return 10;
    }

    @Override
    public int getWidth(Font font) {
        int width = font.width(nodeCost);
        if (display.hasTotalBadge()) {
            width += BADGE_GAP + (int) Math.ceil(font.width(display.totalBadge()) * BADGE_SCALE);
        }
        return width;
    }

    @Override
    public void renderText(
            Font font,
            int x,
            int y,
            Matrix4f matrix,
            MultiBufferSource.BufferSource bufferSource) {
        font.drawInBatch(
                nodeCost, x, y, LIGHT_GRAY, true, matrix, bufferSource,
                Font.DisplayMode.NORMAL, 0, FULL_BRIGHT);
        if (!display.hasTotalBadge()) {
            return;
        }

        float badgeX = x + font.width(nodeCost) + BADGE_GAP;
        Matrix4f badgeMatrix = new Matrix4f(matrix);
        badgeMatrix.translate(badgeX, y + BADGE_Y_OFFSET, 0.0F);
        badgeMatrix.scale(BADGE_SCALE, BADGE_SCALE, 1.0F);
        int color = display.badgeColor() == NodeCostTooltip.BadgeColor.RED ? RED : GREEN;
        font.drawInBatch(
                display.totalBadge(), 0.0F, 0.0F, color, true, badgeMatrix, bufferSource,
                Font.DisplayMode.NORMAL, 0, FULL_BRIGHT);
    }
}
