package net.arna.jcraft.common.component.impl.player;

import lombok.NonNull;
import net.arna.jcraft.api.registry.JAdvancementTriggerRegistry;
import net.arna.jcraft.api.spec.SpecType;
import net.arna.jcraft.api.spec.SpecTypeUtil;
import net.arna.jcraft.api.component.player.CommonSpecComponent;
import net.arna.jcraft.api.spec.JSpec;
import net.arna.jcraft.api.registry.JSpecTypeRegistry;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public abstract class CommonSpecComponentImpl implements CommonSpecComponent {
    protected final LivingEntity user;
    private SpecType type = JSpecTypeRegistry.NONE.get();
    private JSpec<?, ?> spec;

    public CommonSpecComponentImpl(final LivingEntity livingEntity) {
        this.user = livingEntity;
    }

    @Override
    public SpecType getType() {
        return type;
    }

    @Override
    public void setType(final SpecType type) {
        setTypeRaw(type, false);
        sync(user);
    }

    private void setTypeRaw(final SpecType type, final boolean loading) {
        this.type = type;
        spec = type == null ? null : type.createSpec(user);
        if (!SpecTypeUtil.isNone(type) && user instanceof ServerPlayer player) {
            if (!loading) {
                JUtils.maySendSpecAboutInfo(player);
            }
            JAdvancementTriggerRegistry.OBTAINED_SPEC.trigger(player, type);
        }
    }

    @Nullable
    @Override
    public JSpec<?, ?> getSpec() {
        return spec;
    }

    public void sync(final Entity entity) {
    }

    public void readFromNbt(final @NonNull CompoundTag tag) {
        SpecType type = SpecTypeUtil.readFromNBT(tag, "Type");
        if (type != null) {
            setTypeRaw(type, true);
        }
    }

    public void writeToNbt(final @NonNull CompoundTag tag) {
        if (type != null) {
            tag.putString("Type", type.getId().toString());
        }
    }

    public boolean shouldSyncWith(final ServerPlayer player) {
        return player == this.user; // Only our player needs to know.
    }
}
