package net.arna.jcraft.common.attack.moves.aerosmith;

import com.mojang.datafixers.Products;
import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.api.MoveSelectionResult;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.registry.JParticleTypeRegistry;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.common.attack.core.data.BaseMoveExtras;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class BreathXrayMove<A extends IAttacker<? extends A, ?>> extends AbstractMove<BreathXrayMove<A>, A> {
    @Getter
    private final Object2IntMap<LivingEntity> detected = new Object2IntOpenHashMap<>(32);
    private boolean active = true;
    private float range;
    private float scanAngle = 0.0f;
    @Getter
    private boolean requireRemote;

    public BreathXrayMove(int cooldown, float moveDistance, float range, boolean requireRemote) {
        super(cooldown, 0, 0, moveDistance);
        this.range = range;
        this.requireRemote = requireRemote;
    }

    public float getRange() {
        return range;
    }

    public BreathXrayMove<?> withRange(float range) {
        this.range = range;
        return getThis();
    }

    public BreathXrayMove<?> withRequireRemote(boolean require) {
        this.requireRemote = require;
        return getThis();
    }

    @Override
    public void onInitiate(A attacker) {
        super.onInitiate(attacker);
        active = !active;
    }

    @Override
    public void tick(A attacker) {
        final var iterator = detected.object2IntEntrySet().iterator();

        while (iterator.hasNext()) {
            var entry = iterator.next();
            int remainingTicks = entry.getIntValue() - 1;

            if (remainingTicks > 0) {
                entry.setValue(remainingTicks);
                continue;
            }

            iterator.remove();
        }

        scanAngle += 0.1f;
        scanAngle %= Mth.PI * 2.0;

        if (!active) return;

        if (requireRemote && !attacker.isRemote()) return;

        final LivingEntity base = attacker.getBaseEntity();
        if (base == null) return;

        final LivingEntity user = attacker.getUser();
        if (user == null) return;

        if (user instanceof ServerPlayer serverPlayer) {
            final Vec3 pos = base.position();

            boolean doPing = false;

            for (Entity entity : base.level().getEntities().getAll()) {
                if (entity.distanceToSqr(pos) > range * range) continue;

                if (entity == user || entity == base) continue;

                if (entity instanceof LivingEntity living) {
                    final Vec3 target = living.position().add(GravityChangerAPI.getEyeOffset(living));

                    if (!withinScanArc(pos, base.getLookAngle(), target, scanAngle, Mth.DEG_TO_RAD * 30.0)) continue;

                    if (!detected.containsKey(living))
                        if (living.hasLineOfSight(base))
                            doPing = true;

                    detected.put(living, 20);

                    displayBreathParticle(serverPlayer, target);
                }
            }

            if (doPing) playPingSound(serverPlayer);
        }
    }

    /**
     * Checks if a target point lies within a specific slice of a rotation sweep.
     *
     * @param origin      Attacker's position
     * @param lookVec     Attacker's forward facing vector
     * @param target      Target entity's position
     * @param theta       The current sweep offset (in radians) updated every tick
     * @param sweepArc    The width of the scanning beam "slice" (in radians)
     */
    private boolean withinScanArc(final Vec3 origin, final Vec3 lookVec, final Vec3 target, final float theta, final double sweepArc) {
        Vec3 dirToTarget = target.subtract(origin).normalize();

        double attackerYaw = Math.atan2(lookVec.z, lookVec.x);
        double targetYaw = Math.atan2(dirToTarget.z, dirToTarget.x);

        double currentSweepHeading = attackerYaw + theta;

        double angleDiff = targetYaw - currentSweepHeading;
        while (angleDiff <= -Math.PI) angleDiff += Math.PI * 2;
        while (angleDiff > Math.PI) angleDiff -= Math.PI * 2;

        return Math.abs(angleDiff) <= sweepArc / 2.0;
    }

    public static void displayBreathParticle(@NonNull final ServerPlayer serverPlayer, @NonNull final Vec3 target) {
        serverPlayer.connection.send(
                new ClientboundLevelParticlesPacket(
                        JParticleTypeRegistry.OVERLAP.get(),
                        true,
                        target.x, target.y, target.z,
                        0, 0, 0,
                        0,
                        1
                )
        );
    }

    public static void playPingSound(@NonNull final ServerPlayer serverPlayer) {
        final var pos = serverPlayer.position();

        serverPlayer.connection.send(
                new ClientboundSoundPacket(
                        Holder.direct(JSoundRegistry.AS_RADAR_PING.get()),
                        SoundSource.PLAYERS,
                        pos.x, pos.y, pos.z,
                        1, 1, 0
                )
        );
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
        return copyExtras(new BreathXrayMove<>(getCooldown(), getMoveDistance(), getRange(), isRequireRemote()));
    }

    public static class Type extends AbstractMove.Type<BreathXrayMove<?>> {
        public static final Type INSTANCE = new Type();

        protected RecordCodecBuilder<BreathXrayMove<?>, Float> range() {
            return Codec.FLOAT.fieldOf("range").forGetter(BreathXrayMove::getRange);
        }

        protected RecordCodecBuilder<BreathXrayMove<?>, Boolean> requireRemote() {
            return Codec.BOOL.fieldOf("requireRemote").forGetter(BreathXrayMove::isRequireRemote);
        }

        protected Products.P5<RecordCodecBuilder.Mu<BreathXrayMove<?>>, BaseMoveExtras, Integer, Float, Float, Boolean>
        xrayDefault(RecordCodecBuilder.Instance<BreathXrayMove<?>> instance) {
            return instance.group(extras(), cooldown(), moveDistance(), range(), requireRemote());
        }

        @Override
        protected @NotNull App<RecordCodecBuilder.Mu<BreathXrayMove<?>>, BreathXrayMove<?>> buildCodec(final RecordCodecBuilder.Instance<BreathXrayMove<?>> instance) {
            return xrayDefault(instance).apply(instance, applyExtras(BreathXrayMove::new));
        }
    }
}
