package net.arna.jcraft.common.attack.moves.magiciansred;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.entity.projectile.AnkhProjectile;
import net.arna.jcraft.common.entity.stand.MagiciansRedEntity;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.common.util.MobilityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.List;
import java.util.Set;

public class RedirectAttack extends AbstractMove<RedirectAttack, MagiciansRedEntity> {
    public RedirectAttack(int cooldown, int windup, int duration, float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
        mobilityType = MobilityType.TELEPORT; // this is a LIE, it just tells the AI to use it at a range of >3m
    }

    @Override
    public @NonNull Set<LivingEntity> perform(MagiciansRedEntity attacker, LivingEntity user, MoveContext ctx) {
        List<AnkhProjectile> ankhs = attacker.getWorld().getEntitiesByClass(AnkhProjectile.class,
                attacker.getBoundingBox().expand(32), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR);

        Vec3d eyePos = getOffsetHeightPos(attacker);
        if (!ankhs.isEmpty()) {
            Vec3d pos = JUtils.raycastAll(user, eyePos, eyePos.add(user.getRotationVector().multiply(24)), RaycastContext.FluidHandling.NONE);

            for (AnkhProjectile ankh : ankhs) {
                if (ankh.getOwner() != user) continue;
                ankh.setVariation(false);
                ankh.setVelocity(pos.subtract(ankh.getPos()).normalize().multiply(0.6));
            }
        }

        return Set.of();
    }

    @Override
    protected @NonNull RedirectAttack getThis() {
        return this;
    }

    @Override
    public @NonNull RedirectAttack copy() {
        return copyExtras(new RedirectAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }
}
