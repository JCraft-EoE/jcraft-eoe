package net.arna.jcraft.common.attack.moves.killerqueen;

import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.component.living.BombTrackerComponent;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.entity.damage.JDamageSources;
import net.arna.jcraft.common.entity.stand.KillerQueenEntity;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.registry.JSoundRegistry;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.Set;

@Getter
public class KQGrabHitAttack extends AbstractMove<KQGrabHitAttack, KillerQueenEntity> {
    private final int stun;

    public KQGrabHitAttack(int cooldown, int windup, int duration, float moveDistance, int stun) {
        super(cooldown, windup, duration, moveDistance);
        this.stun = stun;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(KillerQueenEntity attacker, LivingEntity user, MoveContext ctx) {
        attacker.playSound(JSoundRegistry.KQ_DETONATE, 1, 1);

        BombTrackerComponent.BombData bombData = JComponents.getBombTracker(user).getMainBomb();

        if (bombData.bombEntity instanceof LivingEntity livingEntity) {
            ServerWorld world = (ServerWorld) attacker.getWorld();

            Vec3d pos = livingEntity.getPos();
            JCraft.createParticle(world, pos.x, pos.y, pos.z, JParticleType.BOOM);
            JUtils.serverPlaySound(JSoundRegistry.KQ_EXPLODE, world, pos, 96);

            DamageSource damageSource = JDamageSources.stand(attacker);

            StandEntity.damageLogic(world, livingEntity, new Vec3d(0, 1, 0), stun, 3, true,
                    11f, false, 4, damageSource, user, null);
            livingEntity.addStatusEffect(new StatusEffectInstance(JStatusRegistry.KNOCKDOWN, 35, 0, true, false));
        }

        bombData.reset();

        return Set.of();
    }

    @Override
    protected @NonNull KQGrabHitAttack getThis() {
        return this;
    }

    @Override
    public @NonNull KQGrabHitAttack copy() {
        return copyExtras(new KQGrabHitAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getStun()));
    }
}
