package net.arna.jcraft.common.attack.moves.thehand;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractUppercutAttack;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public class Stomp2Attack<A extends IAttacker<? extends A, ?>> extends AbstractUppercutAttack<Stomp2Attack<A>, A> {
    public Stomp2Attack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun,
                        float hitboxSize, float knockback, float offset, float strength) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset, strength);
    }

    @Override
    public @NonNull MoveType<Stomp2Attack<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    public @NonNull Set<LivingEntity> perform(A attacker, LivingEntity user) {
        Set<LivingEntity> targets = super.perform(attacker, user);

        final LivingEntity baseEntity = attacker.getBaseEntity();
        JComponentPlatformUtils.getShockwaveHandler(baseEntity.level())
                .addShockwave(baseEntity.position().add(user.getLookAngle()), new Vec3(GravityChangerAPI.getGravityDirection(baseEntity).step()), 2.5f);

        targets.forEach(livingEntity -> livingEntity.removeEffect(JStatusRegistry.KNOCKDOWN.get()));

        return targets;
    }

    @Override
    protected @NonNull Stomp2Attack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull Stomp2Attack<A> copy() {
        return copyExtras(new Stomp2Attack<A>(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(),
                getStun(), getHitboxSize(), getKnockback(), getOffset(), getStrength()));
    }

    public static class Type extends AbstractUppercutAttack.Type<Stomp2Attack<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<Stomp2Attack<?>>, Stomp2Attack<?>> buildCodec(RecordCodecBuilder.Instance<Stomp2Attack<?>> instance) {
            return uppercutDefault(instance, Stomp2Attack::new);
        }
    }
}
