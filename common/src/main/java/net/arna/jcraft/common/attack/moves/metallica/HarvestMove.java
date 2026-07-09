package net.arna.jcraft.common.attack.moves.metallica;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.MoveSelectionResult;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.enums.MoveInputType;
import net.arna.jcraft.api.attack.moves.AbstractBarrageAttack;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.registry.JTagRegistry;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.common.entity.stand.MetallicaEntity;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.Optional;
import java.util.Set;

import static net.arna.jcraft.common.entity.stand.MetallicaEntity.SIPHON_POS;

public class HarvestMove extends AbstractBarrageAttack<HarvestMove, MetallicaEntity> {
    public HarvestMove(int duration, float moveDistance, int interval) {
        super(0, 0, duration, moveDistance, 0, 0, 0, 0, 0, interval);
        withHoldable();
    }

    @Override
    public @NonNull MoveType<HarvestMove> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public void onInitiate(MetallicaEntity attacker) {
        super.onInitiate(attacker);
        attacker.getEntityData().set(SIPHON_POS, Optional.empty());
    }

    @Override
    public void onUserMoveInput(final MetallicaEntity attacker, final MoveInputType type, final boolean pressed,final  boolean moveInitiated) {
        super.onUserMoveInput(attacker, type, pressed, moveInitiated);
        // Must be held
        if (type.getMoveClass() == getMoveClass() && !pressed) attacker.cancelMove();
    }

    @Override
    public void activeTick(MetallicaEntity attacker, int moveStun) {
        super.activeTick(attacker, moveStun);

        LivingEntity user = attacker.getUser();

        if (user instanceof Mob mob) { // AI iron harvest
            final float iron = attacker.getIron();
            if (iron >= MetallicaEntity.IRON_MAX || (iron >= (MetallicaEntity.IRON_MAX / 3.0f) && mob.getRandom().nextFloat() > 0.1f))
                // Cancel automatically if iron is full
                attacker.cancelMove();
        }
    }

    @Override
    public @NonNull Set<LivingEntity> perform(MetallicaEntity attacker, LivingEntity user) {
        // final Set<LivingEntity> targets = super.perform(attacker, user);

        if (user instanceof Mob mob) {
            final boolean noBlock = mob.getBlockStateOn().isAir();
            final BlockPos hitPos = mob.getOnPos();

            attacker.getEntityData().set(SIPHON_POS, noBlock ? Optional.empty() : Optional.of(hitPos));
            if (noBlock) return Set.of();

            // Add iron
            float gain = user.level().getBlockState(hitPos).is(JTagRegistry.IRON_BLOCKS) ? 3f : 1.5f;
            if (attacker.getEntityData().get(MetallicaEntity.INVISIBLE)) gain /= 2.0f;

            attacker.addIron(gain);
            return Set.of();
        }

        final BlockHitResult hitResult = JUtils.genericBlockRaycast(user.level(), user, 5, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE);

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            final BlockPos hitPos = hitResult.getBlockPos();
            attacker.getEntityData().set(SIPHON_POS, Optional.of(hitPos));

            // Add iron
            float gain = user.level().getBlockState(hitPos).is(JTagRegistry.IRON_BLOCKS) ? 3f : 1.5f;
            if (attacker.getEntityData().get(MetallicaEntity.INVISIBLE)) gain /= 2.0f;

            attacker.addIron(gain);
        } else {
            attacker.getEntityData().set(SIPHON_POS, Optional.empty());
        }

        return Set.of();
    }

    @Override
    public MoveSelectionResult specificMoveSelectionCriterion(MetallicaEntity attacker, LivingEntity mob,
                                                                                  LivingEntity target, int stunTicks,
                                                                                  int enemyMoveStun, double distance, StandEntity<?, ?> enemyStand,
                                                                                  AbstractMove<?, ?> enemyAttack) {
        final float iron = attacker.getIron();
        if (iron < (MetallicaEntity.IRON_MAX / 3.0f) && mob.getRandom().nextFloat() > 0.1f) return MoveSelectionResult.USE;
        return iron >= MetallicaEntity.IRON_MAX ? MoveSelectionResult.STOP : MoveSelectionResult.PASS;
    }

    @Override
    protected @NonNull HarvestMove getThis() {
        return this;
    }

    @Override
    public @NonNull HarvestMove copy() {
        return copyExtras(new HarvestMove(getDuration(), getMoveDistance(), getInterval()));
    }

    public static class Type extends AbstractBarrageAttack.Type<HarvestMove> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<HarvestMove>, HarvestMove> buildCodec(RecordCodecBuilder.Instance<HarvestMove> instance) {
            return instance.group(extras(), attackExtras(), duration(), moveDistance(), interval())
                    .apply(instance, applyAttackExtras(HarvestMove::new));
        }
    }
}
