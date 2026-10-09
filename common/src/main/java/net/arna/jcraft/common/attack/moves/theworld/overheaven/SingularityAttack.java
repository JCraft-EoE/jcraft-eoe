package net.arna.jcraft.common.attack.moves.theworld.overheaven;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.AttackData;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import static net.arna.jcraft.api.Attacks.damageLogic;

@Getter
public final class SingularityAttack<A extends IAttacker<? extends A, ?>> extends AbstractSimpleAttack<SingularityAttack<A>, A> {
    private final boolean blockBypass;

    public SingularityAttack(final int cooldown, final int windup, final int duration, final float moveDistance, final float damage, final int stun,
                             final float hitboxSize, final float knockback, final float offset, final boolean blockBypass) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
        this.blockBypass = blockBypass;
    }

    @Override
    protected void processTarget(final A attacker, final LivingEntity target, final Vec3 kbVec, final DamageSource damageSource) {
        damageLogic(attacker.getEntityWorld(), target, new AttackData(
                kbVec, getStun(), getStunType().ordinal(), true,
                getDamage(), isLift(), getBlockStun(), damageSource, attacker.getUserOrThrow(),
                getHitAnimation(), attacker.getMoveUsage(), true, false)
        );

        if (blockBypass) {
            target.removeEffect(JStatusRegistry.DAZED.get());
            JCraft.stun(target, getStun(), 0, attacker.getBaseEntity());
        }
    }

    @Override
    public @NonNull MoveType<SingularityAttack<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    protected @NonNull SingularityAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull SingularityAttack<A> copy() {
        return copyExtras(new SingularityAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(),
                getDamage(), getStun(), getHitboxSize(), getKnockback(), getOffset(), blockBypass));
    }

    public static class Type extends AbstractSimpleAttack.Type<SingularityAttack<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<SingularityAttack<?>>, SingularityAttack<?>> buildCodec(RecordCodecBuilder.Instance<SingularityAttack<?>> instance) {
            return instance.group(extras(), attackExtras(), cooldown(), windup(), duration(), moveDistance(), damage(),
                    stun(), hitboxSize(), knockback(), offset(),
                    Codec.BOOL.fieldOf("block_bypass").forGetter(SingularityAttack::isBlockBypass))
                    .apply(instance, applyAttackExtras(SingularityAttack::new));
        }
    }
}
