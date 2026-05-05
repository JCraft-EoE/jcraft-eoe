package net.arna.jcraft.common.attack.moves.magiciansred;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractBarrageAttack;
import net.arna.jcraft.api.splatter.JSplatterManager;
import net.arna.jcraft.common.entity.stand.MagiciansRedEntity;
import net.arna.jcraft.common.splatter.GasolineSplatter;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public final class FlamethrowerAttack extends AbstractBarrageAttack<FlamethrowerAttack, MagiciansRedEntity> {
    public FlamethrowerAttack(final int cooldown, final int windup, final int duration, final float moveDistance,
                              final float damage, final int stun, final float hitboxSize, final float knockback,
                              final float offset, final int interval) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset, interval);
    }

    @Override
    public @NonNull MoveType<FlamethrowerAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final MagiciansRedEntity attacker, final LivingEntity user) {
        final Set<LivingEntity> targets = super.perform(attacker, user);
        for (LivingEntity target : targets) {
            if (!target.isOnFire()) {
                target.setSecondsOnFire(getInterval());
            }
        }
        return targets;
    }

    @Override
    protected void performHook(final MagiciansRedEntity attacker, final Set<LivingEntity> targets,
                               final Set<AABB> boxes, final DamageSource damageSource,
                               final Vec3 forwardPos, final Vec3 rotationVector) {
        if (attacker.level().isClientSide()) return;

        JSplatterManager splatterManager = JSplatterManager.get(attacker.level());
        for (AABB box : boxes) {
            splatterManager.getIntersections(box, s -> s instanceof GasolineSplatter)
                    .forEach(s -> ((GasolineSplatter) s).lightOnFire());
        }
    }

    @Override
    protected @NonNull FlamethrowerAttack getThis() {
        return this;
    }

    @Override
    public @NonNull FlamethrowerAttack copy() {
        return copyExtras(new FlamethrowerAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(),
                getStun(), getHitboxSize(), getKnockback(), getOffset(), getInterval()));
    }

    public static class Type extends AbstractBarrageAttack.Type<FlamethrowerAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<FlamethrowerAttack>, FlamethrowerAttack> buildCodec(RecordCodecBuilder.Instance<FlamethrowerAttack> instance) {
            return barrageDefault(instance, FlamethrowerAttack::new);
        }
    }
}