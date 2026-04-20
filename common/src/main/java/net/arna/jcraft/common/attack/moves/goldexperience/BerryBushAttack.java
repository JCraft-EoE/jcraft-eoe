package net.arna.jcraft.common.attack.moves.goldexperience;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.stand.GoldExperienceEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public final class BerryBushAttack extends AbstractSimpleAttack<BerryBushAttack, GoldExperienceEntity> {
    private static final BlockState BERRY_BUSH = Blocks.SWEET_BERRY_BUSH.defaultBlockState().setValue(SweetBerryBushBlock.AGE, 1);

    public BerryBushAttack(final int cooldown, final int windup, final int duration, final float moveDistance, final float damage, final int stun,
                           final float hitboxSize, final float knockback, final float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
    }

    @Override
    public @NotNull MoveType<BerryBushAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final GoldExperienceEntity attacker, final LivingEntity user) {
        final Level world = attacker.level();
        final BlockPos blockPos = attacker.blockPosition();
        if (world.getBlockState(blockPos).isAir() && world.getBlockState(blockPos.below()).canOcclude() && world.getGameRules().getRule(JCraft.STAND_GRIEFING).get()) {
            world.setBlockAndUpdate(blockPos, BERRY_BUSH);
        }

        return super.perform(attacker, user);
    }

    @Override
    protected @NonNull BerryBushAttack getThis() {
        return this;
    }

    @Override
    public @NonNull BerryBushAttack copy() {
        return copyExtras(new BerryBushAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(), getStun(),
                getHitboxSize(), getKnockback(), getOffset()));
    }

    public static class Type extends AbstractSimpleAttack.Type<BerryBushAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NotNull App<RecordCodecBuilder.Mu<BerryBushAttack>, BerryBushAttack> buildCodec(RecordCodecBuilder.Instance<BerryBushAttack> instance) {
            return attackDefault(instance, BerryBushAttack::new);
        }
    }
}
