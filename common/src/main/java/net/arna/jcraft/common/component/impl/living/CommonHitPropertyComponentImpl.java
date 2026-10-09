package net.arna.jcraft.common.component.impl.living;

import lombok.Getter;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public abstract class CommonHitPropertyComponentImpl implements CommonHitPropertyComponent {
    protected final Entity entity;
    protected long endHitAnimTime = 0;
    @Getter
    protected CommonHitPropertyComponent.HitAnimation hitAnimation = null;
    @Getter
    protected Vec3 randomRotation = Vec3.ZERO;
    protected final Random random;

    protected CommonHitPropertyComponentImpl(final Entity entity) {
        this.entity = entity;
        this.random = new Random();
    }

    @Override
    public long endHitAnimTime() {
        return endHitAnimTime - entity.level().getGameTime();
    }

    @Override
    public void setHitAnimation(final CommonHitPropertyComponent.HitAnimation hitAnimation, final int duration) {
        this.hitAnimation = hitAnimation;
        this.endHitAnimTime = entity.level().getGameTime() + duration;
        sync(entity);
    }

    public void tick() {
    }

    public void sync(final Entity entity) {
    }

    public boolean shouldSyncWith(final ServerPlayer player) {
        return player.distanceToSqr(entity) <= 6400;
    }

    public void writeSyncPacket(final FriendlyByteBuf buf, final ServerPlayer recipient) {
        buf.writeVarLong(endHitAnimTime);
        if (endHitAnimTime > 0) {
            buf.writeVarInt(hitAnimation.ordinal());
        }
    }

    public void applySyncPacket(final FriendlyByteBuf buf) {
        endHitAnimTime = buf.readVarLong();
        if (endHitAnimTime > 0) {
            hitAnimation = CommonHitPropertyComponent.HitAnimation.values()[buf.readVarInt()];
        }

        randomRotation = new Vec3(
                random.nextGaussian(),
                random.nextGaussian(),
                random.nextGaussian()
        );
    }

    public void readFromNbt(final CompoundTag tag) {
        if (tag.isEmpty()) return;
        endHitAnimTime = tag.getLong("EndTime");
        hitAnimation = CommonHitPropertyComponent.HitAnimation.values()[tag.getInt("AnimIndex")];
    }

    public void writeToNbt(final CompoundTag tag) {
        if (hitAnimation == null) return;
        tag.putLong("EndTime", endHitAnimTime);
        tag.putInt("AnimIndex", hitAnimation.ordinal());
    }
}
