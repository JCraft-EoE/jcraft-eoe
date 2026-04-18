package net.arna.jcraft.common.item;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.component.living.CommonStandComponent;
import net.arna.jcraft.api.registry.JAdvancementTriggerRegistry;
import net.arna.jcraft.api.stand.StandType;
import net.arna.jcraft.common.attack.moves.tusk.TuskActCycleMove;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.Advancement;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Upgrade Tusk N — grants the advancement that unlocks Tusk Act N+1.
 * "Upgrade Tusk 1" unlocks Act 2; "Upgrade Tusk 2" unlocks Act 3.
 */
public class TuskUpgradeItem extends Item {
    private final int targetAct;

    public TuskUpgradeItem(Properties properties, int targetAct) {
        super(properties);
        this.targetAct = targetAct;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);
        if (world.isClientSide) return InteractionResultHolder.pass(stack);
        if (!(user instanceof ServerPlayer sp)) return InteractionResultHolder.fail(stack);

        var serverAdv = sp.getServer().getAdvancements();

        String prereqId = "tusk_act" + (targetAct - 1);
        Advancement prereq = serverAdv.getAdvancement(new ResourceLocation("jcraft", prereqId));
        if (prereq == null || !sp.getAdvancements().getOrStartProgress(prereq).isDone()) {
            user.displayClientMessage(
                    Component.translatable("item.jcraft.tusk_upgrade.needs_previous").withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(stack);
        }

        ResourceLocation targetId = new ResourceLocation("jcraft", "tusk_act" + targetAct);
        Advancement target = serverAdv.getAdvancement(targetId);
        if (target != null && sp.getAdvancements().getOrStartProgress(target).isDone()) {
            user.displayClientMessage(
                    Component.translatable("item.jcraft.tusk_upgrade.already_unlocked").withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(stack);
        }

        if (targetAct == 2) {
            JAdvancementTriggerRegistry.TUSK_ACT2.trigger(sp);
        } else if (targetAct == 3) {
            JAdvancementTriggerRegistry.TUSK_ACT3.trigger(sp);
        }

        // Auto-switch to the new act if the player is currently using any Tusk act
        CommonStandComponent standData = JComponentPlatformUtils.getStandComponent(sp);
        StandType currentType = standData.getType();
        StandType act1 = TuskActCycleMove.getActStandType(1);
        StandType act2 = TuskActCycleMove.getActStandType(2);
        StandType act3 = TuskActCycleMove.getActStandType(3);
        if (currentType == act1 || currentType == act2 || currentType == act3) {
            StandType newType = TuskActCycleMove.getActStandType(targetAct);
            if (newType != null) {
                standData.setTypeAndSkin(newType, standData.getSkin());
                JUtils.maySendStandAboutInfo(sp);
                sp.unRide();
                JCraft.summon(sp.level(), sp);
            }
        }

        stack.shrink(1);
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.jcraft.tusk_upgrade_" + (targetAct - 1) + ".tooltip")
                .withStyle(ChatFormatting.GRAY));
    }
}
