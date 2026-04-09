package net.arna.jcraft.common.attack.moves.tusk;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.entity.projectile.NailProjectile;
import net.arna.jcraft.common.entity.stand.TuskAct3Entity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.Set;

/**
 * Tusk Act 3 - Handhole (Special 1)
 *
 * First press: Fires a nail that sticks to a surface. An arm "hole" is opened there.
 * While active, pressing Light fires a Golden Rectangle Nail from that position.
 * The hole stays open for up to 20 ticks idle, or until another press discards it.
 */
public final class HandholeAttack extends AbstractMove<HandholeAttack, TuskAct3Entity> {
    private WeakReference<NailProjectile> handholeNail;
    private boolean holeActive = false;
    private int idleTicks = 0;
    private static final int IDLE_TIMEOUT = 200;

    public HandholeAttack(int cooldown, int windup, int duration, float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
        ranged = true;
    }

    @Override
    public @NotNull MoveType<HandholeAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public void tick(TuskAct3Entity attacker) {
        NailProjectile nail = handholeNail == null ? null : handholeNail.get();
        if (nail != null && nail.isAlive()) {
            if (nail.inGround || nail.entityStuckTo != null) {
                idleTicks++;
                if (idleTicks > IDLE_TIMEOUT) {
                    nail.discard();
                    holeActive = false;
                    handholeNail = null;
                    idleTicks = 0;
                }
            }
        } else {
            holeActive = false;
            handholeNail = null;
        }
        // Keep TuskAct3Entity's direct reference in sync — used for M1 intercept
        NailProjectile live = handholeNail == null ? null : handholeNail.get();
        attacker.activeHandholeNail = (live != null && live.isAlive()
                && (live.inGround || live.entityStuckTo != null)) ? live : null;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(TuskAct3Entity attacker, LivingEntity user) {
        NailProjectile existing = handholeNail == null ? null : handholeNail.get();

        if (existing != null && existing.isAlive() && (existing.inGround || existing.entityStuckTo != null)) {
            // Second press with an active hole: discard it (recall arm)
            existing.discard();
            holeActive = false;
            handholeNail = null;
            idleTicks = 0;
            attacker.activeHandholeNail = null;
            return Set.of();
        }

        // Fire the handhole nail
        NailProjectile nail = NailProjectile.handholeFromTuskAct3(attacker);
        if (nail == null) return Set.of();

        Vec3 spawnPos = attacker.position().add(GravityChangerAPI.getEyeOffset(user).scale(0.75));
        nail.setPos(spawnPos);
        Vec3 target = JUtils.getCrosshairTarget(user, 50.0);
        nail.setDeltaMovement(target.subtract(spawnPos).normalize().scale(2.5));

        attacker.level().addFreshEntity(nail);
        handholeNail = new WeakReference<>(nail);
        holeActive = true;
        idleTicks = 0;

        return Set.of();
    }

    /**
     * Called by TuskAct3Entity when user presses Light while handhole is active.
     * Fires a shot from the handhole's position.
     * @return true if a shot was fired from the hole
     */
    public boolean tryFireFromHole(TuskAct3Entity attacker, LivingEntity user) {
        NailProjectile hole = handholeNail == null ? null : handholeNail.get();
        if (hole == null || !hole.isAlive() || !hole.inGround) return false;

        NailProjectile shot = NailProjectile.handholeShot(attacker);
        if (shot == null) return false;

        Vec3 shotOrigin = hole.position().add(0, 1, 0);
        shot.setPos(shotOrigin);

        // Aim at the exact point the crosshair is targeting (not just the look direction from a different position)
        Vec3 eyePos = user.getEyePosition();
        Vec3 targetPoint = eyePos.add(user.getLookAngle().scale(50.0));
        Vec3 shootDir = targetPoint.subtract(shotOrigin).normalize();
        shot.setDeltaMovement(shootDir.scale(2.5));

        attacker.level().addFreshEntity(shot);
        idleTicks = 0; // Reset idle timer on use
        return true;
    }

    public boolean isHoleActive() {
        NailProjectile hole = handholeNail == null ? null : handholeNail.get();
        return hole != null && hole.isAlive() && hole.inGround;
    }

    @Override
    protected @NonNull HandholeAttack getThis() {
        return this;
    }

    @Override
    public @NonNull HandholeAttack copy() {
        return copyExtras(new HandholeAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }

    public static class Type extends AbstractMove.Type<HandholeAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NotNull App<RecordCodecBuilder.Mu<HandholeAttack>, HandholeAttack>
        buildCodec(RecordCodecBuilder.Instance<HandholeAttack> instance) {
            return baseDefault(instance, HandholeAttack::new);
        }
    }
}
