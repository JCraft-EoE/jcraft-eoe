package net.arna.jcraft.common.attack.moves.killerqueen;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.api.component.living.CommonBombTrackerComponent;
import net.arna.jcraft.common.entity.stand.AbstractKillerQueenEntity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class BombPlantAttack<A extends IAttacker<? extends A, ?>> extends AbstractSimpleAttack<BombPlantAttack<A>, A> {
    public BombPlantAttack(final int cooldown, final int windup, final int duration, final float moveDistance,
                           final int stun, final float hitboxSize, final float offset) {
        super(cooldown, windup, duration, moveDistance, 0f, stun, hitboxSize, 0f, offset);
    }

    private static final Vec3 halfBox = new Vec3(0.5, 0.5, 0.5);

    @Override
    public @NotNull MoveType<BombPlantAttack<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final A attacker, final LivingEntity user) {
        final CommonBombTrackerComponent.BombData mainBomb = JComponentPlatformUtils.getBombTracker(user).getMainBomb();

        final Set<LivingEntity> targets = super.perform(attacker, user);

        final Vec3 rotVec = getRotVec(attacker);
        final LivingEntity baseEntity = attacker.getBaseEntity();
        final Vec3 boxCenter = baseEntity.position().add(
                RotationUtil.vecPlayerToWorld(new Vec3(0, baseEntity.getBbHeight() * 0.66, 0), GravityChangerAPI.getGravityDirection(baseEntity))
        ).add(rotVec);

        targets.stream()
                .findFirst()
                .<Entity>map(JUtils::getUserIfStand)
                .or(() -> {
                    // If none are found, re-do an optimized hitbox check for any entity type
                    List<Entity> hit = baseEntity.level().getEntitiesOfClass(Entity.class,
                            new AABB(boxCenter.subtract(halfBox), boxCenter.add(halfBox)),
                            EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(e -> e != baseEntity && e != user));
                    return hit.isEmpty() ? Optional.empty() : Optional.of(hit.get(0));
                })
                .ifPresentOrElse(mainBomb::setBomb, () -> {
                    // If none are found again, try to place the bomb on the wall
                    BlockPos closePos = BlockPos.containing(boxCenter.subtract(rotVec));
                    BlockPos farPos = BlockPos.containing(boxCenter);
                    BlockState blockState = baseEntity.level().getBlockState(closePos);
                    if (blockState.isAir()) {
                        blockState = baseEntity.level().getBlockState(farPos);
                        if (!blockState.isAir()) {
                            mainBomb.setBomb(farPos);
                        }
                    } else {
                        mainBomb.setBomb(closePos);
                    }
                });

        return targets;
    }

    @Override
    protected @NonNull BombPlantAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull BombPlantAttack<A> copy() {
        return copyExtras(new BombPlantAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getStun(),
                getHitboxSize(), getOffset()));
    }

    public static class Type extends AbstractSimpleAttack.Type<BombPlantAttack<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NotNull App<RecordCodecBuilder.Mu<BombPlantAttack<?>>, BombPlantAttack<?>> buildCodec(RecordCodecBuilder.Instance<BombPlantAttack<?>> instance) {
            return instance.group(extras(), attackExtras(), cooldown(), windup(), duration(), moveDistance(), stun(), hitboxSize(), offset())
                    .apply(instance, applyAttackExtras(BombPlantAttack::new));
        }
    }
}
