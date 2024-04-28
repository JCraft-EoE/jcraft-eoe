package net.arna.jcraft.common.attack.moves.theworld.overheaven;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.projectile.KnifeProjectile;
import net.arna.jcraft.common.entity.stand.TheWorldOverHeavenEntity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

import java.util.Set;

public class AerialDivineFinisherAttack extends AbstractSimpleAttack<AerialDivineFinisherAttack, TheWorldOverHeavenEntity> {
    public AerialDivineFinisherAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun,
                                      float hitboxSize, float knockback, float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
        ranged = true;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(TheWorldOverHeavenEntity attacker, LivingEntity user, MoveContext ctx) {
        Set<LivingEntity> targets = super.perform(attacker, user, ctx);

        Vec3d heightOffset = RotationUtil.vecPlayerToWorld(new Vec3d(0, user.getHeight() / 2.0, 0), GravityChangerAPI.getGravityDirection(user));

        Random random = attacker.getRandom();
        for (int i = 0; i < 8; i++) {
            KnifeProjectile knife = new KnifeProjectile(attacker.getWorld(), user);
            knife.setLightning(true);
            knife.pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY;
            knife.setVelocity(user, user.getPitch(), user.getYaw(), 0.0F, 2F, 1F);
            knife.setPosition(user.getPos().add(heightOffset).add(
                    random.nextTriangular(0, 0.5),
                    random.nextTriangular(0, 0.5),
                    random.nextTriangular(0, 0.5)));
            attacker.getWorld().spawnEntity(knife);
        }

        return targets;
    }

    @Override
    protected @NonNull AerialDivineFinisherAttack getThis() {
        return this;
    }

    @Override
    public @NonNull AerialDivineFinisherAttack copy() {
        return copyExtras(new AerialDivineFinisherAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(),
                getDamage(), getStun(), getHitboxSize(), getKnockback(), getOffset()));
    }
}
