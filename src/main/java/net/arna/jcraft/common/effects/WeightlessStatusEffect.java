package net.arna.jcraft.common.effects;

import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.component.living.MiscComponent;
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
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.Random;

import static net.arna.jcraft.common.gravity.api.GravityChangerAPI.getGravityDirection;

public class WeightlessStatusEffect extends StatusEffect {
    private static final Random random = new Random();

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
            MiscComponent misc = JComponents.getMiscData(entity);

            if (!entity.isAlive()) {
                entity.removeStatusEffect(this);
                return;
            }

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
                misc.setHoverTime(0);
            } else {
                int newHoverTime = misc.getHoverTime() + 1;
                misc.setHoverTime(newHoverTime);
                if (newHoverTime > 10) // If not near ground for half a second
                    entity.removeStatusEffect(this);
            }
        }
    }

    @Override
    public void onApplied(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        super.onApplied(entity, attributes, amplifier);

        if (entity.getWorld().isClient) return;

        MiscComponent misc = JComponents.getMiscData(entity);
        misc.setPrevNoGrav(entity.hasNoGravity());
        misc.setHoverTime(0);

        if (entity.isDead()) return; // Don't screw with the gravity of dead entities, it will persist on players

        Direction lookDir = JUtils.getLookDirection(entity);
        if (amplifier == 1) {
            if (lookDir != GravityChangerAPI.getGravityDirection(entity))
                GravityChangerAPI.addGravity(entity, new Gravity(lookDir, 1, 200, "effect"));
        } else entity.setNoGravity(true);
    }

    @Override
    public void onRemoved(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        super.onRemoved(entity, attributes, amplifier);

        if (entity.getWorld().isClient) return;
        GravityChangerAPI.clearGravity(entity);
        if (!JComponents.getMiscData(entity).getPrevNoGrav())
            entity.setNoGravity(false);
    }
}
