package net.arna.jcraft.common.attack.moves.speedking;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.arna.jcraft.common.entity.damage.JDamageSources;
import net.arna.jcraft.common.entity.stand.SpeedKingEntity;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Set;

import static net.arna.jcraft.api.Attacks.damageLogic;

public final class OverheatAttack extends AbstractMove<OverheatAttack, SpeedKingEntity> {
    private static final double EXPLOSION_RADIUS = 4.4;
    private static final double SEARCH_RANGE = 32.0;

    public OverheatAttack(final int cooldown, final int windup, final int duration, final float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
    }

    @Override
    public @NonNull MoveType<OverheatAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final SpeedKingEntity attacker, final LivingEntity user) {
        if (attacker.level().isClientSide()) return Set.of();

        List<LivingEntity> heatedTargets = attacker.level().getEntitiesOfClass(LivingEntity.class,
                new AABB(user.position().subtract(SEARCH_RANGE, SEARCH_RANGE, SEARCH_RANGE),
                        user.position().add(SEARCH_RANGE, SEARCH_RANGE, SEARCH_RANGE)),
                e -> e != user && e != attacker && e.isAlive()
                        && HeatTrapManager.getHeat(e) > 0
                        && user.getUUID().equals(HeatTrapManager.getAttackerUUID(e)));

        for (LivingEntity target : heatedTargets) {
            int heat = HeatTrapManager.getHeat(target);
            explode(attacker, user, target.position().add(0, target.getBbHeight() * 0.5, 0), heat);
            HeatTrapManager.clearHeat(target);
        }

        return Set.of();
    }

    public static void explode(final SpeedKingEntity stand, final LivingEntity user, final Vec3 pos, int heat) {
        final ServerLevel serverLevel = (ServerLevel) stand.level();

        JCraft.createParticle(serverLevel, pos.x, pos.y, pos.z, JParticleType.BOOM);
        JUtils.serverPlaySound(JSoundRegistry.KQ_EXPLODE.get(), serverLevel, pos, 96);

        final DamageSource damageSource = JDamageSources.stand(stand);
        final Set<? extends LivingEntity> toExplode = AbstractSimpleAttack.findHits(stand, pos, EXPLOSION_RADIUS, damageSource);

        float damage = 5.5f + (heat * 1.5f);
        int boilingDuration = heat * 80;

        for (LivingEntity living : toExplode) {
            final Vec3 kbVec = living.getEyePosition().subtract(pos).normalize();
            damageLogic(stand.level(), living, kbVec, 2, 3, true, damage, false, 4, damageSource, user, null);
            living.addEffect(new MobEffectInstance(JStatusRegistry.BOILING.get(), boilingDuration, 0, false, true));
        }
    }

    @Override
    protected @NonNull OverheatAttack getThis() { return this; }

    @Override
    public @NonNull OverheatAttack copy() {
        return copyExtras(new OverheatAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }

    public static class Type extends AbstractMove.Type<OverheatAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<OverheatAttack>, OverheatAttack> buildCodec(RecordCodecBuilder.Instance<OverheatAttack> instance) {
            return baseDefault(instance, OverheatAttack::new);
        }
    }
}
