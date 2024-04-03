package net.arna.jcraft.common.component.impl.living;

import lombok.Getter;
import net.arna.jcraft.common.component.living.HitPropertyComponent;
import net.arna.jcraft.common.component.JComponents;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

public class HitPropertyComponentImpl implements HitPropertyComponent {
    private final Entity entity;
    private long endHitAnimTime = 0;
    @Getter
    HitPropertyComponent.HitAnimation hitAnimation = null;
    @Getter
    private Vec3d randomRotation = Vec3d.ZERO;
    public HitPropertyComponentImpl(Entity entity) {
        this.entity = entity;
    }

    @Override
    public long endHitAnimTime() {
        return endHitAnimTime - entity.getWorld().getTime();
    }

    @Override
    public void setHitAnimation(HitPropertyComponent.HitAnimation hitAnimation, int duration) {
        this.hitAnimation = hitAnimation;
        this.endHitAnimTime = entity.getWorld().getTime() + duration;
        sync();
    }

    @Override
    public void tick() { }

    private void sync() {
        JComponents.HIT_PROPERTY.sync(entity);
    }

    @Override
    public boolean shouldSyncWith(ServerPlayerEntity player) {
        return player.squaredDistanceTo(entity) <= 6400;
    }

    @Override
    public void writeSyncPacket(PacketByteBuf buf, ServerPlayerEntity recipient) {
        buf.writeVarLong(endHitAnimTime);
        if (endHitAnimTime > 0)
            buf.writeVarInt(hitAnimation.ordinal());
    }

    @Override
    public void applySyncPacket(PacketByteBuf buf) {
        endHitAnimTime = buf.readVarLong();
        if (endHitAnimTime > 0)
            hitAnimation = HitPropertyComponent.HitAnimation.values()[buf.readVarInt()];

        Random random = entity.getWorld().getRandom();
        randomRotation = new Vec3d(
                random.nextGaussian(),
                random.nextGaussian(),
                random.nextGaussian()
        );
    }

    @Override
    public void readFromNbt(NbtCompound tag) { }
    @Override
    public void writeToNbt(NbtCompound tag) { }
}
