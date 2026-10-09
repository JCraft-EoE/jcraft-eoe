package net.arna.jcraft.common.component.impl.living;

import lombok.NonNull;
import net.arna.jcraft.api.component.living.CommonSpinComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public abstract class CommonSpinComponentImpl implements CommonSpinComponent {

    protected final LivingEntity entity;

    protected CommonSpinComponentImpl(final @NonNull LivingEntity entity) {
        this.entity = entity;
    }

    public void sync(final Entity entity) {
        // intentionally left empty
    }

    public boolean shouldSyncWith(final ServerPlayer player) {
        return true;
    }

    public void writeSyncPacket(final FriendlyByteBuf buf, final ServerPlayer recipient) {
        // intentionally left empty
    }

    public void applySyncPacket(final FriendlyByteBuf buf) {
        // intentionally left empty
    }

    public void readFromNbt(final @NonNull CompoundTag tag) {
        // intentionally left empty
    }

    public void writeToNbt(final @NonNull CompoundTag tag) {
        // intentionally left empty
    }

}
