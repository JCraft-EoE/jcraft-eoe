package net.arna.jcraft.common.component.impl.living;

import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.component.living.StandComponent;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.entity.stand.StandType;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

public class StandComponentImpl implements StandComponent {
    private final Entity entity;
    private StandEntity<?, ?> stand;
    @Getter
    private StandType type;
    @Getter
    private int skin;

    public StandComponentImpl(Entity entity) {
        this.entity = entity;
    }

    @Override
    public void setTypeAndSkin(@Nullable StandType type, int skin) {
        this.type = type;
        this.skin = skin;
        sync();
    }

    @Override
    public void setSkin(int skin) {
        if (type == null) return;

        this.skin = MathHelper.clamp(skin, 0, type.getSkinCount());
        sync();
    }

    @Override
    public void setStand(@Nullable StandEntity<?, ?> stand) {
        this.stand = stand;
        sync();
    }

    @Nullable
    @Override
    public StandEntity<?, ?> getStand() {
        if (stand != null && !stand.isAlive())
            setStand(null);
        // Checks if the stand user has a passenger, and updates the stand if the passenger and stand do not match
        if (entity.getFirstPassenger() instanceof StandEntity<?, ?> passenger && stand != passenger)
            setStand(passenger);
        // Otherwise, returns the stored stand value
        return stand;
    }

    private void sync() {
        JComponents.STAND.sync(entity);
    }

    @Override
    public void readFromNbt(@NonNull NbtCompound tag) {
        int rawType = tag.getInt("Type");
        type = rawType == 0 ? null : StandType.fromId(rawType);
        skin = tag.getInt("Skin");
    }

    @Override
    public void writeToNbt(@NonNull NbtCompound tag) {
        tag.putInt("Type", type == null ? 0 : type.getId());
        tag.putInt("Skin", skin);
    }

    @Override
    public void applySyncPacket(PacketByteBuf buf) {
        StandComponent.super.applySyncPacket(buf);

        Entity entity = buf.readBoolean() ? this.entity.getWorld().getEntityById(buf.readVarInt()) : null;
        if (entity == null || entity instanceof StandEntity<?,?>) stand = (StandEntity<?, ?>) entity;
    }

    @Override
    public void writeSyncPacket(PacketByteBuf buf, ServerPlayerEntity recipient) {
        StandComponent.super.writeSyncPacket(buf, recipient);

        buf.writeBoolean(stand != null);
        if (stand != null) buf.writeVarInt(stand.getId());
    }
}
