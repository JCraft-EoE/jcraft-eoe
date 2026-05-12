package net.arna.jcraft.common.attack.moves.magiciansred;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractTossMove;
import net.arna.jcraft.common.entity.projectile.KnifeProjectile;
import net.arna.jcraft.common.entity.stand.MagiciansRedEntity;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Magician's Red throw move. Thrown items deal fire damage on hit.
 */
public final class MagiciansRedTossMove extends AbstractTossMove<MagiciansRedTossMove, MagiciansRedEntity> {

    public MagiciansRedTossMove(final int cooldown, final int windup, final int duration, final float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
    }

    public MagiciansRedTossMove(final int cooldown, final int windup, final int duration, final float moveDistance, final float velocityMultiplier) {
        super(cooldown, windup, duration, moveDistance, velocityMultiplier);
    }

    public MagiciansRedTossMove(final int cooldown, final int windup, final int duration, final float moveDistance, final float velocityMultiplier, final float spreadMultiplier) {
        super(cooldown, windup, duration, moveDistance, velocityMultiplier, spreadMultiplier);
    }

    @Override
    public @NonNull MoveType<MagiciansRedTossMove> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    protected void handleKnifeBundle(final LivingEntity living, final Level level, final ItemStack itemStack, final float velocity, final float spreadMultiplier, final boolean decrement, final @NonNull Vec3 spawnPos) {
        final LivingEntity owner = JUtils.getUserIfStand(living);
        for (int i = 0; i < 9; i++) {
            final KnifeProjectile knife = new KnifeProjectile(level, owner);
            knife.setPos(spawnPos.add(
                    level.random.triangle(0, 0.5),
                    level.random.triangle(0, 0.5),
                    level.random.triangle(0, 0.5)
            ));
            knife.shootFromRotation(living, living.getXRot(), living.getYRot(), 0f, velocity, 5f * spreadMultiplier);
            knife.setSecondsOnFire(600);
            level.addFreshEntity(knife);
        }
        if (decrement) itemStack.shrink(1);
    }

    @Override
    protected void onTossed(final MagiciansRedEntity attacker, final LivingEntity user, final Entity thrown) {
        if (thrown != null) {
            thrown.setSecondsOnFire(600); //makes thrown entities appear on fire
        }
    }

    @Override
    protected @NonNull MagiciansRedTossMove getThis() {
        return this;
    }

    @Override
    public @NonNull MagiciansRedTossMove copy() {
        return copyExtras(new MagiciansRedTossMove(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getVelocityMultiplier(), getSpreadMultiplier()));
    }

    public static class Type extends AbstractTossMove.Type<MagiciansRedTossMove> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<MagiciansRedTossMove>, MagiciansRedTossMove> buildCodec(final RecordCodecBuilder.Instance<MagiciansRedTossMove> instance) {
            return instance.group(cooldown(), windup(), duration(), moveDistance(), velocityMultiplier(), spreadMultiplier()).apply(instance, MagiciansRedTossMove::new);
        }
    }
}
