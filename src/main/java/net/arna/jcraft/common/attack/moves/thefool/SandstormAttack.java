package net.arna.jcraft.common.attack.moves.thefool;

import com.google.common.reflect.TypeToken;
import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.core.ctx.MoveVariable;
import net.arna.jcraft.common.attack.moves.base.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.stand.TheFoolEntity;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.registry.JObjectRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SandstormAttack extends AbstractSimpleAttack<SandstormAttack, TheFoolEntity> {
    public static final MoveVariable<LivingEntity> SUPER_TARGET = new MoveVariable<>(LivingEntity.class);
    public static final MoveVariable<List<FallingBlockEntity>> SANDS = new MoveVariable<>(new TypeToken<>() {});

    public SandstormAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun, float hitboxSize, float knockback, float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
        hitSpark = JParticleType.HIT_SPARK_3;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(TheFoolEntity attacker, LivingEntity user, MoveContext ctx) {
        Set<LivingEntity> targets = super.perform(attacker, user, ctx);
        if (targets.isEmpty()) return targets;

        LivingEntity superTarget = JUtils.getUserIfStand(targets.stream().findFirst().orElseThrow());
        ctx.set(SUPER_TARGET, superTarget);

        List<FallingBlockEntity> sands = ctx.get(SANDS);

        for (int i = 0; i < 8; i++) {
            FallingBlockEntity sand = FallingBlockEntity.spawnFromBlock(attacker.getWorld(), superTarget.getBlockPos(),
                    JObjectRegistry.FOOLISH_SAND_BLOCK.getDefaultState());
            sand.timeFalling = -160;
            sand.noClip = true;
            sand.dropItem = false;
            sand.setBoundingBox(new Box(0, 0, 0, 0, 0, 0));
            sand.setNoGravity(true);
            sands.add(sand);
        }

        return targets;
    }

    public void tickSandstorm(TheFoolEntity attacker) {
        MoveContext ctx = attacker.getMoveContext();

        List<FallingBlockEntity> sands = ctx.get(SANDS);
        LivingEntity superTarget = ctx.get(SUPER_TARGET);

        if (superTarget == null) {
            return;
        } else {
            if (sands.isEmpty()) {
                ctx.set(SUPER_TARGET, null);
                return;
            }
            if (!superTarget.isAlive()) {
                ctx.set(SUPER_TARGET, null);
                discardSands(attacker);
                return;
            }
        }

        superTarget.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 10, 0, true, false));
        superTarget.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 40, 0, true, false));

        int age = attacker.age;
        if (age % 20 == 0) {
            sands.get(0).discard();
            sands.remove(0);
        }

        Random random = attacker.getRandom();
        Vec3d targetPos = superTarget.getPos().add(0, superTarget.getHeight() / 2, 0);

        int i = 0;
        int j = 1;
        for (FallingBlockEntity sand : sands) {
            if (sand == null || sand.isRemoved())
                continue;
            i++;
            j *= -1;

            Vec3d newVel = sand.getVelocity().multiply(0.25).add( // Suppress current velocity
                    // And add tracking
                    targetPos.subtract(
                            // MathHelper.sin(t) * 2, (isEven ? MathHelper.sin(t) : MathHelper.cos(t)) * 2, MathHelper.cos(t) * 2
                            sand.getPos().add(
                                    random.nextDouble() - 0.5 + Math.sin(age * i / 10.0 * j),
                                    random.nextDouble() * 2 - 1,
                                    random.nextDouble() - 0.5 + Math.cos(age * i / 10.0 * j))
                    ).normalize().multiply(0.5)
            );

            sand.setVelocity(newVel);
            sand.velocityModified = true;
        }
    }

    public void discardSands(TheFoolEntity attacker) {
        List<FallingBlockEntity> sands = attacker.getMoveContext().get(SANDS);
        sands.forEach(Entity::discard);
        sands.clear();
    }

    @Override
    public void registerContextEntries(MoveContext ctx) {
        ctx.register(SUPER_TARGET);
        ctx.register(SANDS, new ArrayList<>());
    }

    @Override
    protected @NonNull SandstormAttack getThis() {
        return this;
    }

    @Override
    public @NonNull SandstormAttack copy() {
        return copyExtras(new SandstormAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(),
                getStun(), getHitboxSize(), getKnockback(), getOffset()));
    }
}
