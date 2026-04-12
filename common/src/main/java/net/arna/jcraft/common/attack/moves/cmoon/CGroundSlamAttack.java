package net.arna.jcraft.common.attack.moves.cmoon;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.arna.jcraft.common.entity.stand.CMoonEntity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public final class CGroundSlamAttack extends AbstractSimpleAttack<CGroundSlamAttack, CMoonEntity> {
    public CGroundSlamAttack(final int cooldown, final int windup, final int duration, final float moveDistance, final float damage, final int stun,
                             final float hitboxSize, final float knockback, final float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
    }

    @Override
    protected void processTarget(final CMoonEntity attacker, final LivingEntity target, final Vec3 kbVec, final DamageSource damageSource) {
        super.processTarget(attacker, target, kbVec, damageSource);

        final LivingEntity user = attacker.getUserOrThrow();
        GravityChangerAPI.setWorldVelocity(target, GravityChangerAPI.getGravityDirection(user).step());
        target.hurtMarked = true;
        if (user.isShiftKeyDown()) {
            target.addEffect(new MobEffectInstance(JStatusRegistry.KNOCKDOWN.get(), 30, 0, true, false));
        }
    }

    @Override
    public void performHook(final CMoonEntity attacker, final Set<LivingEntity> targets, final Set<AABB> boxes, final DamageSource damageSource, final Vec3 forwardPos, final Vec3 rotationVector) {
        final Level world = attacker.level();
        final Vec3i gravityVector = GravityChangerAPI.getGravityDirection(attacker).getNormal();

        LivingEntity user = attacker.getUserOrThrow();
        if (mayBreak(user, null)) {
            BlockPos bPos = attacker.blockPosition();

            // Adjust pancake shape for gravity
            Vec3i min = new Vec3i(-2, -2, -2);
            Vec3i max = new Vec3i(3, 3, 3);
            min = min.subtract(gravityVector);
            max = max.offset(gravityVector);

            for (int x = min.getX(); x < max.getX(); x++) {
                for (int y = min.getY(); y < max.getY(); y++) {
                    for (int z = min.getZ(); z < max.getZ(); z++) {
                        final BlockPos curPos = bPos.offset(x, y, z);

                        if (mayBreak(user, curPos, s -> s.getBlock().getExplosionResistance() <= 10 && !s.isAir())) {
                            continue;
                        }

                        final BlockState curState = world.getBlockState(curPos);
                        final FallingBlockEntity fallingBlock = FallingBlockEntity.fall(world, curPos, curState);
                        fallingBlock.setDeltaMovement(-gravityVector.getX() * 0.5, -gravityVector.getY() * 0.5, -gravityVector.getZ() * 0.5);
                        fallingBlock.time = -120;
                        fallingBlock.hurtMarked = true;
                        fallingBlock.hasImpulse = true;
                    }
                }
            }
        }

        JComponentPlatformUtils.getShockwaveHandler(attacker.level()).addShockwave(attacker.position().add(rotationVector), new Vec3(GravityChangerAPI.getGravityDirection(attacker).step()), 4.0f);
    }

    @Override
    public @NotNull MoveType<CGroundSlamAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    protected @NonNull CGroundSlamAttack getThis() {
        return this;
    }

    @Override
    public @NonNull CGroundSlamAttack copy() {
        return copyExtras(new CGroundSlamAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(),
                getStun(), getHitboxSize(), getKnockback(), getOffset()));
    }

    public static class Type extends AbstractSimpleAttack.Type<CGroundSlamAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NotNull App<RecordCodecBuilder.Mu<CGroundSlamAttack>, CGroundSlamAttack> buildCodec(RecordCodecBuilder.Instance<CGroundSlamAttack> instance) {
            return instance.group(
                    extras(), attackExtras(), cooldown(), windup(), duration(),
                    moveDistance(), damage(), stun(), hitboxSize(), knockback(), offset()
            ).apply(instance, applyAttackExtras(CGroundSlamAttack::new));
        }
    }
}
