package net.arna.jcraft.common.attack.moves.magiciansred;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.projectile.RedBindEntity;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.minecraft.world.entity.LivingEntity;

import java.util.Set;

public final class RedBindAttack<A extends IAttacker<? extends A, ?>> extends AbstractSimpleAttack<RedBindAttack<A>, A> {
    public RedBindAttack(final int cooldown, final int windup, final int duration, final float moveDistance, final float damage, final int stun,
                         final float hitboxSize, final float knockback, final float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
    }

    @Override
    public @NonNull MoveType<RedBindAttack<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final A attacker, final LivingEntity user) {
        final Set<LivingEntity> targets = super.perform(attacker, user);
        if (targets.isEmpty()) {
            return targets;
        }

        final LivingEntity boundEntity = JUtils.getUserIfStand(targets.stream().findFirst().orElseThrow());

        if (JUtils.isBlocking(boundEntity)) {
            return Set.of();
        }

        // Remove Stand
        final StandEntity<?, ?> boundStand = JUtils.getStand(boundEntity);
        if (boundStand != null) {
            boundStand.setCurrentMove(null);
            boundStand.setMoveStun(0);
            boundStand.desummon();
        }

        // Stun
        boundEntity.removeEffect(JStatusRegistry.DAZED.get());
        JCraft.stun(boundEntity, RedBindEntity.LIFE_TIME, 0, user);

        // Create and bind
        final RedBindEntity redBind = new RedBindEntity(attacker.getEntityWorld());
        redBind.setPos(boundEntity.position());
        redBind.setMaster(user);
        redBind.setBoundEntity(boundEntity);
        attacker.getEntityWorld().addFreshEntity(redBind);

        return targets;
    }

    @Override
    protected @NonNull RedBindAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull RedBindAttack<A> copy() {
        return copyExtras(new RedBindAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(), getStun(),
                getHitboxSize(), getKnockback(), getOffset()));
    }

    public static class Type extends AbstractSimpleAttack.Type<RedBindAttack<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<RedBindAttack<?>>, RedBindAttack<?>> buildCodec(RecordCodecBuilder.Instance<RedBindAttack<?>> instance) {
            return attackDefault(instance, RedBindAttack::new);
        }
    }
}
