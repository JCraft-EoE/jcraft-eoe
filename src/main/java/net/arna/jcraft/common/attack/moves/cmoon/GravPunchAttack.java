package net.arna.jcraft.common.attack.moves.cmoon;

import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractSimpleAttack;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.entity.stand.CMoonEntity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.Gravity;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.Set;

public class GravPunchAttack extends AbstractSimpleAttack<GravPunchAttack, CMoonEntity> {
    public static final String GRAVITY_SOURCE = JCraft.MOD_ID + "$" + GravPunchAttack.class.getSimpleName();

    public GravPunchAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun,
                           float hitboxSize, float knockback, float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
        hitSpark = JParticleType.HIT_SPARK_2; //todo: gravpunch inversion hitspark
    }

    @Override
    protected void processTarget(CMoonEntity attacker, LivingEntity target, Vec3d kbVec, DamageSource damageSource) {
        super.processTarget(attacker, target, kbVec, damageSource);

        Direction oppositeGravity = GravityChangerAPI.getGravityDirection(target).getOpposite();
        GravityChangerAPI.addGravity(target, new Gravity(oppositeGravity, 2, 60, GRAVITY_SOURCE));
        target.addStatusEffect(new StatusEffectInstance(JStatusRegistry.WEIGHTLESS, 60, 0, true, false));
        // Launches them up relative to their original gravity, to prevent ground clipping
        JUtils.setVelocity(target, oppositeGravity.getOffsetX() * 0.2, oppositeGravity.getOffsetY() * 0.2, oppositeGravity.getOffsetZ() * 0.2);
    }

    @Override
    public void performHook(CMoonEntity attacker, Set<LivingEntity> targets, Set<Box> boxes, DamageSource damageSource, Vec3d forwardPos, Vec3d rotationVector, MoveContext ctx) {
        if (targets.isEmpty()) return;
        JComponents.getShockwaveHandler(attacker.getWorld()).addShockwave(forwardPos, new Vec3d(GravityChangerAPI.getGravityDirection(attacker).getUnitVector()), 3.0f);
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
