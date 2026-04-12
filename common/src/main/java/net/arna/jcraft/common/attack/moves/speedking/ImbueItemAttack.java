package net.arna.jcraft.common.attack.moves.speedking;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.entity.stand.SpeedKingEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class ImbueItemAttack extends AbstractMove<ImbueItemAttack, SpeedKingEntity> {

    public ImbueItemAttack(final int cooldown, final int windup, final int duration, final float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
    }

    @Override
    public @NonNull MoveType<ImbueItemAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final SpeedKingEntity attacker, final LivingEntity user) {
        final Level world = attacker.level();
        final Vec3 standPos = attacker.position();
        final boolean crouching = user.isShiftKeyDown();

        if (crouching) {
            imbueBlocks(world, standPos);
        } else {
            imbueItems(world, standPos, user);
        }

        return Set.of();
    }

    private void imbueItems(Level world, Vec3 centerPos, LivingEntity user) {
        AABB searchArea = new AABB(centerPos.add(-5, -3, -5), centerPos.add(5, 3, 5));
        List<ItemEntity> nearbyItems = world.getEntitiesOfClass(ItemEntity.class, searchArea, EntitySelector.ENTITY_STILL_ALIVE);

        for (ItemEntity item : nearbyItems) {
            item.getItem().getOrCreateTag().putBoolean("SpeedKingHeated", true);
            item.getItem().getOrCreateTag().putLong("HeatedTime", world.getGameTime());
            item.getItem().getOrCreateTag().putUUID("SpeedKingUser", user.getUUID());
            item.setSecondsOnFire(1);
        }
    }

    private void imbueBlocks(Level world, Vec3 centerPos) {
        final int radius = 3;
        final BlockPos center = new BlockPos((int) centerPos.x, (int) centerPos.y, (int) centerPos.z);

        for (int x = -radius; x <= radius; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    BlockState state = world.getBlockState(pos);

                    if (state.hasProperty(BlockStateProperties.LIT) && !state.getValue(BlockStateProperties.LIT)) {
                        world.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.LIT, true));
                    }
                }
            }
        }
    }

    @Override
    protected @NonNull ImbueItemAttack getThis() {
        return this;
    }

    @Override
    public @NonNull ImbueItemAttack copy() {
        return copyExtras(new ImbueItemAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }

    public static class Type extends AbstractMove.Type<ImbueItemAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<ImbueItemAttack>, ImbueItemAttack> buildCodec(RecordCodecBuilder.Instance<ImbueItemAttack> instance) {
            return baseDefault(instance, ImbueItemAttack::new);
        }
    }
}