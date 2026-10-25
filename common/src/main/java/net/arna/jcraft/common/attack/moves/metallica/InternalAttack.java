package net.arna.jcraft.common.attack.moves.metallica;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.Attacks;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.arna.jcraft.common.entity.projectile.RazorProjectile;
import net.arna.jcraft.common.entity.stand.MetallicaEntity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.lang.ref.WeakReference;
import java.util.Set;

public class InternalAttack<A extends IAttacker<? extends A, ?>> extends AbstractMove<InternalAttack<A>, A> {
    private static final int RAZOR_VOMIT_DURATION = 20;
    private WeakReference<LivingEntity> razorTarget;
    private int razorTime;

    public InternalAttack(int cooldown, int windup, int duration) {
        super(cooldown, windup, duration, 0);
        manualCooldown = true;
    }

    @Override
    public @NonNull MoveType<InternalAttack<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    public void tick(final A attacker) {
        final LivingEntity target = razorTarget == null ? null : razorTarget.get();
        if (razorTime > 0 && target != null && target.isAlive()) {
            if (razorTime % 2 == 0) {
                RazorProjectile razor = new RazorProjectile(attacker.getBaseEntity().level(), attacker.getUserOrThrow());
                razor.setPos(target.position().add(GravityChangerAPI.getEyeOffset(target)));
                JUtils.shoot(razor, attacker.getUserOrThrow(), target.getXRot(), target.getYRot(), target.getRandom().nextFloat() - 0.5f, 0.5f, 30.0f);
                attacker.getBaseEntity().level().addFreshEntity(razor);
            }

            razorTime--;
        }
    }

    @Override
    public @NonNull Set<LivingEntity> perform(A attacker, LivingEntity user) {
        final Vec3 eyePos = user.position().add(GravityChangerAPI.getEyeOffset(user));
        final Vec3 rotVec = user.getLookAngle();
        final HitResult hitResult = JUtils.raycastAll(user, eyePos, eyePos.add(rotVec.scale(12.0)), ClipContext.Fluid.NONE, EntitySelector.LIVING_ENTITY_STILL_ALIVE);
        // JCraft.createParticle((ServerLevel) user.level(), hitResult.getLocation().x, hitResult.getLocation().y, hitResult.getLocation().z, JParticleType.STUN_PIERCE);
        if (hitResult instanceof EntityHitResult entityHitResult && entityHitResult.getEntity() instanceof LivingEntity living) {
            final LivingEntity target = JUtils.getUserIfStand(living);
            if (target.hasEffect(JStatusRegistry.HYPOXIA.get())) {
                if (user instanceof ServerPlayer serverPlayer) {
                    serverPlayer.displayClientMessage(Component.literal("Cannot attack hypoxic targets."), true);
                }
            } else {
                ServerLevel serverWorld = (ServerLevel) user.level();
                final double x = target.getX(), y = target.getY(), z = target.getZ();
                final RandomSource random = attacker.getBaseEntity().getRandom();
                for (int i = 0; i < 3; i++) {
                    JCraft.createParticle(serverWorld,
                            x + random.nextGaussian(),
                            y + random.nextGaussian(),
                            z + random.nextGaussian(),
                            JParticleType.SWEEP_ATTACK
                    );
                }

                Attacks.damage(attacker.getBaseEntity(), 3.5f, serverWorld.damageSources().sting(user), target);
                target.addEffect(new MobEffectInstance(JStatusRegistry.HYPOXIA.get(), 60, 0, false, true));
                JComponentPlatformUtils.getCooldowns(user).setCooldown(getMoveClass().getDefaultCooldownType(), getCooldown());

                razorTarget = new WeakReference<>(target);
                razorTime = RAZOR_VOMIT_DURATION;
            }
        }

        return Set.of();
    }

    @Override
    protected @NonNull InternalAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull InternalAttack<A> copy() {
        return copyExtras(new InternalAttack<>(getCooldown(), getWindup(), getDuration()));
    }

    public static class Type extends AbstractMove.Type<InternalAttack<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<InternalAttack<?>>, InternalAttack<?>> buildCodec(RecordCodecBuilder.Instance<InternalAttack<?>> instance) {
            return instance.group(extras(), cooldown(), windup(), duration()).apply(instance, applyExtras(InternalAttack::new));
        }
    }
}
