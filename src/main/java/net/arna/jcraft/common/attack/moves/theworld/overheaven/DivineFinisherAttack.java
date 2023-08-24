package net.arna.jcraft.common.attack.moves.theworld.overheaven;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.projectile.KnifeProjectile;
import net.arna.jcraft.common.entity.stand.TheWorldOverHeavenEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.util.math.Vec3d;

import java.util.Set;

public class DivineFinisherAttack extends AbstractSimpleAttack<DivineFinisherAttack, TheWorldOverHeavenEntity> {
    public DivineFinisherAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun,
                                float hitboxSize, float knockback, float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
        ranged = true;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(TheWorldOverHeavenEntity attacker, LivingEntity user, MoveContext ctx) {
        Set<LivingEntity> targets = super.perform(attacker, user, ctx);

        Vec3d rotVec = user.getRotationVector();

        for (int i = 0; i < 4; i++) {
            KnifeProjectile knife = new KnifeProjectile(attacker.getWorld(), user);
            knife.setDelayedLightning(10 + i * 5);
            knife.pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY;
            knife.setNoGravity(true);
            knife.setVelocity(new Vec3d(rotVec.x * 0.7, 0, rotVec.z * 0.7).rotateY(1.5708f * i));
            knife.setPosition(attacker.getEyePos());
            attacker.getWorld().spawnEntity(knife);
        }

        return targets;
    }

    @Override
    protected @NonNull DivineFinisherAttack getThis() {
        return this;
    }

    @Override
    public @NonNull DivineFinisherAttack copy() {
        return copyExtras(new DivineFinisherAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(),
                getDamage(), getStun(), getHitboxSize(), getKnockback(), getOffset()));
    }
}
