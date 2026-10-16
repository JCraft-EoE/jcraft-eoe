package net.arna.jcraft.common.component.impl.living;

import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.component.living.CommonSpinComponent;
import net.arna.jcraft.api.registry.JTagRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

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

    @Override
    public void goldenRatioC(final int amount) {

    }

    @Override
    public void goldenRatioB(final int amount) {

    }

    @Override
    public void goldenRatioA(final int amount) {

    }

    public void tick() {
        // check the current selected slot for changes
        if (entity instanceof final Player player) {
            if (slot != null) {
                // if slot was in hotbar and changed, cancel spinning
                if (slot != Inventory.SLOT_OFFHAND && slot != (byte)player.getInventory().selected) {
                    cancelSpinning();
                }
                // else if slot was in offhand and mainhand is now hovering over valid spin item, switch
                else if (slot == Inventory.SLOT_OFFHAND) {
                    final ItemStack switcharoo = player.getMainHandItem();
                    if (!switcharoo.isEmpty() && !switcharoo.is(JTagRegistry.UNTHROWABLE) && switcharoo.getCount() == 1) {
                        setSlot((byte)player.getInventory().selected);
                    }
                }
            }
        }
        // actual spinning tick
        if (slot != null) {
            JCraft.LOGGER.info("Spinning: " + (slot == Inventory.SLOT_OFFHAND ? entity.getOffhandItem() : entity.getMainHandItem()).getItem());
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
