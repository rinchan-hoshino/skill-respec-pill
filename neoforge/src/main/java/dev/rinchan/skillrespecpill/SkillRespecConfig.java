package dev.rinchan.skillrespecpill;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class SkillRespecConfig {
    public static final ModConfigSpec SPEC;
    private static final ModConfigSpec.BooleanValue CASCADE_REFUND_ENABLED;

    static {
        var builder = new ModConfigSpec.Builder();
        CASCADE_REFUND_ENABLED = builder
                .comment("Refund all unlocked dependent nodes when an unlocked node is clicked.")
                .define("cascade_refund_enabled", true);
        SPEC = builder.build();
    }

    private SkillRespecConfig() {
    }

    public static boolean cascadeRefundEnabled() {
        return CASCADE_REFUND_ENABLED.get();
    }
}
