package net.arna.jcraft.common.attack.moves.theworld.overheaven;

import lombok.NonNull;
import net.arna.jcraft.common.attack.moves.base.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.damage.JDamageSources;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.entity.stand.TheWorldOverHeavenEntity;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.math.Vec3d;

public class SingularityAttack extends AbstractSimpleAttack<SingularityAttack, TheWorldOverHeavenEntity> {
    private final boolean blockBypass;

    public SingularityAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun,
                             float hitboxSize, float knockback, float offset, boolean blockBypass) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
        this.blockBypass = blockBypass;
    }

    @Override
    protected void processTarget(TheWorldOverHeavenEntity attacker, LivingEntity target, Vec3d kbVec, DamageSource damageSource) {
        super.processTarget(attacker, target, kbVec, damageSource);

        if (blockBypass) {
            target.removeStatusEffect(JStatusRegistry.DAZED);
            StandEntity.stun(target, getStun(), 0);
        }

        StandEntity.trueDamage(6, JDamageSources.stand(attacker), target);
    }

    @Override
    protected @NonNull SingularityAttack getThis() {
        return this;
    }

    @Override
    public @NonNull SingularityAttack copy() {
        return copyExtras(new SingularityAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(),
                getDamage(), getStun(), getHitboxSize(), getKnockback(), getOffset(), blockBypass));
    }
}
