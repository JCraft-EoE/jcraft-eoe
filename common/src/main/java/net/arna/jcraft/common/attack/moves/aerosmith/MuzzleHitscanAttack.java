package net.arna.jcraft.common.attack.moves.aerosmith;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.enums.MoveInputType;
import net.arna.jcraft.api.attack.moves.AbstractHitscanAttack;
import net.arna.jcraft.api.splatter.JSplatterManager;
import net.arna.jcraft.common.entity.stand.AerosmithEntity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.splatter.GasolineSplatter;
import net.arna.jcraft.common.util.JParticleType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public class MuzzleHitscanAttack extends AbstractHitscanAttack<MuzzleHitscanAttack, AerosmithEntity> {
    private static final float HALF_PI = (float)Math.PI/2;

    private final float originalSpread;
    private int shootCount;
    private boolean wantToContinue;

    public MuzzleHitscanAttack(final int cooldown, final int windup, final int duration, final float moveDistance,
                               final float damage, final int stun, final float knockback, final float range, final float spread) {
        super(cooldown, windup, duration, moveDistance, damage, stun, knockback, range, spread);
        originalSpread = spread;
        cancelMoves = false;
    }

    @Override
    public void onInitiate(AerosmithEntity attacker) {
        super.onInitiate(attacker);
        wantToContinue = true;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final AerosmithEntity attacker, final LivingEntity user) {
        attacker.addOverheat(0.225f);
        spread = Mth.clamp(originalSpread + attacker.getOverheat() / 100, 0f, HALF_PI);

        Vec3 hitPos = attacker.isRemote() ?
                fire(attacker, user, attacker.position(), attacker.getLookAngle()) :
                fire(attacker, user, user.position().add(GravityChangerAPI.getEyeOffset(user)), user.getLookAngle());

        // Heat the block at the bullet's hit position for the durationTicks
        // hitPos often lands exactly on a face boundary, so the containing pos resolves to air — fall back one block down
        BlockPos hitBlock = BlockPos.containing(hitPos);
        if (user.level().getBlockState(hitBlock).isAir()) {
            hitBlock = hitBlock.below();
        }
        if (!user.level().getBlockState(hitBlock).isAir()) {
            BulletHeatManager.heatBlock(user.level(), hitBlock, 20); //edit this to change the heated blocks heat time
        }

        // Ignite any gasoline splatters at the bullet's hit position, but with a slight delay
        JSplatterManager.get(user.level())
                .getHit(hitPos, s -> s instanceof GasolineSplatter)
                .forEach(s -> ((GasolineSplatter) s).lightOnFireDelayed(3));

        final Vec3 eyes = attacker.getEyePosition();
        final float rot = attacker.getYRot(); // in degrees
        final double x = Math.cos(Math.toRadians(rot));
        final double z = Math.sin(Math.toRadians(rot));
        final int side = (shootCount++ % 2) * 2 - 1;
        JCraft.createParticle((ServerLevel)user.level(),
                eyes.x() + 0.6 * side * x - 0.2 * z,
                eyes.y() - 0.8,
                eyes.z() + 0.6 * side * z + 0.2 * x,
                JParticleType.HIT_SPARK_1);

        return Set.of();
    }

    @Override
    public void onUserMoveInput(final AerosmithEntity attacker, final MoveInputType type, final boolean pressed, final boolean moveInitiated) {
        super.onUserMoveInput(attacker, type, pressed, moveInitiated);
        // Only handle release here. The press is handled by onInitiate() to avoid a multiplayer
        // race condition where the thenAccept callback in PlayerInputPacket queues onUserMoveInput(true)
        // after the release's onUserMoveInput(false) has already run, re-locking the attack on.
        if (getMoveClass() == type.getMoveClass() && !pressed) {
            wantToContinue = false;
        }
    }

    @Override
    public void tick(AerosmithEntity attacker) {
        super.tick(attacker);

        if (wantToContinue && attacker.getMoveStun() < 1) {
            attacker.cancelMove(false);
            attacker.initMove(getMoveClass());
        }
    }

    @Override
    public void onDeactivate(AerosmithEntity attacker) {
        super.onDeactivate(attacker);

        if (!wantToContinue)
            attacker.stopVolaSound();
    }

    @Override
    protected Vec3 hitscanTraceParticleOrigin(final AerosmithEntity attacker) {
        final Vec3 eyes = attacker.getEyePosition();
        final float rot = attacker.getYRot(); // in degrees
        final double x = Math.cos(Math.toRadians(rot));
        final double z = Math.sin(Math.toRadians(rot));
        int side = (shootCount % 2) * 2 - 1;
        return new Vec3(
                eyes.x() + 0.6 * side * x - 0.2 * z,
                eyes.y() - 0.8,
                eyes.z() + 0.6 * side * z + 0.2 * x);
    }

    @Override
    protected Vec3 hitscanTraceParticleVelocity(final AerosmithEntity attacker, final Vec3 goal) {
        final Vec3 start = hitscanTraceParticleOrigin(attacker);
        return goal.subtract(start).scale(0.16);
    }

    @Override
    protected float getBlockDestructionMultiplier(AerosmithEntity attacker) {
        return super.getBlockDestructionMultiplier(attacker) * Mth.clamp(1 - attacker.getOverheat() / 100f, 0, 1);
    }

    @Override
    public @NonNull MoveType<MuzzleHitscanAttack> getMoveType() {
        return MuzzleHitscanAttack.Type.INSTANCE.cast();
    }

    @Override
    protected @NonNull MuzzleHitscanAttack getThis() {
        return this;
    }

    @Override
    public @NonNull MuzzleHitscanAttack copy() {
        return copyExtras(new MuzzleHitscanAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(), getStun(),
                getKnockback(), getRange(), originalSpread));
    }

    public static class Type extends AbstractHitscanAttack.Type<MuzzleHitscanAttack> {
        public static final MuzzleHitscanAttack.Type INSTANCE = new MuzzleHitscanAttack.Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<MuzzleHitscanAttack>, MuzzleHitscanAttack> buildCodec(RecordCodecBuilder.Instance<MuzzleHitscanAttack> instance) {
            return hitscanDefault(instance, MuzzleHitscanAttack::new);
        }
    }
}