package net.arna.jcraft.mixin.client;

import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Camera.class)
public interface CameraInvoker {
    @Invoker("getMaxZoom")
    double invokeClipToSpace(double desiredCameraDistance);

    @Invoker("move")
    void invokeMoveBy(double x, double y, double z);

    @Invoker("setPosition")
    void invokeSetPos(double x, double y, double z);

}
