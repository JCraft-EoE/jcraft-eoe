package net.arna.jcraft.common.attack.moves.thefool;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.api.registry.JBlockRegistry;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class SandstormAttack<A extends IAttacker<? extends A, ?>> extends AbstractSimpleAttack<SandstormAttack<A>, A> {
    private WeakReference<LivingEntity> superTarget = new WeakReference<>(null);
    private final List<WeakReference<FallingBlockEntity>> sandEntities = new ArrayList<>();

    public SandstormAttack(final int cooldown, final int windup, final int duration, final float moveDistance,
                           final float damage, final int stun, final float hitboxSize, final float knockback, final float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
        hitSpark = JParticleType.HIT_SPARK_3;
    }

    @Override
    public @NonNull MoveType<SandstormAttack<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    public void tick(final A attacker) {
        if (attacker.hasUser())
            tickSandstorm(attacker);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final A attacker, final LivingEntity user) {
        final Set<LivingEntity> targets = super.perform(attacker, user);
        if (targets.isEmpty()) {
            return targets;
        }

        final LivingEntity superTarget = JUtils.getUserIfStand(targets.stream().findFirst().orElseThrow());
        this.superTarget = new WeakReference<>(superTarget);

        for (int i = 0; i < 8; i++) {
            final FallingBlockEntity sand = FallingBlockEntity.fall(attacker.getEntityWorld(), superTarget.blockPosition(),
                    JBlockRegistry.FOOLISH_SAND_BLOCK.get().defaultBlockState());
            sand.time = -160;
            sand.noPhysics = true;
            sand.dropItem = false;
            sand.setBoundingBox(new AABB(0, 0, 0, 0, 0, 0));
            sand.setNoGravity(true);
            sandEntities.add(new WeakReference<>(sand));
        }

        return targets;
    }

    public void tickSandstorm(final A attacker) {
        LivingEntity superTarget = this.superTarget.get();

        if (superTarget == null) {
            return;
        } else {
            if (sandEntities.isEmpty()) {
                this.superTarget = new WeakReference<>(null);
                return;
            }

            if (!superTarget.isAlive()) {
                this.superTarget = new WeakReference<>(null);
                discardSands(attacker);
                return;
            }
        }

        superTarget.addEffect(
                new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, 0, true, false)
        );
        superTarget.addEffect(
                new MobEffectInstance(MobEffects.BLINDNESS, 40, 0, true, false)
        );

        final int age = attacker.getBaseEntity().tickCount;
        if (age % 20 == 0) {
            // Remove the oldest sand entity and any that may have been removed or killed.
            for (int i = 0; i < sandEntities.size(); i++) {
                WeakReference<FallingBlockEntity> entityRef = sandEntities.get(i);
                FallingBlockEntity entity = entityRef == null ? null : entityRef.get();
                if (entity == null || entity.isRemoved()) {
                    sandEntities.remove(i);
                    i--;
                    continue;
                }

                entity.discard();
                sandEntities.remove(i);
            }
        }

        final RandomSource random = attacker.getBaseEntity().getRandom();
        final Vec3 targetPos = superTarget.position().add(0, superTarget.getBbHeight() / 2, 0);

        int i = 0;
        int j = 1;
        for (WeakReference<FallingBlockEntity> sandRef : sandEntities) {
            FallingBlockEntity sand = sandRef == null ? null : sandRef.get();
            if (sand == null || sand.isRemoved()) {
                continue;
            }

            i++;
            j *= -1;

            final Vec3 newVel = sand.getDeltaMovement().scale(0.25).add( // Suppress current velocity
                    // And add tracking
                    targetPos.subtract(
                            // MathHelper.sin(t) * 2, (isEven ? MathHelper.sin(t) : MathHelper.cos(t)) * 2, MathHelper.cos(t) * 2
                            sand.position().add(
                                    random.nextDouble() - 0.5 + Math.sin(age * i / 10.0 * j),
                                    random.nextDouble() * 2 - 1,
                                    random.nextDouble() - 0.5 + Math.cos(age * i / 10.0 * j))
                    ).normalize().scale(0.5)
            );

            sand.setDeltaMovement(newVel);
            sand.hurtMarked = true;
        }
    }

    public void discardSands(final A attacker) {
        sandEntities.stream()
                .map(WeakReference::get)
                .filter(Objects::nonNull)
                .forEach(Entity::discard);
        sandEntities.clear();
    }

    @Override
    protected @NonNull SandstormAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull SandstormAttack<A> copy() {
        return copyExtras(new SandstormAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(),
                getStun(), getHitboxSize(), getKnockback(), getOffset()));
    }

    public static class Type extends AbstractSimpleAttack.Type<SandstormAttack<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<SandstormAttack<?>>, SandstormAttack<?>> buildCodec(RecordCodecBuilder.Instance<SandstormAttack<?>> instance) {
            return attackDefault(instance, SandstormAttack::new);
        }
    }
}
