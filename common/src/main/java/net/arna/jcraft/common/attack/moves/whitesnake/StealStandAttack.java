package net.arna.jcraft.common.attack.moves.whitesnake;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.api.component.living.CommonStandComponent;
import net.arna.jcraft.api.registry.JTagRegistry;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.api.stand.StandType;
import net.arna.jcraft.api.stand.StandTypeUtil;
import net.arna.jcraft.common.config.JServerConfig;
import net.arna.jcraft.common.entity.stand.WhiteSnakeEntity;
import net.arna.jcraft.common.item.StandDiscItem;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

public final class StealStandAttack extends AbstractSimpleAttack<StealStandAttack, WhiteSnakeEntity> {

    public StealStandAttack(final int cooldown, final int windup, final int duration, final float moveDistance,
                            final float damage, final int stun, final float hitboxSize,
                            final float knockback, final float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final WhiteSnakeEntity attacker, final LivingEntity user) {
        final Set<LivingEntity> targets = super.perform(attacker, user);

        // Exclusive stands mode — stand stealing is completely disregarded
        if (JServerConfig.EXCLUSIVE_STANDS.getValue()) {
            return targets;
        }

        for (final LivingEntity target : targets) {
            // skip players if the config doesn't allow stealing from them
            if (target instanceof Player && !JServerConfig.WS_STEAL_STANDS_FROM_PLAYERS.getValue()) {
                continue;
            }

            // skip entities flagged as cannot-take-stand-from
            if (target.getType().is(JTagRegistry.CANNOT_TAKE_STAND_FROM)) {
                continue;
            }

            final CommonStandComponent standComp = JComponentPlatformUtils.getStandComponent(target);
            final StandType standType = standComp.getType();
            if (StandTypeUtil.isNone(standType)) { // nothing to steal
                continue;
            }

            final int skin = Mth.clamp(standComp.getSkin(), 0, standType.getData().getInfo().getSkinCount() - 1);

            // Build the disc before dismantling the stand
            final ItemStack disc = StandDiscItem.createDiscStack(standType, skin);

            // Clean up the active stand entity
            final StandEntity<?, ?> stand = standComp.getStand();
            if (stand != null) {
                stand.desummon();
                standComp.setStand(null);
            }

            // Strip the stand type from the target
            standComp.setType(null);

            // Hand the disc to the White Snake user
            if (user instanceof Player player) {
                if (!player.addItem(disc)) {
                    player.drop(disc, false);
                }
            } else {
                user.spawnAtLocation(disc);
            }
        }

        return targets;
    }

    @Override
    protected @NonNull StealStandAttack getThis() {
        return this;
    }

    @Override
    public @NonNull StealStandAttack copy() {
        return copyExtras(new StealStandAttack(
                getCooldown(), getWindup(), getDuration(), getMoveDistance(),
                getDamage(), getStun(), getHitboxSize(), getKnockback(), getOffset()
        ));
    }

    @Override
    public @NonNull MoveType<StealStandAttack> getMoveType() {
        return Type.INSTANCE;
    }

    public static class Type extends AbstractSimpleAttack.Type<StealStandAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<StealStandAttack>, StealStandAttack> buildCodec(
                final RecordCodecBuilder.Instance<StealStandAttack> instance) {
            return attackDefault(instance, StealStandAttack::new);
        }
    }
}
