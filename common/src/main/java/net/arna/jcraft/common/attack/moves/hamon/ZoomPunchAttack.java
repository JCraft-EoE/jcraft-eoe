package net.arna.jcraft.common.attack.moves.hamon;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.api.component.living.CommonHamonComponent;
import net.arna.jcraft.api.registry.JAdvancementTriggerRegistry;
import net.arna.jcraft.common.spec.HamonSpec;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;

import java.util.Set;

public final class ZoomPunchAttack extends AbstractSimpleAttack<ZoomPunchAttack, HamonSpec> {
    public static final float CHARGE_COST = 10.0F;
    public ZoomPunchAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun,
                         float hitboxSize, float knockback, float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
    }

    @Override
    public @NonNull MoveType<ZoomPunchAttack> getMoveType() {
        return ZoomPunchAttack.Type.INSTANCE;
    }

    @Override
    public void onInitiate(HamonSpec attacker) {
        super.onInitiate(attacker);

        attacker.drainCharge(CHARGE_COST);
        attacker.setUseHamonNext(false);

        final float pitch = attacker.user.getXRot();

        if (pitch <= -20.0F) {
            withOffset(-0.5F);
            attacker.setAnimation("hm.zp.hi", 24, 1.0f);
            return;
        }

        if (pitch >= 20.0F) {
            withOffset(0.5F);
            attacker.setAnimation("hm.zp.lo", 24, 1.0f);
            return;
        }

        withOffset(0.0F);
        attacker.setAnimation("hm.zp.mi", 24, 1.0f);
    }

    @Override
    public void tick(HamonSpec attacker) {
        super.tick(attacker);

        if (!(attacker.getCurrentMove() instanceof ZoomPunchAttack)) return;

        final int moveStun = attacker.getMoveStun();

        if (moveStun == 13 || moveStun == 12) {
            JUtils.addVelocity(
                    attacker.user,
                    attacker.user.getLookAngle().scale(0.2)
            );
        }
    }

    @Override
    public @NonNull Set<LivingEntity> perform(HamonSpec attacker, LivingEntity user) {
        final Set<LivingEntity> targets = super.perform(attacker, user);

        if (JUtils.getSpec(user) instanceof final HamonSpec hamonSpec) {
            for (LivingEntity target : targets) {
                hamonSpec.processTarget(target);
                if (user instanceof final ServerPlayer player && target instanceof Enemy) {
                    JAdvancementTriggerRegistry.HAMON3.trigger(player);
                    final CommonHamonComponent hamon = JComponentPlatformUtils.getHamon(player);
                    if (player.getServer() != null) {
                        hamon.setLastZoomPunched(target.getUUID());
                    }
                }
            }
        }

        return targets;
    }

    @Override
    protected @NonNull ZoomPunchAttack getThis() {
        return this;
    }

    @Override
    public @NonNull ZoomPunchAttack copy() {
        return copyExtras(new ZoomPunchAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(),
                getStun(), getHitboxSize(), getKnockback(), getOffset()));
    }

    public static class Type extends AbstractSimpleAttack.Type<ZoomPunchAttack> {
        public static final ZoomPunchAttack.Type INSTANCE = new ZoomPunchAttack.Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<ZoomPunchAttack>, ZoomPunchAttack> buildCodec(RecordCodecBuilder.Instance<ZoomPunchAttack> instance) {
            return attackDefault(instance, ZoomPunchAttack::new);
        }
    }
}
