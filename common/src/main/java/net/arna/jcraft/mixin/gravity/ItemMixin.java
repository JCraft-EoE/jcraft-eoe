package net.arna.jcraft.mixin.gravity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Item.class)
public abstract class ItemMixin {

    @WrapOperation(
            method = "getPlayerPOVHitResult",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;getYRot()F",
                    ordinal = 0
            )
    )
    private static float jcraft$raycast_getYaw(final Player instance, final Operation<Float> original) {
        final Direction direction = GravityChangerAPI.getGravityDirection(instance);
        if (direction == Direction.DOWN) {
            return original.call(instance);
        }
        return RotationUtil.rotPlayerToWorld(original.call(instance), instance.getXRot(), direction).x;
    }

    @WrapOperation(
            method = "getPlayerPOVHitResult",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;getXRot()F",
                    ordinal = 0
            )
    )
    private static float jcraft$raycast_getPitch(final Player instance, final Operation<Float> original) {
        final Direction direction = GravityChangerAPI.getGravityDirection(instance);
        if (direction == Direction.DOWN) {
            return original.call(instance);
        }
        return RotationUtil.rotPlayerToWorld(instance.getYRot(), original.call(instance), direction).y;
    }

}
