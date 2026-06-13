package net.arna.jcraft.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.Window;
import net.arna.jcraft.api.attack.moves.AbstractTossMove;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.client.rendering.api.callbacks.DisplayResizeCallback;
import net.arna.jcraft.common.entity.stand.AerosmithEntity;
import net.arna.jcraft.common.util.JUtils;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MinecraftMixin {
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
     */
    @Inject(method = "shouldEntityAppearGlowing", at = @At("RETURN"), cancellable = true)
    private void jcraft$aerosmithUserGlow(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) return; // already glowing for another reason

        if (!(entity instanceof AerosmithEntity aerosmith)) return;
        if (!aerosmith.isRemote()) return;

        final LocalPlayer player = ((Minecraft) (Object) this).player;
        if (player == null) return;
        if (JUtils.getStand(player) != aerosmith) return;
        if (player.hasLineOfSight(aerosmith)) return;

        cir.setReturnValue(true);
    }

    @WrapOperation(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;pickBlock()V"))
    private void jcraft$ignorePickBlockWhenStandOut(Minecraft instance, Operation<Void> original) {
        final StandEntity<?,?> stand = JUtils.getStand(instance.player);
        if (stand != null && stand.getMove(AbstractTossMove.class) != null) {
            return;
        }
        original.call(instance);
    }

    @ModifyReturnValue(method = "shouldEntityAppearGlowing", at = @At("RETURN"))
    private boolean jcraft$makeAerosmithGlowWhenNotInLos(boolean original, @Local(argsOnly = true) Entity entity) {
        return original || entity instanceof AerosmithEntity as && !as.isInLineOfSight() && as.getUser() == player;
    }
}
