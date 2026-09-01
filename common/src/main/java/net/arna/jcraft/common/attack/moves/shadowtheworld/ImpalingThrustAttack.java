package net.arna.jcraft.common.attack.moves.shadowtheworld;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.AttackData;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.api.component.world.CommonShockwaveHandlerComponent;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.arna.jcraft.common.entity.stand.ShadowTheWorldEntity;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static net.arna.jcraft.api.Attacks.damageLogic;

public class ImpalingThrustAttack extends AbstractMove<ImpalingThrustAttack, ShadowTheWorldEntity> {
    public ImpalingThrustAttack(int cooldown, int windup, int duration, float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
    }

    @Override
    public @NonNull MoveType<ImpalingThrustAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(ShadowTheWorldEntity attacker, LivingEntity user) {
        final ServerLevel world = (ServerLevel) attacker.level();
        final CommonShockwaveHandlerComponent shockwaveHandler = JComponentPlatformUtils.getShockwaveHandler(world);

        boolean unblockable = getChargeTime() >= 30;

        final Vec3 start = user.getEyePosition(), end = user.getEyePosition().add(user.getLookAngle().scale(8));
        HitResult hitResult = world.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, user));
        final Vec3 pos2 = hitResult.getLocation();
        final Vec3 towardsVec = pos2.subtract(start);

        final DamageSource playerSource = world.damageSources().mobAttack(user);

        user.teleportToWithTicket(pos2.x, pos2.y, pos2.z);

        final double count = Math.round(start.distanceTo(pos2));

        boolean hitAny = false;
        Set<LivingEntity> processed = new HashSet<>();
        for (int i = 0; i < count; i++) {
            final Vec3 curPos = start.add(towardsVec.scale(i / count));
            if (i % 3 == 0) shockwaveHandler.addShockwave(curPos, towardsVec, 2.25f);

            final Vec3 vec1 = curPos.add(-1, -1, -1);
            final Vec3 vec2 = curPos.add(1, 1, 1);

            JUtils.displayHitbox(world, vec1, vec2);

            final List<LivingEntity> hurt = world.getEntitiesOfClass(LivingEntity.class, new AABB(vec1, vec2),
                    EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(e -> e != attacker && e != user));
            hurt.removeIf(processed::contains);
            if (processed.addAll(hurt)) {
                hitAny = true;
                JCraft.createParticle(world,
                        curPos.x + attacker.getRandom().nextGaussian(),
                        curPos.y + attacker.getRandom().nextGaussian(),
                        curPos.z + attacker.getRandom().nextGaussian(),
                        JParticleType.HIT_SPARK_2
                );
                for (LivingEntity ent : hurt) {
                    final LivingEntity target = JUtils.getUserIfStand(ent);

                    // +6 on hit/-4 on block launcher
                    // +0 if you count STW desummon not letting you block

                    final var attackData = new AttackData(
                            target.position().subtract(curPos).normalize(), 10 + attacker.getDesummonTime(), 3, unblockable,
                            8.0f, true, 12, playerSource, user, CommonHitPropertyComponent.HitAnimation.LAUNCH, null,
                            true, unblockable, true
                    );

                    damageLogic(world, target, attackData);
                    target.addEffect(new MobEffectInstance(JStatusRegistry.KNOCKDOWN.get(), 35, 0, true, false));
                }
            }
        }

        if (hitAny) {
            attacker.playSound(JSoundRegistry.IMPACT_1.get(), 1.0f, 1.0f);
        }

        attacker.playSound(JSoundRegistry.TIME_SKIP.get(), 1f, 1f);
        attacker.playSound(JSoundRegistry.STW_ZAP.get(), 1f, 1f);

        return Set.of();
    }

    @Override
    protected @NonNull ImpalingThrustAttack getThis() {
        return this;
    }

    @Override
    public @NonNull ImpalingThrustAttack copy() {
        return copyExtras(new ImpalingThrustAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }

    public static class Type extends AbstractMove.Type<ImpalingThrustAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<ImpalingThrustAttack>, ImpalingThrustAttack> buildCodec(RecordCodecBuilder.Instance<ImpalingThrustAttack> instance) {
            return baseDefault(instance, ImpalingThrustAttack::new);
        }
    }
}
