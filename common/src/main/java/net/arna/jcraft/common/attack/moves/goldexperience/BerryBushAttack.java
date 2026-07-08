package net.arna.jcraft.common.attack.moves.goldexperience;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public final class BerryBushAttack<A extends IAttacker<? extends A, ?>> extends AbstractSimpleAttack<BerryBushAttack<A>, A> {
    private static final BlockState BERRY_BUSH = Blocks.SWEET_BERRY_BUSH.defaultBlockState().setValue(SweetBerryBushBlock.AGE, 1);

    public BerryBushAttack(final int cooldown, final int windup, final int duration, final float moveDistance, final float damage, final int stun,
                           final float hitboxSize, final float knockback, final float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
    }

    @Override
    public @NotNull MoveType<BerryBushAttack<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final A attacker, final LivingEntity user) {
        final LivingEntity baseEntity = attacker.getBaseEntity();
        final Level world = baseEntity.level();
        final BlockPos blockPos = baseEntity.blockPosition();
        if (world.getBlockState(blockPos).isAir() && world.getBlockState(blockPos.below()).canOcclude() && world.getGameRules().getRule(JCraft.STAND_GRIEFING).get()) {
            world.setBlockAndUpdate(blockPos, BERRY_BUSH);
        }

        return super.perform(attacker, user);
    }

    @Override
    protected @NonNull BerryBushAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull BerryBushAttack<A> copy() {
        return copyExtras(new BerryBushAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(), getStun(),
                getHitboxSize(), getKnockback(), getOffset()));
    }

    public static class Type extends AbstractSimpleAttack.Type<BerryBushAttack<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NotNull App<RecordCodecBuilder.Mu<BerryBushAttack<?>>, BerryBushAttack<?>> buildCodec(RecordCodecBuilder.Instance<BerryBushAttack<?>> instance) {
            return attackDefault(instance, BerryBushAttack::new);
        }
    }
}
