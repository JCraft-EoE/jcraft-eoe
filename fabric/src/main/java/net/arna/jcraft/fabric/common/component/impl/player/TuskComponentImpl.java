package net.arna.jcraft.fabric.common.component.impl.player;

import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.component.impl.player.CommonTuskComponentImpl;
import net.arna.jcraft.fabric.common.component.player.TuskComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class TuskComponentImpl extends CommonTuskComponentImpl implements TuskComponent {
    private final Player player;

    public TuskComponentImpl(Player player) {
        super(player);
        this.player = player;
    }

    @Override
    public void sync(Entity entity) {
        // Use ComponentRegistry.getOrCreate to avoid circular dependency with JComponents.TUSK
        ComponentRegistry.getOrCreate(JCraft.id("tusk"), TuskComponent.class).sync(player);
    }

    @Override
    public void readFromNbt(@NonNull CompoundTag tag) {
        super.readFromNbt(tag);
    }

    @Override
    public void writeToNbt(@NonNull CompoundTag tag) {
        super.writeToNbt(tag);
    }

    @Override
    public boolean shouldSyncWith(ServerPlayer player) {
        return super.shouldSyncWith(player);
    }
}