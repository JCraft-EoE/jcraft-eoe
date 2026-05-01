package net.arna.jcraft.common.attack.moves.aerosmith;

import com.mojang.datafixers.Products;
import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.MoveSelectionResult;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.registry.JParticleTypeRegistry;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.common.attack.core.data.BaseMoveExtras;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class BreathXrayMove<A extends IAttacker<? extends A, ?>> extends AbstractMove<BreathXrayMove<A>, A> {
    private boolean active = true;
    private float range;

    public BreathXrayMove(int cooldown, float moveDistance, float range) {
        super(cooldown, 0, 0, moveDistance);
        this.range = range;
    }

    public float getRange() {
        return range;
    }

    public BreathXrayMove<?> withRange(float range) {
        this.range = range;
        return getThis();
    }

    @Override
    public void onInitiate(A attacker) {
        super.onInitiate(attacker);
        active = !active;
    }

    @Override
    public void tick(A attacker) {
        if (!active) return;

        final LivingEntity base = attacker.getBaseEntity();

        if (base == null) return;

        final LivingEntity user = attacker.getUser();

        if (user == null) return;

        if (user instanceof ServerPlayer serverPlayer) {
            final Vec3 pos = base.position();

            for (Entity entity : base.level().getEntities().getAll()) {
                if (entity.distanceToSqr(pos) > range * range) continue;

                if (entity == user || entity == base) continue;

                if (entity instanceof LivingEntity living) {
                    final Vec3 target = living.position().add(GravityChangerAPI.getEyeOffset(living));

                    if (living.hasLineOfSight(base)) {
                        serverPlayer.connection.send(
                                new ClientboundLevelParticlesPacket(
                                        JParticleTypeRegistry.OVERLAP.get(),
                                        false,
                                        target.x, target.y, target.z,
                                        0, 0, 0,
                                        0,
                                        1
                                )
                        );
                    }
                }
            }
        }
    }

    @Override
    public MoveSelectionResult specificMoveSelectionCriterion(A attacker, LivingEntity mob, LivingEntity target, int stunTicks, int enemyMoveStun, double distance, StandEntity<?, ?> enemyStand, AbstractMove<?, ?> enemyAttack) {
        return MoveSelectionResult.PASS;
    }

    @Override
    public @NonNull MoveType<BreathXrayMove<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    public @NonNull Set<LivingEntity> perform(A attacker, LivingEntity user) {
        return Set.of();
    }

    @Override
    protected @NonNull BreathXrayMove<A> getThis() {
        return this;
    }

    @Override
    public @NonNull BreathXrayMove<A> copy() {
        return copyExtras(new BreathXrayMove<>(getCooldown(), getMoveDistance(), getRange()));
    }

    public static class Type extends AbstractMove.Type<BreathXrayMove<?>> {
        public static final Type INSTANCE = new Type();

        protected RecordCodecBuilder<BreathXrayMove<?>, Float> range() {
            return Codec.FLOAT.fieldOf("range").forGetter(BreathXrayMove::getRange);
        }

        protected Products.P4<RecordCodecBuilder.Mu<BreathXrayMove<?>>, BaseMoveExtras, Integer, Float, Float>
        xrayDefault(RecordCodecBuilder.Instance<BreathXrayMove<?>> instance) {
            return instance.group(extras(), cooldown(), moveDistance(), range());
        }

        @Override
        protected @NotNull App<RecordCodecBuilder.Mu<BreathXrayMove<?>>, BreathXrayMove<?>> buildCodec(final RecordCodecBuilder.Instance<BreathXrayMove<?>> instance) {
            return xrayDefault(instance).apply(instance, applyExtras(BreathXrayMove::new));
        }
    }
}
