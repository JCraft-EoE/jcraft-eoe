package net.arna.jcraft.common.attack.moves.killerqueen;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.MoveSelectionResult;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.api.component.living.CommonBombTrackerComponent;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.common.entity.damage.JDamageSources;
import net.arna.jcraft.common.entity.projectile.BubbleProjectile;
import net.arna.jcraft.common.entity.stand.AbstractKillerQueenEntity;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

import static net.arna.jcraft.api.Attacks.damageLogic;

public final class KQDetonateAttack extends AbstractMove<KQDetonateAttack, AbstractKillerQueenEntity<?, ?>> {
    public KQDetonateAttack(final int cooldown, final int windup, final int duration, final float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
    }

    @Override
    public @NotNull MoveType<KQDetonateAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final AbstractKillerQueenEntity<?, ?> attacker, final LivingEntity user) {
        final CommonBombTrackerComponent.BombData bombData = JComponentPlatformUtils.getBombTracker(user).getMainBomb();

        final Entity bombEntity = bombData.bombEntity;
        final Vec3 bombPos = bombData.getBombPos();

        if (bombPos != null) {
            if (bombEntity instanceof ItemEntity || bombEntity instanceof BubbleProjectile) {
                bombEntity.discard();
            }
            explode(attacker, user, bombPos);
        }

        bombData.reset();

        return Set.of();
    }

    @Override
    public MoveSelectionResult specificMoveSelectionCriterion(AbstractKillerQueenEntity<?, ?> attacker,
                                                                                  LivingEntity mob, LivingEntity target, int stunTicks,
                                                                                  int enemyMoveStun, double distance, StandEntity<?, ?> enemyStand,
                                                                                  AbstractMove<?, ?> enemyAttack) {
        final Vec3 bombPos = JComponentPlatformUtils.getBombTracker(mob).getMainBomb().getBombPos();
        return bombPos != null && target.distanceToSqr(bombPos) < 9.0D ?
                MoveSelectionResult.USE : MoveSelectionResult.STOP;
    }

    @Override
    protected @NonNull KQDetonateAttack getThis() {
        return this;
    }

    @Override
    public @NonNull KQDetonateAttack copy() {
        return copyExtras(new KQDetonateAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }

    public static void explode(final AbstractKillerQueenEntity<?, ?> stand, final Entity user, final Vec3 pos) {
        explode(stand, user, pos, 11.0f, 4.4);
    }

    public static void explode(final AbstractKillerQueenEntity<?, ?> stand, final Entity user, final Vec3 pos, float damage, double hitboxSize) {
        final ServerLevel serverWorld = (ServerLevel) stand.level();

        JCraft.createParticle(serverWorld, pos.x, pos.y, pos.z, JParticleType.BOOM);
        JUtils.serverPlaySound(JSoundRegistry.KQ_EXPLODE.get(), serverWorld, pos, 96);

        final DamageSource damageSource = JDamageSources.stand(stand);
        final Set<? extends LivingEntity> toExplode = AbstractSimpleAttack.findHits(stand, pos, hitboxSize, damageSource);

        for (LivingEntity living : toExplode) {
            final Vec3 kbVec = living.getEyePosition().subtract(pos).normalize();
            damageLogic(stand.level(), living, kbVec, 2, 3, true, damage, false, 4, damageSource, user, null);
            living.addEffect(new MobEffectInstance(JStatusRegistry.KNOCKDOWN.get(), 35, 0, true, false));
        }
    }

    public static class Type extends AbstractMove.Type<KQDetonateAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<KQDetonateAttack>, KQDetonateAttack> buildCodec(RecordCodecBuilder.Instance<KQDetonateAttack> instance) {
            return baseDefault(instance, KQDetonateAttack::new);
        }
    }
}
