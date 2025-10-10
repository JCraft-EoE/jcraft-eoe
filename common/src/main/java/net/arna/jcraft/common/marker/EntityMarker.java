package net.arna.jcraft.common.marker;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.common.util.NbtUtils;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.UUID;

public record EntityMarker(UUID id, CompoundTag state) implements Marker<UUID, EntityMarker> {

    public static final @NonNull Codec<EntityMarker> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    UUIDUtil.CODEC.fieldOf("id").forGetter(EntityMarker::id),
                    CompoundTag.CODEC.fieldOf("state").forGetter(EntityMarker::state))
            .apply(instance, EntityMarker::new));

    @Override
    public @NonNull UUID getId() {
        return id;
    }

    @Override
    public @NonNull Codec<EntityMarker> getCodec() {
        return CODEC;
    }

    public @NonNull Optional<Vec3> extractPosition() {
        if (!state.contains(Identifiers.POSITION.toString())) {
            return Optional.empty();
        }
        return Optional.of(NbtUtils.getVec3(state, Identifiers.POSITION.toString()));
    }
}
