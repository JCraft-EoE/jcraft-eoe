package net.arna.jcraft.common.component.impl.entity;

import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.api.component.entity.CommonTimeStopComponent;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.arna.jcraft.common.util.JUtils.addVelocity;
import static net.arna.jcraft.common.util.JUtils.stopTick;

@Getter
public abstract class CommonTimeStopComponentImpl implements CommonTimeStopComponent {
    private final Entity entity;
    private int ticks;
    // Launch buildup implementation, because players are special snowflakes and don't build it in timestop like enemies do
    private Vec3 totalVelocity = Vec3.ZERO;

    protected CommonTimeStopComponentImpl(final Entity entity) {
        this.entity = entity;
    }

    @Override
    public void setTicks(final int ticks) {
        if (this.ticks <= 0) // If just beginning a new Timestop
        {
            totalVelocity = Vec3.ZERO;
        }
        this.ticks = ticks;
        sync(entity);
    }

    public void sync(final Entity entity) {
    }

    @Override
    public void addTotalVelocity(final Vec3 vel) {
        totalVelocity = totalVelocity.add(vel);
    }

    @Override
    public void tick(final CallbackInfo ci) {
        if (ticks <= 0) {
            return;
        }

        stopTick(entity);
        for (final Entity passenger : entity.getPassengers()) {
            stopTick(passenger);
        }
        ticks--;

        if (ticks == 0 && totalVelocity.lengthSqr() > 0.01) {
            // Lift off ground to stop friction from cutting the launch short
            Vec3i localUp = GravityChangerAPI.getGravityDirection(entity).getOpposite().getNormal();
            double upX = entity.getX() + localUp.getX() * 0.1;
            double upY = entity.getY() + localUp.getY() * 0.1;
            double upZ = entity.getZ() + localUp.getZ() * 0.1;
            entity.teleportToWithTicket(upX, upY, upZ);
            entity.setOnGround(false);

            addVelocity(entity, totalVelocity.x, totalVelocity.y, totalVelocity.z);
        }

        ci.cancel();
    }

    public void readFromNbt(final @NonNull CompoundTag tag) {
        ticks = tag.getInt("Ticks");
    }

    public void writeToNbt(final @NonNull CompoundTag tag) {
        tag.putInt("Ticks", ticks);
    }
}
