package net.arna.jcraft.common.attack.moves.tusk;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.Attacks;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.entity.damage.JDamageSources;
import net.arna.jcraft.common.entity.projectile.NailProjectile;
import net.arna.jcraft.common.entity.stand.TuskAct3Entity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.util.CooldownType;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.Set;

/**
 * Tusk Act 3 - Wormhole (Ultimate)
 * First press: Fires a slow homing nail.
 * Second press (while nail is alive): Teleports to the nail. Deals damage in configurable radius.
 * Like KQBTD's Bites the Dust — initMove routes to teleport when nail is active.
 */
public final class WormholeAttack extends AbstractMove<WormholeAttack, TuskAct3Entity> {
    private WeakReference<NailProjectile> wormholeNail;
    private boolean nailActive = false;

    // Configurable via .withTeleportDamage() in the moveset definition
    private float teleportDamage = 0f;
    private float teleportBlastRadius = 0f;

    public WormholeAttack(int cooldown, int windup, int duration, float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
        ranged = true;
        manualCooldown = true;
    }

    /** Set the teleport arrival damage and blast radius (call from moveset builder). */
    public WormholeAttack withTeleportDamage(float damage, float blastRadius) {
        this.teleportDamage = damage;
        this.teleportBlastRadius = blastRadius;
        return this;
    }

    @Override
    public @NotNull MoveType<WormholeAttack> getMoveType() {
        return Type.INSTANCE;
    }

    public boolean isNailActive() {
        return nailActive;
    }

    /**
     * Immediately teleports to the nail — bypasses ALL cooldown/movestun checks.
     * Called from initMove (before super.initMove), like BTD detonation.
     */
    public boolean doTeleportNow(TuskAct3Entity attacker, LivingEntity user) {
        final NailProjectile nail = wormholeNail == null ? null : wormholeNail.get();
        if (nail == null || !nail.isAlive()) return false;
        doTeleport(attacker, user, nail);
        return true;
    }

    @Override
    public void tick(final TuskAct3Entity attacker) {
        final NailProjectile nail = wormholeNail == null ? null : wormholeNail.get();
        nailActive = nail != null && nail.isAlive();
        if (!nailActive) {
            wormholeNail = null;
        }
    }

    @Override
    public @NonNull Set<LivingEntity> perform(TuskAct3Entity attacker, LivingEntity user) {
        fireNail(attacker, user);
        return Set.of();
    }

    private void doTeleport(TuskAct3Entity attacker, LivingEntity user, NailProjectile nail) {
        Vec3 nailPos = nail.position();
        user.teleportTo(nailPos.x, nailPos.y, nailPos.z);

        // Damage in blast radius (configured via withTeleportDamage in the moveset)
        if (teleportDamage > 0 && teleportBlastRadius > 0) {
            float r = teleportBlastRadius;
            AABB blastBox = new AABB(nailPos.x - r, nailPos.y - r, nailPos.z - r,
                    nailPos.x + r, nailPos.y + r, nailPos.z + r);
            attacker.level().getEntitiesOfClass(LivingEntity.class, blastBox,
                    e -> e != user && e.isAlive() && !e.isSpectator())
                    .forEach(e -> Attacks.trueDamage(teleportDamage, JDamageSources.stand(attacker), e));
        }

        nail.discard();
        wormholeNail = null;
        nailActive = false;
    }

    private void fireNail(TuskAct3Entity attacker, LivingEntity user) {
        NailProjectile nail = NailProjectile.wormholeFromTuskAct3(attacker);
        if (nail == null) return;

        JComponentPlatformUtils.getCooldowns(user).setCooldown(CooldownType.STAND_ULTIMATE, getCooldown());

        // Redirect through handhole if active, otherwise fire from eye level with crosshair aim
        if (!attacker.redirectThroughHandhole(nail, user, 0.5f)) {
            Vec3 spawnPos = user.position().add(GravityChangerAPI.getEyeOffset(user).scale(0.75));
            nail.setPos(spawnPos);
            Vec3 target = JUtils.getCrosshairTarget(user, 50.0);
            nail.setDeltaMovement(target.subtract(spawnPos).normalize().scale(0.5));
        }

        attacker.level().addFreshEntity(nail);
        wormholeNail = new WeakReference<>(nail);
        nailActive = true;
    }

    @Override
    protected @NonNull WormholeAttack getThis() {
        return this;
    }

    @Override
    public @NonNull WormholeAttack copy() {
        WormholeAttack copy = new WormholeAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance());
        copy.teleportDamage = this.teleportDamage;
        copy.teleportBlastRadius = this.teleportBlastRadius;
        return copyExtras(copy);
    }

    public static class Type extends AbstractMove.Type<WormholeAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NotNull App<RecordCodecBuilder.Mu<WormholeAttack>, WormholeAttack>
        buildCodec(RecordCodecBuilder.Instance<WormholeAttack> instance) {
            return baseDefault(instance, WormholeAttack::new);
        }
    }
}
