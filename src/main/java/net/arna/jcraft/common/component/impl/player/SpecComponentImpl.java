package net.arna.jcraft.common.component.impl.player;

import lombok.NonNull;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.component.player.SpecComponent;
import net.arna.jcraft.common.spec.JSpec;
import net.arna.jcraft.common.spec.SpecType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public class SpecComponentImpl implements SpecComponent {
    private final PlayerEntity player;
    private SpecType type = SpecType.NONE;
    private JSpec<?, ?> spec;

    public SpecComponentImpl(PlayerEntity player) {
        this.player = player;
    }

    @Override
    public SpecType getType() {
        return type;
    }

    @Override
    public void setType(@NonNull SpecType type) {
        setTypeRaw(type);
        sync();
    }

    private void setTypeRaw(SpecType type) {
        this.type = type;
        spec = type.createNew(player);
    }

    @Nullable
    @Override
    public JSpec<?, ?> getSpec() {
        return spec;
    }

    private void sync() {
        JComponents.SPEC.sync(player);
    }

    @Override
    public void readFromNbt(@NonNull NbtCompound tag) {
        setTypeRaw(SpecType.fromId(tag.getInt("Type")));
    }

    @Override
    public void writeToNbt(@NonNull NbtCompound tag) {
        tag.putInt("Type", type.getId());
    }

    @Override
    public boolean shouldSyncWith(ServerPlayerEntity player) {
        return player == this.player; // Only our player needs to know, I believe.
    }
}
