package dev.rinchan.skillrespecpill.mixin;

import dev.rinchan.skillrespecpill.client.NodeCostTooltipComponent;
import dev.rinchan.skillrespecpill.client.NodeCostTooltipSequence;
import java.util.List;
import java.util.ListIterator;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsMixin {
    @Inject(method = "renderTooltipInternal", at = @At("HEAD"), require = 1)
    private void skillRespecPill$replaceCostTooltipLine(
            Font font,
            List<ClientTooltipComponent> components,
            int x,
            int y,
            ClientTooltipPositioner positioner,
            CallbackInfo callbackInfo) {
        ListIterator<ClientTooltipComponent> iterator = components.listIterator();
        while (iterator.hasNext()) {
            ClientTooltipComponent component = iterator.next();
            if (!(component instanceof ClientTextTooltip textTooltip)) {
                continue;
            }
            FormattedCharSequence text = ((ClientTextTooltipAccessor) (Object) textTooltip)
                    .skillRespecPill$getText();
            if (text instanceof NodeCostTooltipSequence costTooltip) {
                iterator.set(new NodeCostTooltipComponent(costTooltip.display()));
            }
        }
    }
}
