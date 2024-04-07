package net.arna.jcraft.common.component.impl.entity;

import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.component.entity.TimeStopComponent;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.arna.jcraft.common.util.JUtils.*;

@Getter
public class TimeStopComponentImpl implements TimeStopComponent {
    private final Entity entity;
    private int ticks;
    // Launch buildup implementation, because players are special snowflakes and don't build it in timestop like enemies do
    private Vec3d totalVelocity = Vec3d.ZERO;

    public TimeStopComponentImpl(Entity entity) {
        this.entity = entity;
    }

    @Override
    public void setTicks(int ticks) {
        if (this.ticks <= 0) // If just beginning a new Timestop
            totalVelocity = Vec3d.ZERO;
        this.ticks = ticks;
        JComponents.TIME_STOP.sync(entity);
    }

    @Override
    public void addTotalVelocity(Vec3d vel) {
        totalVelocity = totalVelocity.add(vel);
    }

    @Override
    public void tick(CallbackInfo ci) {
        if (ticks <= 0) return;

        stopTick(entity);
        for (Entity passenger : entity.getPassengerList()) stopTick(passenger);
        ticks--;

        if (ticks == 0 && totalVelocity.lengthSquared() > 0.01) {
            // Lift off ground to stop friction from cutting the launch short
            Vec3i localUp = GravityChangerAPI.getGravityDirection(entity).getOpposite().getVector();
            double upX = entity.getX() + localUp.getX() * 0.1;
            double upY = entity.getY() + localUp.getY() * 0.1;
            double upZ = entity.getZ() + localUp.getZ() * 0.1;
            entity.teleport(upX, upY, upZ);
            entity.setOnGround(false);

            addVelocity(entity, totalVelocity.x, totalVelocity.y, totalVelocity.z);
        }

        ci.cancel();
    }

    @Override
    public void readFromNbt(@NonNull NbtCompound tag) {
    }

    @Override
    public void writeToNbt(@NonNull NbtCompound tag) {

    }
}
