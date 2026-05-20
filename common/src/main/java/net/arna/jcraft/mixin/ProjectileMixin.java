package net.arna.jcraft.mixin;

import net.arna.jcraft.api.AttackData;
import net.arna.jcraft.api.Attacks;
import net.arna.jcraft.api.attack.enums.StunType;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.api.registry.JParticleTypeRegistry;
import net.arna.jcraft.common.attack.moves.hamon.ImproviserMove;
import net.arna.jcraft.common.spec.HamonSpec;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(Projectile.class)
public abstract class ProjectileMixin {

    @Unique
    private HamonSpec jcraft$hamon;

    @Inject(method = "setOwner(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"))
    protected void jcraft$hamonize(final Entity owner, final CallbackInfo ci) {
        final Projectile projectile = (Projectile)(Object)this;
        if (projectile.tickCount == 0 && owner instanceof LivingEntity living &&
                JUtils.getSpec(living) instanceof HamonSpec hamon &&
                hamon.getCurrentMove() instanceof ImproviserMove) {
            jcraft$hamon = hamon;
        }
    }

    @Inject(method = "onHit(Lnet/minecraft/world/phys/HitResult;)V", at = @At("HEAD"))
    protected void jcraft$hamonHit(final HitResult result, final CallbackInfo ci) {
        if (jcraft$hamon == null) {
            return;
        }
        final Projectile projectile = (Projectile)(Object)this;
        final Entity owner = projectile.getOwner();
        final Level level = projectile.level();
        final Vec3 center = projectile.position();
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
            var packet = new ClientboundLevelParticlesPacket(JParticleTypeRegistry.HAMON_SPARK.get(),
                    false,
                    living.getX(), living.getY(), living.getZ(),
                    1, 1, 1,
                    0.2f, 10);
            for (ServerPlayer tracker : JUtils.tracking(living))
                tracker.connection.send(packet);
        }
    }

}
