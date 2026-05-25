package net.arna.jcraft.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.arna.jcraft.api.AttackData;
import net.arna.jcraft.api.Attacks;
import net.arna.jcraft.api.attack.enums.StunType;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.api.registry.JParticleTypeRegistry;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.common.attack.moves.hamon.ImproviserMove;
import net.arna.jcraft.common.spec.HamonSpec;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(Projectile.class)
public abstract class ProjectileMixin {

    @Shadow
    private boolean hasBeenShot;

    @Unique
    private HamonSpec jcraft$hamon;

    @Inject(method = "setOwner(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"))
    protected void jcraft$hamonize(final Entity owner, final CallbackInfo ci) {
        final Projectile projectile = (Projectile)(Object)this;
        if (projectile.tickCount == 0 && owner instanceof LivingEntity living &&
                JUtils.getSpec(living) instanceof HamonSpec hamon &&
                hamon.getCurrentMove() instanceof ImproviserMove &&
                hamon.getCharge() >= ImproviserMove.PROJECTILE_IMPROVISER_CHARGE
        ) {
            hamon.drainCharge(ImproviserMove.PROJECTILE_IMPROVISER_CHARGE);
            jcraft$hamon = hamon;
        }
    }

    /**
     * If the projectile should display hamon particles. Only gets called if the projectile is hamonized.
     * @return <code>true</code> if particles should be displayed, <code>false</code> otherwise.
     */
    @Unique
    protected boolean jcraft$shouldDisplayHamonParticles() {
        return hasBeenShot && !((Projectile)(Object)this).onGround();
    }

    @WrapMethod(method = "tick()V")
    protected void jcraft$hamonTrail(final Operation<Void> original) {
        if (jcraft$hamon == null) {
            original.call();
            return;
        }
        final Projectile projectile = (Projectile)(Object)this;
        if (!hasBeenShot) {
            projectile.playSound(JSoundRegistry.HAMON_RING.get());
        }
        original.call();
        if (!projectile.level().isClientSide()) {
            final Vec3 position = projectile.position();
            if (jcraft$shouldDisplayHamonParticles()) {
                var packet = new ClientboundLevelParticlesPacket(JParticleTypeRegistry.HAMON_SPARK.get(),
                        false,
                        position.x(), position.y(), position.z(),
                        0.1f, 0.1f, 0.1f,
                        0.2f, 1);
                for (ServerPlayer tracker : JUtils.tracking(projectile)) {
                    tracker.connection.send(packet);
                }
            }
        }
    }

    @Inject(method = "onHit(Lnet/minecraft/world/phys/HitResult;)V", at = @At("HEAD"))
    protected void jcraft$hamonHit(final HitResult result, final CallbackInfo ci) {
        if (jcraft$hamon == null || result.getType() == HitResult.Type.MISS) {
            return;
        }
        final Projectile projectile = (Projectile)(Object)this;
        final Entity owner = projectile.getOwner();
        final Level level = projectile.level();
        final Vec3 center = result.getLocation();
        final double size = 1.0;
        final Set<LivingEntity> possibleTargets = JUtils.generateHitbox(level, center, size, Set.of());
        for (final LivingEntity living : possibleTargets) {
            if (living == owner || JUtils.getUserIfStand(living) == owner) {
                continue;
            }
            jcraft$hamon.processTarget(living);
            Attacks.damageLogic(level, living, new AttackData(
                    Vec3.ZERO, 10, StunType.BURSTABLE.ordinal(), false,
                    3f, true, 3, level.damageSources().indirectMagic(owner, null),
                    owner, CommonHitPropertyComponent.HitAnimation.CRUSH, null,
                    false, false
            ));
        }
        // add hit particle effect
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    JParticleTypeRegistry.HITSPARK_1.get(), // Use hitspark_1 for block hits
                    center.x(), center.y(), center.z(),
                    1, // particle count
                    0, 0, 0, // spread
                    0 // speed
            );
        }
    }

}
