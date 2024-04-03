package net.arna.jcraft.common.attack.moves.madeinheaven;

import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.component.living.HitPropertyComponent;
import net.arna.jcraft.common.entity.stand.MadeInHeavenEntity;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.common.util.MobilityType;
import net.arna.jcraft.registry.JSoundRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
public class SpeedSliceAttack extends AbstractMove<SpeedSliceAttack, MadeInHeavenEntity> {
    private final float damage, hitboxSize, knockback;

    public SpeedSliceAttack(int cooldown, int windup, int duration, float moveDistance, float damage, float hitboxSize,
                            float knockback) {
        super(cooldown, windup, duration, moveDistance);
        this.damage = damage;
        this.hitboxSize = hitboxSize;
        this.knockback = knockback;

        ranged = true;
        mobilityType = MobilityType.TELEPORT;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(MadeInHeavenEntity attacker, LivingEntity user, MoveContext ctx) {
        return doSpeedSlice(attacker, user.getEyePos(), user.getEyePos().add(user.getRotationVector().multiply(8)),
                getDamage(), getKnockback(),getHitboxSize(), 20, 1);
    }

    public static Set<LivingEntity> doSpeedSlice(MadeInHeavenEntity attacker, Vec3d start, Vec3d end, float damage, float knockback, float size, int stunTicks, int stunType) {
        World world = attacker.getWorld();
        LivingEntity user = attacker.getUserOrThrow();
        HitResult hitResult = world.raycast(new RaycastContext(start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, user));
        Vec3d pos1 = user.getPos();
        Vec3d pos2 = hitResult.getPos();
        Vec3d towardsVec = pos2.subtract(pos1);

        Vec3d kbVec = towardsVec.normalize();

        DamageSource playerSource = world.getDamageSources().mobAttack(user);

        user.teleport(pos2.x, pos2.y, pos2.z);

        Set<LivingEntity> targets = new HashSet<>();
        double count = Math.round(pos1.distanceTo(pos2));

        for (int i = 0; i < count; i++) {
            Vec3d curPos = pos1.add(towardsVec.multiply(i / count));

            Vec3d vec1 = curPos.add(-size, -size, -size);
            Vec3d vec2 = curPos.add(size, size, size);

            JUtils.displayHitbox(world, vec1, vec2);

            List<LivingEntity> hurt = world.getEntitiesByClass(LivingEntity.class, new Box(vec1, vec2),
                    EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.and(e -> e != attacker && e != user));
            hurt.removeIf(targets::contains);
            targets.addAll(hurt);
        }

        for (LivingEntity ent : targets) {
            LivingEntity target = JUtils.getUserIfStand(ent);
            StandEntity.damageLogic(world, target, kbVec.multiply(knockback).add(0, knockback / 4, 0),
                    stunTicks, stunType, false, damage, true, (int) (4 + damage), playerSource, user, HitPropertyComponent.HitAnimation.MID);
        }

        if (attacker.getAccelTime() > 0 && !targets.isEmpty()) attacker.incrementSpeedometer();

        attacker.playSound(JSoundRegistry.MIH_ZOOM, 1f, 1f);

        return targets;
    }

    @Override
    protected @NonNull SpeedSliceAttack getThis() {
        return this;
    }

    @Override
    public @NonNull SpeedSliceAttack copy() {
        return copyExtras(new SpeedSliceAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(),
                getDamage(), getHitboxSize(), getKnockback()));
    }
}
