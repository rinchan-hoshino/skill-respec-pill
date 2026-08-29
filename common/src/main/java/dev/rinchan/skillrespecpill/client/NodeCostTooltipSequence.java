package dev.rinchan.skillrespecpill.client;

import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;

/** Invisible transport from Puffish Skills' text tooltip list to the custom cost renderer. */
public record NodeCostTooltipSequence(NodeCostTooltip.Display display) implements FormattedCharSequence {
    @Override
    public boolean accept(FormattedCharSink sink) {
        return true;
    }
}
