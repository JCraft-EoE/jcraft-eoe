package net.arna.jcraft.common.component.impl.player;

import lombok.NonNull;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.component.player.PhComponent;
import net.arna.jcraft.common.component.player.SpecComponent;
import net.arna.jcraft.common.spec.JSpec;
import net.arna.jcraft.common.spec.SpecType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public class PhComponentImpl implements PhComponent {
    private final PlayerEntity player;
    private int level = 0;

    public PhComponentImpl(PlayerEntity player) {
        this.player = player;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public void increaseLevel() {
        level++;
    }

    @Override
    public void resetLevel() {
        level = 0;
    }

    private void sync() {
        JComponents.PH.sync(player);
    }

    @Override
    public void readFromNbt(@NonNull NbtCompound tag) {
        level = tag.getInt("level");
    }

    @Override
    public void writeToNbt(@NonNull NbtCompound tag) {
        tag.putInt("level", level);
    }

    @Override
    public boolean shouldSyncWith(ServerPlayerEntity player) {
        return player == this.player; // Only our player needs to know, I believe.
    }
}
