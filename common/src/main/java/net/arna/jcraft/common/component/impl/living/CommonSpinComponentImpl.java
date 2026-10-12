package net.arna.jcraft.common.component.impl.living;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.component.living.CommonSpinComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public abstract class CommonSpinComponentImpl implements CommonSpinComponent {

    protected final LivingEntity entity;

    @Getter
    @Setter
    protected InteractionHand itemHand = null;

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
        // TODO implement
    }

    public void applySyncPacket(final FriendlyByteBuf buf) {
        // TODO implement
    }

    public void readFromNbt(final @NonNull CompoundTag tag) {
        // intentionally left empty
    }

    public void writeToNbt(final @NonNull CompoundTag tag) {
        // intentionally left empty
    }

    public void tick() {
        JCraft.LOGGER.warn("I'm ticking!");
    }

}
