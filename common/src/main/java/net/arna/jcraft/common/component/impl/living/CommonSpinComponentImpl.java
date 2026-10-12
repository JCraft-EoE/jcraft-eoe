package net.arna.jcraft.common.component.impl.living;

import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.component.living.CommonSpinComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public abstract class CommonSpinComponentImpl implements CommonSpinComponent {

    protected final LivingEntity entity;

    @Getter
    protected Byte slot;

    protected CommonSpinComponentImpl(final @NonNull LivingEntity entity) {
        this.entity = entity;
    }

    @Override
    public void setSlot(final Byte slot) {
        this.slot = slot;
        sync(entity);
    }

    @Override
    public void cancelSpinning() {
        this.slot = null;
        sync(entity);
    }

    @Override
    public void endSpinning() {
        this.slot = null;
        sync(entity);
    }

    public void tick() {
        if (entity instanceof final Player player) {
            if (slot != null && slot != 0 && slot != (byte)player.getInventory().selected) {
                cancelSpinning();
            }
        }
        if (slot != null) {
            JCraft.LOGGER.info("Spinning");
        }
    }

    public void sync(final Entity entity) {
        // intentionally left empty
    }

    public boolean shouldSyncWith(final ServerPlayer player) {
        return true;
    }

    public void writeSyncPacket(final FriendlyByteBuf buf, final ServerPlayer recipient) {
        buf.writeBoolean(slot != null);
        if (slot != null) {
            buf.writeByte(slot);
        }
    }

    public void applySyncPacket(final FriendlyByteBuf buf) {
        if (buf.readBoolean()) {
            slot = buf.readByte();
        }
        else {
            slot = null;
        }
    }

    public void readFromNbt(final @NonNull CompoundTag tag) {
        // intentionally left empty
    }

    public void writeToNbt(final @NonNull CompoundTag tag) {
        // intentionally left empty
    }

}
