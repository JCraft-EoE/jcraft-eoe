package net.arna.jcraft.client.mixin;

import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Camera.class)
public interface CameraInvoker {
    @Invoker("clipToSpace")
    double invokeClipToSpace(double desiredCameraDistance);

    @Invoker("moveBy")
    void invokeMoveBy(double x, double y, double z);

    @Invoker("setPos")
    void invokeSetPos(Vec3d pos);

}
