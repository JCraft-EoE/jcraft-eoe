package net.arna.jcraft.common.attack.moves.cmoon;

import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.moves.base.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.stand.CMoonEntity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.Gravity;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class GravPunchAttack extends AbstractSimpleAttack<GravPunchAttack, CMoonEntity> {
    public static final String GRAVITY_SOURCE = JCraft.MOD_ID + "$" + GravPunchAttack.class.getSimpleName();

    public GravPunchAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun,
                           float hitboxSize, float knockback, float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
        hitSpark = JParticleType.HIT_SPARK_2;
    }

    @Override
    protected void processTarget(CMoonEntity attacker, LivingEntity target, Vec3d kbVec, DamageSource damageSource) {
        super.processTarget(attacker, target, kbVec, damageSource);

        GravityChangerAPI.addGravity(target, new Gravity(Direction.UP, 2, 60, GRAVITY_SOURCE));
        target.addStatusEffect(new StatusEffectInstance(JStatusRegistry.WEIGHTLESS, 60, 0, true, false));
        target.velocityModified = true;
    }

    @Override
    protected @NonNull GravPunchAttack getThis() {
        return this;
    }

    @Override
    public @NonNull GravPunchAttack copy() {
        return copyExtras(new GravPunchAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(),
                getStun(), getHitboxSize(), getKnockback(), getOffset()));
    }
}
