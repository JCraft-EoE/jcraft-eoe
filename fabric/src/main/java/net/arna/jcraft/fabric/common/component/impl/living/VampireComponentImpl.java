package net.arna.jcraft.fabric.common.component.impl.living;

import lombok.NonNull;
import net.arna.jcraft.common.component.impl.living.CommonVampireComponentImpl;
import net.arna.jcraft.fabric.common.component.JComponents;
import net.arna.jcraft.fabric.common.component.living.VampireComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class VampireComponentImpl extends CommonVampireComponentImpl implements VampireComponent {
    public VampireComponentImpl(LivingEntity entity) {
        super(entity);
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void sync(Entity entity) {
        JComponents.VAMPIRE.sync(entity);
    }

    @Override
    public boolean shouldSyncWith(ServerPlayer player) {
        return super.shouldSyncWith(player);
    }

    @Override
    public void writeSyncPacket(FriendlyByteBuf buf, ServerPlayer recipient) {
        super.writeSyncPacket(buf, recipient);
    }

    @Override
    public void applySyncPacket(FriendlyByteBuf buf) {
        super.applySyncPacket(buf);
    }

    @Override
    public void readFromNbt(@NonNull CompoundTag tag) {
        super.readFromNbt(tag);
    }

    @Override
    public void writeToNbt(@NonNull CompoundTag tag) {
        super.writeToNbt(tag);
    }
}
