package net.arna.jcraft.client.mixin.gravity;

import net.minecraft.client.network.OtherClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(OtherClientPlayerEntity.class)
public abstract class OtherClientPlayerEntityMixin {
   // @Override
   // public Direction gravitychanger$getGravityDirection() {
   //     return this.gravitychanger$getTrackedGravityDirection();
   // }
//
   // @Override
   // public void gravitychanger$setGravityDirection(Direction gravityDirection, boolean initialGravity) {
   //     this.gravitychanger$setTrackedGravityDirection(gravityDirection);
   // }
}
