package net.arna.jcraft.common.effects;

import net.arna.jcraft.api.component.living.CommonMiscComponent;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import java.util.Random;

import static net.arna.jcraft.common.gravity.api.GravityChangerAPI.getGravityDirection;

public class WeightlessStatusEffect extends MobEffect {
    private static final Random random = new Random();

    public WeightlessStatusEffect() {
        super(MobEffectCategory.NEUTRAL, 0x000011);
    }

    @Override
    public boolean isDurationEffectTick(final int duration, final int amplifier) {
        return amplifier == 1;
    }

    @Override
    public void applyEffectTick(final LivingEntity entity, final int amplifier) {
        Level world = entity.level();

        Vec3 pos = entity.position();
        Vec3 downPos = pos.add(RotationUtil.vecPlayerToWorld(0.0, -5.0, 0.0, getGravityDirection(entity)));
        if (entity.level().isClientSide) {
            world.addParticle(
                    ParticleTypes.REVERSE_PORTAL,
                    pos.x + random.nextDouble() - 0.5,
                    pos.y + random.nextDouble() - 0.5,
                    pos.z + random.nextDouble() - 0.5,
                    0, 0, 0
            );
        } else {
            CommonMiscComponent misc = JComponentPlatformUtils.getMiscData(entity);

            if (!entity.isAlive()) {
                entity.removeEffect(this);
                return;
            }

            HitResult hitResult = world.clip(
                    new ClipContext(
                            pos,
                            downPos,
                            ClipContext.Block.COLLIDER,
                            ClipContext.Fluid.NONE,
                            entity
                    )
            );

            if (hitResult.getType() == HitResult.Type.BLOCK) {
                misc.setHoverTime(0);
            } else {
                int newHoverTime = misc.getHoverTime() + 1;
                misc.setHoverTime(newHoverTime);
                if (newHoverTime > 10) // If not near ground for half a second
                {
                    entity.removeEffect(this);
                }
            }
        }
    }

    @Override
    public void addAttributeModifiers(final LivingEntity entity, final AttributeMap attributes, final int amplifier) {
        super.addAttributeModifiers(entity, attributes, amplifier);

        if (entity.level().isClientSide) {
            return;
        }

        CommonMiscComponent misc = JComponentPlatformUtils.getMiscData(entity);
        misc.setPrevNoGrav(entity.isNoGravity());
        misc.setHoverTime(0);
    }

    @Override
    public void removeAttributeModifiers(final LivingEntity entity, final AttributeMap attributes, final int amplifier) {
        super.removeAttributeModifiers(entity, attributes, amplifier);

        if (entity.level().isClientSide) {
            return;
        }
        GravityChangerAPI.clearGravity(entity);
        if (!JComponentPlatformUtils.getMiscData(entity).getPrevNoGrav()) {
            entity.setNoGravity(false);
        }
    }
}
