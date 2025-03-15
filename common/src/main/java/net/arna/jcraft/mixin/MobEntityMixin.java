package net.arna.jcraft.mixin;

import net.arna.jcraft.common.entity.Behavior;
import net.arna.jcraft.common.entity.BrainType;
import net.arna.jcraft.common.item.BrainDiscItem;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.arna.jcraft.registry.JItemRegistry;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public class MobEntityMixin {
    @Inject(method = "getControllingPassenger", at = @At("HEAD"), cancellable = true)
    private void jcraft$dontDisableAI(CallbackInfoReturnable<LivingEntity> cir) {
        if (JComponentPlatformUtils.getStandData(Mob.class.cast(this)).getStand() != null)
            cir.setReturnValue(null);
    }

    @Inject(method = "mobInteract", at = @At("RETURN"), cancellable = true)
    private void jcraft$brainExchange(final Player player, final InteractionHand hand, final CallbackInfoReturnable<InteractionResult> cir) {
        if (cir.getReturnValue() != InteractionResult.PASS) {
            return;
        }
        ItemStack disc = player.getItemInHand(hand);
        if (disc.is(JItemRegistry.BRAIN_DISC.get())) {
            final Mob mob = (Mob)(Object)this;
            final BrainType mobBrainType = BrainType.find(mob);
            final BrainType discBrainType = BrainDiscItem.getBrainType(disc);
            // TODO the check if the mob is lobotomized should be done via a component on the mob
            if (discBrainType == null && mob.brain != BrainDiscItem.NO_BRAINER && mobBrainType != null) {
                mob.brain = BrainDiscItem.NO_BRAINER;
                mob.removeAllGoals(goal -> true);
                mob.removeFreeWill();
                player.setItemInHand(hand, BrainDiscItem.createDiscStack(mobBrainType));
                cir.setReturnValue(InteractionResult.SUCCESS);
            }
            else if (discBrainType != null && mob.brain == BrainDiscItem.NO_BRAINER) {
                final Behavior newBehavior = discBrainType.createBehavior(mob.level());
                if (newBehavior != null) {
                    mob.brain = newBehavior.brain();
                    mob.goalSelector = newBehavior.goalSelector();
                    mob.targetSelector = newBehavior.targetSelector();
                    player.setItemInHand(hand, BrainDiscItem.createDiscStack(null));
                    cir.setReturnValue(InteractionResult.SUCCESS);
                }
            }
        }
    }
}
