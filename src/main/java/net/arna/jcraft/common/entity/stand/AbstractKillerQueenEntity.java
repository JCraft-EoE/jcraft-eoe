package net.arna.jcraft.common.entity.stand;

import it.unimi.dsi.fastutil.ints.IntSet;
import net.arna.jcraft.common.attack.core.BlockableType;
import net.arna.jcraft.common.attack.core.MoveMap;
import net.arna.jcraft.common.attack.core.MoveType;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.attack.moves.killerqueen.BombPlantAttack;
import net.arna.jcraft.common.attack.moves.killerqueen.DetonateAttack;
import net.arna.jcraft.common.attack.moves.killerqueen.ExplosiveDashAttack;
import net.arna.jcraft.common.attack.moves.shared.BarrageAttack;
import net.arna.jcraft.common.attack.moves.shared.SimpleAttack;
import net.arna.jcraft.common.attack.moves.shared.SimpleMultiHitAttack;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.registry.JSoundRegistry;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract sealed class AbstractKillerQueenEntity<E extends AbstractKillerQueenEntity<E, S>, S extends Enum<S> & StandAnimationState<E>> extends StandEntity<E, S>
        permits KillerQueenEntity, KQBTDEntity {
    public static final SimpleAttack<AbstractKillerQueenEntity<?, ?>> LOW = new SimpleAttack<AbstractKillerQueenEntity<?, ?>>(
            0, 8, 13, 0.85f, 4f, 10, 1.5f, 0.5f, 0.1f)
            .withImpactSound(JSoundRegistry.IMPACT_6)
            .withInfo(Text.literal("Low Punch"), Text.literal("frametrap tool, low stun"));
    public static final DetonateAttack DETONATE = new DetonateAttack(20, 5, 6, 1f)
            .withInfo(Text.literal("Detonate"), Text.literal("tiny windup, move queueing is disabled while Detonate is active"));
    public static final SimpleMultiHitAttack<AbstractKillerQueenEntity<?, ?>> LIGHT = SimpleMultiHitAttack.<AbstractKillerQueenEntity<?, ?>>lightAttack(
            19, 0.75f, 3f, 20, 0, IntSet.of(6, 11))
            .withImpactSound(JSoundRegistry.IMPACT_4)
            .withCrouchingVariant(DETONATE)
            .withFollowup(LOW)
            .withInfo(Text.literal("Dual Punch"), Text.literal("combo starter, decent speed, has followup with more blockstun"));
    public static final BarrageAttack<AbstractKillerQueenEntity<?, ?>> BARRAGE = new BarrageAttack<AbstractKillerQueenEntity<?, ?>>(
            240, 0, 50, 0.75f, 1f, 20, 2f, 0.1f, 0, 3)
            .withSound(JSoundRegistry.KQ_BARRAGE)
            .withImpactSound(JSoundRegistry.IMPACT_4)
            .withInfo(Text.literal("Barrage"), Text.literal("fast reliable combo starter/extender, medium stun"));
    public static final BombPlantAttack BOMB_PLANT = new BombPlantAttack(280, 12, 20, 1f, 9, 1.5f, 0f)
            .withBlockableType(BlockableType.NON_BLOCKABLE_EFFECTS_ONLY)
            .withBlockStun(8)
            .withInfo(Text.literal("Bomb Plant"), Text.literal("crouch to plant on the ground below you, stealthily"));
    public static final ExplosiveDashAttack EXPLOSIVE_DASH = new ExplosiveDashAttack(240)
            .withInfo(Text.literal("Explosive Dash"), Text.literal("instantly boosts the user in the aimed direction"));
    protected ItemEntity coin;

    protected AbstractKillerQueenEntity(StandType type, World worldIn, @Nullable SoundEvent summonSound) {
        super(type, worldIn, summonSound, true);
        idleRotation = -30f;

        description = "Explosive SETPLAY";

        pros = List.of(
                "good stun",
                "excellent setups",
                "easy knockdowns",
                "good zoning"
        );

        cons = List.of(
                "limited pressure tools",
                "below-average speed",
                "limited combo tools"
        );

        freespace = """
                BNBs:
                    -Standard bomb plant confirm and SHA setup
                    M1>Barrage>Bomb plant>Detonate(>Sheer Heart Attack)
                    
                    -Confirm while bomb plant is on cd
                    M1>Barrage>Heavy(>Sheer Heart Attack)""";
    }

    @Override
    protected void registerMoves(MoveMap<E, S> moves) {
        moves.register(MoveType.LIGHT, LIGHT, getLightState()).withCrouchingVariant(getDetonateState());
        moves.register(MoveType.BARRAGE, BARRAGE, getBarrageState());
        moves.register(MoveType.UTILITY, EXPLOSIVE_DASH); // No special state for this one.
    }

    public Vec3d getBombPos() {
        Entity bombEntity = moveContext.get(BombPlantAttack.BOMB_ENTITY);
        Vec3d bombPos = moveContext.get(BombPlantAttack.BOMB_POS);
        return bombEntity != null ? bombEntity.getPos() : bombPos;
    }

    protected void detonate() {
        setMove(DETONATE, getDetonateState());
        playSound(JSoundRegistry.KQ_DETONATE, 1, 1);
    }

    // Moveset
    @Override
    public void initMove(MoveType type) {
        if (!hasUser()) return;

        LivingEntity user = getUserOrThrow();
        if (user.hasStatusEffect(JStatusRegistry.DAZED)) return;

        if (type == MoveType.LIGHT) {
            boolean idling = getMoveStun() <= 0;
            if (curMove == null || curMove.getOriginalMove() != LIGHT) {
                if (idling) {
                    if (user.isSneaking()) detonate();
                    else super.initMove(MoveType.LIGHT);
                }
            } else if (getMoveStun() < 7) {
                if (user.isSneaking()) detonate();
                else setMove(LOW, getLowState());
            }
        } else super.initMove(type);
    }

    @Override
    public void desummon() {
        if (coin != null) coin.discard();
        super.desummon();
    }

    @Override
    public MoveSelectionResult specificMoveSelectionCriterion(AbstractMove<?, ? super E> attack, MobEntity mob, LivingEntity target, int stunTicks,
                                                              int enemyMoveStun, double distance, StandEntity<?, ?> enemyStand, AbstractMove<?, ?> enemyAttack) {
        if (enemyStand != null && enemyStand.blocking) return MoveSelectionResult.STOP;
        Vec3d bombPos = getBombPos();
        return bombPos != null && attack == DETONATE && target.squaredDistanceTo(bombPos) < 9.0D ?
                MoveSelectionResult.USE : MoveSelectionResult.PASS;
    }

    @Override
    public void tick() {
        super.tick();

        if (hasUser()) {
            BOMB_PLANT.tickBomb(this);
            if (getCurrentMove() instanceof DetonateAttack) queuedMove = null;
        }
    }

    // Animation code
    protected abstract S getDetonateState();
    protected abstract S getLightState();
    protected abstract S getLowState();
    protected abstract S getBarrageState();
}
