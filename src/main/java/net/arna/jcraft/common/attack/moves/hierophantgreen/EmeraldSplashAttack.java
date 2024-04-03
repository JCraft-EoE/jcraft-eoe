package net.arna.jcraft.common.attack.moves.hierophantgreen;

import it.unimi.dsi.fastutil.ints.IntSet;
import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.IntMoveVariable;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractMultiHitAttack;
import net.arna.jcraft.common.entity.projectile.EmeraldProjectile;
import net.arna.jcraft.common.entity.stand.HGEntity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

import java.util.Set;

public class EmeraldSplashAttack extends AbstractMultiHitAttack<EmeraldSplashAttack, HGEntity> {
    public static final IntMoveVariable CHARGE_TIME = new IntMoveVariable();
    private final float speed;
    private boolean reflect = false;

    public EmeraldSplashAttack(int cooldown, int duration, float moveDistance, float damage, int stun, float knockback, float offset, IntSet hitMoments, float speed) {
        super(cooldown, duration, moveDistance, damage, stun, 0, knockback, offset, hitMoments);
        this.speed = speed;
        ranged = true;
    }

    public EmeraldSplashAttack withReflect() {
        this.reflect = true;
        return this;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(HGEntity attacker, LivingEntity user, MoveContext ctx) {
        int emeraldCount = 3 + ctx.getInt(CHARGE_TIME) / 10;

        for (int i = 0; i < emeraldCount; i++) {
            EmeraldProjectile emerald = new EmeraldProjectile(attacker.getWorld(), user);
            emerald.setVelocity(user, user.getPitch(), user.getYaw(), 0.0F, speed, 5F);

            Vec3d upVec = GravityChangerAPI.getEyeOffset(attacker.getUserOrThrow());
            Vec3d heightOffset = upVec.multiply(0.75);
            emerald.setPosition(attacker.getBaseEntity().getPos().add(heightOffset));

            if (reflect)
                emerald.withReflect();

            attacker.getWorld().spawnEntity(emerald);
        }

        return Set.of();
    }

    @Override
    public void registerContextEntries(MoveContext ctx) {
        ctx.register(CHARGE_TIME);
    }

    @Override
    protected @NonNull EmeraldSplashAttack getThis() {
        return this;
    }

    @Override
    public @NonNull EmeraldSplashAttack copy() {
        EmeraldSplashAttack emeraldSplashAttack = copyExtras(new EmeraldSplashAttack(getCooldown(), getDuration(), getMoveDistance(), getDamage(), getStun(), getKnockback(), getOffset(), getHitMoments(), speed));
        if (reflect)
            emeraldSplashAttack.withReflect();
        return emeraldSplashAttack;
    }
}
