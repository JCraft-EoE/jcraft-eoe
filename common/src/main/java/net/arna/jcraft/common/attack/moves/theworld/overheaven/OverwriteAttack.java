package net.arna.jcraft.common.attack.moves.theworld.overheaven;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.enums.MoveClass;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.stand.TheWorldOverHeavenEntity;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public final class OverwriteAttack extends AbstractSimpleAttack<OverwriteAttack, TheWorldOverHeavenEntity> {
    public static final double NO_LOOK_RANGE = 512.0;
    private final List<Overwrite> overwrites = new ArrayList<>();

    public OverwriteAttack(final int cooldown, final int windup, final int duration, final float moveDistance, final float damage, final int stun,
                           final float hitboxSize, final float knockback, final float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
    }

    @Override
    public void tick(final TheWorldOverHeavenEntity attacker) {
        if (!attacker.hasUser()) {
            return;
        }

        final LivingEntity user = attacker.getUserOrThrow();

        // Mob TW:OH users normally don't swing after charging overwrite. This fixes that.
        RandomSource random = attacker.getRandom();
        if (user instanceof Mob && attacker.getState() == TheWorldOverHeavenEntity.State.CHARGE_OVERWRITE && random.nextBoolean()) {
            attacker.initMove(random.nextBoolean() ? MoveClass.SPECIAL1 : MoveClass.SPECIAL2);
        }

        final int moveStun = attacker.getMoveStun();
        if (moveStun <= 0 && attacker.getOverwriteType() != 0) {
            attacker.setOverwriteType(0);
        }

        overwrites.removeIf(Overwrite::isInvalid);

        for (final Overwrite overwrite : overwrites) {
            overwrite.tick();

            // Make strong reference to the entity, so it doesn't suddenly disappear.
            LivingEntity entity = overwrite.getEntity();
            if (JUtils.isBlocking(entity)) return;

            // Inability to look at master
            final AABB box = entity
                    .getBoundingBox()
                    .expandTowards(entity.getViewVector(1.0F).scale(NO_LOOK_RANGE))
                    .inflate(1.0D);
            final EntityHitResult hitResult = ProjectileUtil.getEntityHitResult(
                    entity, entity.getEyePosition(),
                    entity.getEyePosition().add(entity.getLookAngle().scale(NO_LOOK_RANGE)),
                    box, EntitySelector.NO_CREATIVE_OR_SPECTATOR, NO_LOOK_RANGE);

            if (hitResult == null) {
                continue;
            }
            final Entity lookEntity = hitResult.getEntity();

            if (lookEntity != user && lookEntity != attacker) {
                continue;
            }
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, attacker.getEyePosition().add(
                    random.nextInt() * 10,
                    random.nextInt() * 10,
                    random.nextInt() * 10));
        }
    }

    @Override
    protected void processTarget(final TheWorldOverHeavenEntity attacker, final LivingEntity target, final Vec3 kbVec, final DamageSource damageSource) {
        super.processTarget(attacker, target, kbVec, damageSource);

        switch (attacker.getOverwriteType()) {
            case 1 -> {
                overwrites.add(new Overwrite(target, 200));
            }
            case 2 -> {
                target.setSecondsOnFire(5);
                target.addEffect(new MobEffectInstance(MobEffects.HUNGER, 100, 0, false, true));
                target.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0, false, true));
                target.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 0, false, true));
            }
            case 3 -> {
                target.heal(4f);

                if (!(target instanceof Mob)) {
                    return;
                }
                JComponentPlatformUtils.getMiscData(target).setSlavedTo(attacker.getUserOrThrow().getUUID());
                overwrites.add(new Overwrite(target, 1048576));
            }
        }
    }

    @Override
    public @NonNull MoveType<OverwriteAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    protected @NonNull OverwriteAttack getThis() {
        return this;
    }

    @Override
    public @NonNull OverwriteAttack copy() {
        return copyExtras(new OverwriteAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(),
                getDamage(), getStun(), getHitboxSize(), getKnockback(), getOffset()));
    }

    public static class Type extends AbstractSimpleAttack.Type<OverwriteAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<OverwriteAttack>, OverwriteAttack> buildCodec(RecordCodecBuilder.Instance<OverwriteAttack> instance) {
            return attackDefault(instance, OverwriteAttack::new);
        }
    }

    @Getter
    private static class  Overwrite {
        private final WeakReference<LivingEntity> entity;
        private int time;

        public Overwrite(final LivingEntity entity, final int time) {
            this.entity = new WeakReference<>(entity);
            this.time = time;
        }

        public void tick() {
            if (time > 0) {
                time--;
            }
        }

        public LivingEntity getEntity() {
            return entity.get();
        }

        public boolean isInvalid() {
            LivingEntity entity = getEntity();
            return entity == null || !entity.isAlive() || time <= 0;
        }
    }
}
