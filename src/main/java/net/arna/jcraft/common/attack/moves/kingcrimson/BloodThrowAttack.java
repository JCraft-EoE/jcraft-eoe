package net.arna.jcraft.common.attack.moves.kingcrimson;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.entity.projectile.BloodProjectile;
import net.arna.jcraft.common.entity.stand.KingCrimsonEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.PersistentProjectileEntity;

import java.util.Set;

public class BloodThrowAttack extends AbstractMove<BloodThrowAttack, KingCrimsonEntity> {
    public BloodThrowAttack(int cooldown, int windup, int duration, float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
        ranged = true;
    }

    @Override
    public void onInitiate(KingCrimsonEntity attacker) {
        super.onInitiate(attacker);

        attacker.getUserOrThrow().damage(attacker.getWorld().getDamageSources().magic(), 0.1f); // User throws their blood, dealing a bit of damage.
    }

    @Override
    public @NonNull Set<LivingEntity> perform(KingCrimsonEntity attacker, LivingEntity user, MoveContext ctx) {
        BloodProjectile bloodProjectile = new BloodProjectile(attacker.getWorld(), user);
        bloodProjectile.pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY;
        bloodProjectile.setVelocity(user, user.getPitch(), user.getYaw(), 0, user.isSneaking() ? 1.33F : 0.66F, 0);
        bloodProjectile.setPosition(attacker.getEyePos());
        attacker.getWorld().spawnEntity(bloodProjectile);

        return Set.of();
    }

    @Override
    protected @NonNull BloodThrowAttack getThis() {
        return this;
    }

    @Override
    public @NonNull BloodThrowAttack copy() {
        return copyExtras(new BloodThrowAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }
}
