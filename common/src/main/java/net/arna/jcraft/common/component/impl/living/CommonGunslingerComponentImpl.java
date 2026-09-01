package net.arna.jcraft.common.component.impl.living;

import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.api.component.living.CommonGunslingerComponent;
import net.arna.jcraft.common.spec.RangerSpec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class CommonGunslingerComponentImpl implements CommonGunslingerComponent {

    private final LivingEntity entity;
    @Getter
    private @NonNull ItemStack holsteredItem = ItemStack.EMPTY;
    // only needs to be synchronized to the client, not persisted
    @Getter
    private boolean focusActive = false;
    // only needs to be synchronized to the client, not persisted
    @Getter
    private float focus = RangerSpec.MAX_FOCUS;

    public CommonGunslingerComponentImpl(final LivingEntity entity) {
        this.entity = entity;
    }

    @Override
    public void setHolsteredItem(final @NonNull ItemStack stack) {
        holsteredItem = stack;
    }

    @Override
    public void setFocusActive(final boolean active) {
        focusActive = active;
        sync(entity);
    }

    @Override
    public void setFocus(final float focus) {
        this.focus = focus;
        sync(entity);
    }

    public void sync(final Entity entity) {
    }

    public boolean shouldSyncWith(final ServerPlayer player) {
        return true;
    }

    public void writeSyncPacket(FriendlyByteBuf buf, ServerPlayer recipient) {
        buf.writeBoolean(focusActive);
        buf.writeFloat(focus);
    }

    public void applySyncPacket(FriendlyByteBuf buf) {
        focusActive = buf.readBoolean();
        focus = buf.readFloat();
    }

    @Override
    public void readFromNbt(@NonNull CompoundTag tag) {
        holsteredItem = ItemStack.of(tag.getCompound("HolsteredItem"));
    }

    @Override
    public void writeToNbt(@NonNull CompoundTag tag) {
        if (!holsteredItem.isEmpty()) {
            tag.put("HolsteredItem", holsteredItem.save(new CompoundTag()));
        }
    }

}
