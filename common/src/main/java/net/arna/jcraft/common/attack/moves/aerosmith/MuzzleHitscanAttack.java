package net.arna.jcraft.common.attack.moves.aerosmith;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractHitscanAttack;
import net.arna.jcraft.common.entity.stand.AerosmithEntity;
import net.arna.jcraft.common.util.JParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public class MuzzleHitscanAttack extends AbstractHitscanAttack<MuzzleHitscanAttack, AerosmithEntity> {

    private static final float HALF_PI = (float)Math.PI/2;

    private float originalBreakChance;
    private float originalSpread;
    private int shootCount;

    public MuzzleHitscanAttack(final int cooldown, final int windup, final int duration, final float moveDistance, final float damage, final int stun, final float knockback, final float range, final float hardness, final float breakChance, final float spread) {
        super(cooldown, windup, duration, moveDistance, damage, stun, knockback, range, hardness, breakChance, spread);
        originalBreakChance = breakChance;
        originalSpread = spread;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final AerosmithEntity attacker, final LivingEntity user) {
        attacker.addOverheat(0.225f);
        withBreakChance(Mth.clamp(originalBreakChance - attacker.getOverheat() / 100, 0f, 1f));
        withSpread(Mth.clamp(originalSpread + attacker.getOverheat() / 100, 0f, HALF_PI));
        final Set<LivingEntity> targets = super.perform(attacker, user);
        if (user == null) {
            return targets;
        }
        final Vec3 eyes = attacker.getEyePosition();
        final float rot = attacker.getYRot(); // in degrees
        final double x = Math.cos(Math.toRadians(rot));
        final double z = Math.sin(Math.toRadians(rot));
        int side = (shootCount++ % 2) * 2 - 1;
        JCraft.createParticle((ServerLevel)user.level(),
                // second summand moves it to the left/right (or up/down in case of y), third summand moves it forwards/backwards
                eyes.x() + 0.6 * side * x - 0.2 * z,
                eyes.y() - 0.8,
                eyes.z() + 0.6 * side * z + 0.2 * x,
                JParticleType.HIT_SPARK_1);
        return targets;
    }

    @Override
    protected Vec3 hitscanTraceParticleOrigin(final AerosmithEntity attacker) {
        final Vec3 eyes = attacker.getEyePosition();
        final float rot = attacker.getYRot(); // in degrees
        final double x = Math.cos(Math.toRadians(rot));
        final double z = Math.sin(Math.toRadians(rot));
        int side = (shootCount % 2) * 2 - 1;
        return new Vec3(
                // second summand moves it to the left/right (or up/down in case of y), third summand moves it forwards/backwards
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
                getKnockback(), getRange(), getHardness(), originalBreakChance, originalSpread));
    }

    public static class Type extends AbstractHitscanAttack.Type<MuzzleHitscanAttack> {
        public static final MuzzleHitscanAttack.Type INSTANCE = new MuzzleHitscanAttack.Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<MuzzleHitscanAttack>, MuzzleHitscanAttack> buildCodec(RecordCodecBuilder.Instance<MuzzleHitscanAttack> instance) {
            return hitscanDefault(instance, MuzzleHitscanAttack::new);
        }
    }
}
