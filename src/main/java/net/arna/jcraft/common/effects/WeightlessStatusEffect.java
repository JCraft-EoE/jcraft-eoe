package net.arna.jcraft.common.effects;

import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.Gravity;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.Random;

import static net.arna.jcraft.common.gravity.api.GravityChangerAPI.getGravityDirection;

public class WeightlessStatusEffect extends StatusEffect {
    private boolean previouslyNoGravved = false;
    private final Random random = new Random();
    private int hoverTime = 0;

    public WeightlessStatusEffect() {
        super(StatusEffectCategory.NEUTRAL, 0x000011);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return amplifier == 1;
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        World world = entity.getWorld();

        Vec3d pos = entity.getPos();
        Vec3d downPos = pos.add(RotationUtil.vecPlayerToWorld(0.0, -5.0, 0.0, getGravityDirection(entity)));
        if (entity.getWorld().isClient) {
            world.addParticle(
                    ParticleTypes.REVERSE_PORTAL,
                    pos.x + random.nextDouble() - 0.5,
                    pos.y + random.nextDouble() - 0.5,
                    pos.z + random.nextDouble() - 0.5,
                    0, 0, 0
            );
        } else {
            HitResult hitResult = world.raycast(
                    new RaycastContext(
                            pos,
                            downPos,
                            RaycastContext.ShapeType.COLLIDER,
                            RaycastContext.FluidHandling.NONE,
                            entity
                    )
            );

            if (hitResult.getType() == HitResult.Type.BLOCK) {
                hoverTime = 0;
            } else if (++hoverTime > 10) // If not near ground for a second
                entity.removeStatusEffect(this);
        }
    }

    @Override
    public void onApplied(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        super.onApplied(entity, attributes, amplifier);
        if (entity.getWorld().isClient) return;
        this.previouslyNoGravved = entity.hasNoGravity();
        if (amplifier == 1)
            GravityChangerAPI.addGravity(entity, new Gravity(JUtils.getLookDirection(entity), 1, 200, "effect") );
        else entity.setNoGravity(true);
    }

    @Override
    public void onRemoved(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        super.onRemoved(entity, attributes, amplifier);
        if (entity.getWorld().isClient) return;
        GravityChangerAPI.clearGravity(entity);
        if (!previouslyNoGravved)
            entity.setNoGravity(false);
    }
}
