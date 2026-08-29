package dev.rinchan.skillrespecpill.mixin;

import dev.rinchan.skillrespecpill.client.BatchPreview;
import dev.rinchan.skillrespecpill.client.ClientPolicyState;
import dev.rinchan.skillrespecpill.client.NodeCostTooltip;
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
import net.puffish.skillsmod.client.config.skill.ClientSkillDefinitionConfig;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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

    // Puffish Skills 0.18.3 bypasses Screen's widget render and click dispatch.
    @Inject(method = "render", at = @At("TAIL"), remap = false, require = 1)
    private void skillRespecPill$renderResetButton(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick,
            CallbackInfo callback) {
        if (skillRespecPill$resetButton != null) {
            boolean hasActivePage = optActiveCategoryData.isPresent();
            skillRespecPill$resetButton.active = hasActivePage;
            skillRespecPill$resetButton.visible = hasActivePage;
            skillRespecPill$resetButton.render(graphics, mouseX, mouseY, partialTick);
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true, remap = false, require = 1)
    private void skillRespecPill$clickResetButton(
            double mouseX,
            double mouseY,
            int button,
            CallbackInfoReturnable<Boolean> callback) {
        if (skillRespecPill$resetButton != null
                && skillRespecPill$resetButton.visible
                && skillRespecPill$resetButton.mouseClicked(mouseX, mouseY, button)) {
            callback.setReturnValue(true);
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
        ClientSkillDefinitionConfig definition = config.getDefinitionById(skill.definitionId()).orElseThrow();
        BatchPreview.Preview preview = BatchPreview.forNode(config, data, skill);
        NodeCostTooltip.Flow flow = switch (preview.action()) {
            case UNLOCK -> NodeCostTooltip.Flow.DEBIT;
            case REFUND -> NodeCostTooltip.Flow.CREDIT;
            case FORCED, CASCADE_DISABLED, INVALID_GRAPH -> NodeCostTooltip.Flow.NONE;
        };
        NodeCostTooltip.Display display = NodeCostTooltip.describe(definition.cost(), preview.points(), flow);
        Component line = Component.translatable("tooltip.skill_respec_pill.node_cost", display.nodeCost())
                .withStyle(ChatFormatting.GRAY);
        if (display.hasTotalBadge()) {
            ChatFormatting badgeColor = display.badgeColor() == NodeCostTooltip.BadgeColor.RED
                    ? ChatFormatting.RED
                    : ChatFormatting.GREEN;
            line = line.copy().append(Component.literal(" " + display.totalBadge()).withStyle(badgeColor));
        }
        lines.add(line.getVisualOrderText());
        this.setTooltipForNextRenderPass(lines);
    }
}
