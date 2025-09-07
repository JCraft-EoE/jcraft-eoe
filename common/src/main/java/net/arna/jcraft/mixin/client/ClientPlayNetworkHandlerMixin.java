package net.arna.jcraft.mixin.client;

import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ClientPacketListener.class)
public abstract class ClientPlayNetworkHandlerMixin {
    /*
    @Final
    @Shadow
    private Minecraft minecraft;
    @Shadow
    private ClientLevel level;

    @Inject(method = "handleMoveEntity", at = @At("HEAD"), cancellable = true)
    private void jcraft$handlePrediction(ClientboundMoveEntityPacket packet, CallbackInfo ci) {
        if (JClientConfig.getInstance().isClientsidePrediction()) {
            ClientPacketListener listener = ClientPacketListener.class.cast(this);
            if (minecraft.isSingleplayer()) return;
            ServerData server = minecraft.getCurrentServer();
            if (server == null) return;
            long ping = server.ping;

            PacketUtils.ensureRunningOnSameThread(packet, listener, this.minecraft);
            Entity entity = packet.getEntity(this.level);

            if (entity != null) {
                if (!entity.isControlledByLocalInstance()) {
                    if (packet.hasPosition()) {
                        VecDeltaCodec vecDeltaCodec = entity.getPositionCodec();
                        Vec3 newPos = vecDeltaCodec.decode(packet.getXa(), packet.getYa(), packet.getZa());
                        vecDeltaCodec.setBase(newPos);
                        float f = packet.hasRotation() ? (float)(packet.getyRot() * 360) / 256.0F : entity.getYRot();
                        float g = packet.hasRotation() ? (float)(packet.getxRot() * 360) / 256.0F : entity.getXRot();
                        Vec3 deltaPos = newPos.subtract(entity.position());
                        deltaPos.scale((ping) / 50.0); // estimated time to arrival in ticks
                        entity.lerpTo(
                                newPos.x() + deltaPos.x,
                                newPos.y() + deltaPos.y,
                                newPos.z() + deltaPos.z,
                                f, g, 2, false);
                    } else if (packet.hasRotation()) {
                        float h = (float)(packet.getyRot() * 360) / 256.0F;
                        float i = (float)(packet.getxRot() * 360) / 256.0F;
                        entity.lerpTo(entity.getX(), entity.getY(), entity.getZ(), h, i, 2, false);
                    }

                    entity.setOnGround(packet.isOnGround());
                }
            }
            ci.cancel();
        }
    }

    @ModifyArg(method = "handleTeleportEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;lerpTo(DDDFFIZ)V"), index = 5)
    private int modifyInterpolationSteps(int original) {
        if (JClientConfig.getInstance().isClientsidePrediction()) {
            return 2; // Originally 3
        }
        return original;
    }
     */
}
