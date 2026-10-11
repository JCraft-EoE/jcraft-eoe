package net.arna.jcraft.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.Window;
import net.arna.jcraft.api.attack.moves.AbstractTossMove;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.client.rendering.api.callbacks.DisplayResizeCallback;
import net.arna.jcraft.common.attack.moves.ranger.RangerFocusMove;
import net.arna.jcraft.common.entity.stand.AerosmithEntity;
import net.arna.jcraft.common.item.Peacemaker;
import net.arna.jcraft.common.spec.RangerSpec;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Shadow @Final private Window window;

    @Shadow
    @Nullable
    public LocalPlayer player;

    @Inject(method = "resizeDisplay", at = @At("RETURN"))
    private void jcraft$onResizeDisplay(final CallbackInfo ci) {
        DisplayResizeCallback.EVENT.invoker().onResolutionChanged(window.getWidth(), window.getHeight());
    }

    /**
     * Makes Aerosmith appear glowing (outline visible through blocks) only to its user's client,
     * and only while it is flying remotely without a direct line of sight to the user.
     *
     * @return
     */
    @ModifyReturnValue(method = "shouldEntityAppearGlowing", at = @At("RETURN"))
    private boolean jcraft$aerosmithUserGlow(final boolean original, final Entity entity) {
        final LocalPlayer player = ((Minecraft)(Object)this).player;
        return original || (entity instanceof final AerosmithEntity aerosmith &&
            aerosmith.isRemote() && player != null &&
            JUtils.getStand(player) == aerosmith &&
            !player.hasLineOfSight(aerosmith));
    }

    @WrapOperation(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;pickBlock()V"))
    private void jcraft$ignorePickBlockWhenStandOut(Minecraft instance, Operation<Void> original) {
        // A held gun spends the pick block key on reloading instead.
        if (instance.player != null && instance.player.getMainHandItem().getItem() instanceof Peacemaker) {
            return;
        }

        final StandEntity<?,?> stand = JUtils.getStand(instance.player);
        if (stand != null && stand.getMove(AbstractTossMove.class) != null) {
            return;
        }
        original.call(instance);
    }

    /**
     * Left click works the gun through jcraft's own input map, so vanilla's attack must not also
     * run: without this the shooter punches, swings and burns melee cooldown while cocking.
     *
     * @return
     */
    @WrapMethod(method = "startAttack")
    private boolean jcraft$noMeleeWithGun(final Operation<Boolean> original) {
        if (player != null && player.getMainHandItem().getItem() instanceof Peacemaker) {
            return false;
        }
        return original.call();
    }

    @ModifyReturnValue(method = "shouldEntityAppearGlowing", at = @At("RETURN"))
    private boolean jcraft$makeAerosmithGlowWhenNotInLos(boolean original, @Local(argsOnly = true) Entity entity) {
        return original || entity instanceof AerosmithEntity as && as.isRemote() && !as.isInLineOfSight() && as.getUser() == player;
    }

    // Highlights targets of the ranger's Focus, only on the focusing player's client
    @SuppressWarnings("ConstantValue")
    @ModifyReturnValue(method = "shouldEntityAppearGlowing", at = @At("RETURN"))
    private boolean jcraft$rangerFocusGlow(boolean original, @Local(argsOnly = true) Entity entity) {
        if (original || player == null) return original;
        return JUtils.getSpec(player) instanceof RangerSpec &&
                JComponentPlatformUtils.getGunslinger(player).isFocusActive() &&
                RangerFocusMove.selectOutlineTarget(player) == entity;
    }

}
