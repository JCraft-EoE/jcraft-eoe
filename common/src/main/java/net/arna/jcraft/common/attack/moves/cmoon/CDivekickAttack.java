package net.arna.jcraft.common.attack.moves.cmoon;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractChargeAttack;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.common.entity.stand.CMoonEntity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;

public final class CDivekickAttack extends AbstractChargeAttack<CDivekickAttack, CMoonEntity, CMoonEntity.State> {
    @Getter @Setter
    private Vec3 chargeDir;

    float originalHitboxSize;

    public CDivekickAttack(final int cooldown, final int windup, final int duration, final float moveDistance, final float damage, final int stun,
                          final float hitboxSize, final float knockback, final float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset, CMoonEntity.State.DIVEKICK_HIT);
        originalHitboxSize = hitboxSize;
    }

    @Override
    public @NonNull MoveType<CDivekickAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public void onInitiate(final CMoonEntity attacker) {
        super.onInitiate(attacker);

        withHitboxSize(originalHitboxSize);

        attacker.getUserOrThrow().addEffect(new MobEffectInstance(
                MobEffects.LEVITATION, 10, 2, true, false
        ));
    }

    @Override
    public void activeTick(CMoonEntity attacker, int moveStun) {
        super.activeTick(attacker, moveStun);

        boolean hasntHit = getHitboxSize() > 0;

        if (hasntHit) {
            final var gravity = GravityChangerAPI.getGravityDirection(attacker);

            chargeDir = new Vec3(gravity.step());

            if (moveStun <= getWindupPoint() && (getDuration() - moveStun) % 3 == 0){
                JComponentPlatformUtils.getShockwaveHandler(attacker.level())
                        .addShockwave(attacker.position(), chargeDir, 3.0f);
            }
        }
    }

    @Override
    protected void endCharge(final CMoonEntity attacker) {
        withHitboxSize(0);
        chargeDir = chargeDir.scale(0.25);
        attacker.setMoveStun(10);
        attacker.setState(hitAnimState);
    }

    @Override
    protected Vec3 advanceChargePos(final StandEntity<?, ?> attacker, final float moveDistance, final int windupPoint) {
        return attacker.position().add(chargeDir.scale(moveDistance / windupPoint));
    }

    @NonNull
    @Override
    protected CDivekickAttack getThis() {
        return this;
    }

    @NonNull
    @Override
    public CDivekickAttack copy() {
        return copyExtras(new CDivekickAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(), getStun(),
                getHitboxSize(), getKnockback(), getOffset()));
    }

    public static class Type extends AbstractChargeAttack.Type<CDivekickAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<CDivekickAttack>, CDivekickAttack> buildCodec(RecordCodecBuilder.Instance<CDivekickAttack> instance) {
            return attackDefault(instance, CDivekickAttack::new);
        }
    }
}
