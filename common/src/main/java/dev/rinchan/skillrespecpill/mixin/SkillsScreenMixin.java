package dev.rinchan.skillrespecpill.mixin;

import dev.rinchan.skillrespecpill.client.BatchPreview;
import dev.rinchan.skillrespecpill.client.ClientPolicyState;
import dev.rinchan.skillrespecpill.network.PolicyRequestPayload;
import dev.rinchan.skillrespecpill.network.ResetPagePayload;
import dev.rinchan.skillrespecpill.platform.ClientNetworking;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.puffish.skillsmod.api.Skill;
import net.puffish.skillsmod.client.config.ClientCategoryConfig;
import net.puffish.skillsmod.client.config.skill.ClientSkillConfig;
import net.puffish.skillsmod.client.data.ClientCategoryData;
import net.puffish.skillsmod.client.gui.SkillsScreen;
import net.puffish.skillsmod.client.rendering.ConnectionBatchedRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SkillsScreen.class, remap = false)
public abstract class SkillsScreenMixin extends Screen {
    @Shadow
    private Optional<ClientCategoryData> optActiveCategoryData;

    @Unique
    private Button skillRespecPill$resetButton;

    protected SkillsScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"), remap = false, require = 1)
    private void skillRespecPill$addResetButton(CallbackInfo callback) {
        ClientPolicyState.beginRefresh();
        ClientNetworking.send(PolicyRequestPayload.INSTANCE);
        skillRespecPill$resetButton = Button.builder(
                        Component.translatable("screen.skill_respec_pill.reset_page"),
                        button -> optActiveCategoryData.ifPresent(data -> ClientNetworking.send(
                                new ResetPagePayload(data.getConfig().id()))))
                .bounds(this.width / 2 - 50, this.height - 24, 100, 20)
                .build();
        this.addRenderableWidget(skillRespecPill$resetButton);
    }

    @Inject(method = "render", at = @At("HEAD"), remap = false, require = 1)
    private void skillRespecPill$renderResetButton(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick,
            CallbackInfo callback) {
        if (skillRespecPill$resetButton != null) {
            skillRespecPill$resetButton.active = optActiveCategoryData.isPresent();
        }
    }

    @Redirect(
            method = "lambda$drawContentWithCategory$22",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/puffish/skillsmod/client/data/ClientCategoryData;getSkillState(Lnet/puffish/skillsmod/client/config/skill/ClientSkillConfig;)Lnet/puffish/skillsmod/api/Skill$State;"),
            remap = false,
            require = 1)
    private Skill.State skillRespecPill$renderInteractiveNodesVisibly(
            ClientCategoryData data,
            ClientSkillConfig skill) {
        Skill.State state = data.getSkillState(skill);
        return state == Skill.State.LOCKED ? Skill.State.AVAILABLE : state;
    }

    @Redirect(
            method = "lambda$drawContentWithCategory$21",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/puffish/skillsmod/client/gui/SkillsScreen;setTooltipForNextRenderPass(Ljava/util/List;)V"),
            remap = false,
            require = 1)
    private void skillRespecPill$appendNetPreview(
            SkillsScreen screen,
            List<FormattedCharSequence> lines,
            ClientCategoryConfig config,
            ClientCategoryData data,
            ConnectionBatchedRenderer connectionRenderer,
            GuiGraphics graphics,
            ClientSkillConfig skill) {
        BatchPreview.Preview preview = BatchPreview.forNode(config, data, skill);
        Component line = switch (preview.action()) {
            case UNLOCK -> Component.translatable(
                            "tooltip.skill_respec_pill.batch_unlock", preview.points())
                    .withStyle(ChatFormatting.RED);
            case REFUND -> Component.translatable(
                            "tooltip.skill_respec_pill.cascade_refund", preview.points())
                    .withStyle(ChatFormatting.GREEN);
            case FORCED -> Component.translatable("tooltip.skill_respec_pill.forced")
                    .withStyle(ChatFormatting.GOLD);
            case CASCADE_DISABLED -> Component.translatable("tooltip.skill_respec_pill.cascade_disabled")
                    .withStyle(ChatFormatting.GRAY);
            case INVALID_GRAPH -> Component.translatable("tooltip.skill_respec_pill.invalid_graph")
                    .withStyle(ChatFormatting.DARK_RED);
        };
        lines.add(line.getVisualOrderText());
        this.setTooltipForNextRenderPass(lines);
    }
}
