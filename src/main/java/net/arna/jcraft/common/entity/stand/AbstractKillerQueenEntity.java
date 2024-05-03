package net.arna.jcraft.common.entity.stand;

import net.arna.jcraft.common.attack.core.BlockableType;
import net.arna.jcraft.common.attack.core.MoveMap;
import net.arna.jcraft.common.attack.core.MoveType;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.attack.moves.killerqueen.BombPlantAttack;
import net.arna.jcraft.common.attack.moves.killerqueen.DetonateAttack;
import net.arna.jcraft.common.attack.moves.killerqueen.ExplosiveDashAttack;
import net.arna.jcraft.common.attack.moves.shared.MainBarrageAttack;
import net.arna.jcraft.common.attack.moves.shared.SimpleAttack;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.component.living.CooldownsComponent;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.util.CooldownType;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.registry.JSoundRegistry;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract sealed class AbstractKillerQueenEntity<E extends AbstractKillerQueenEntity<E, S>, S extends Enum<S> & StandAnimationState<E>> extends StandEntity<E, S>
        permits KillerQueenEntity, KQBTDEntity {
    public static final SimpleAttack<AbstractKillerQueenEntity<?, ?>> LOW = new SimpleAttack<AbstractKillerQueenEntity<?, ?>>(
            0, 8, 13, 0.85f, 4f, 10, 1.5f, 0.25f, 0.1f)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withInfo(
                    Text.literal("Low Punch"),
                    Text.literal("frametrap tool, low stun")
            );
    public static final SimpleAttack<AbstractKillerQueenEntity<?, ?>> LIGHT_FOLLOWUP = new SimpleAttack<AbstractKillerQueenEntity<?, ?>>(
            0, 6, 13, 0.8f, 3f, 20, 1.5f, 0.5f, 0.1f)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            // implemented in class: .withFollowup(LOW)
            .withInfo(
                    Text.literal("Second Punch"),
                    Text.literal("frametrap tool")
            );
    public static final DetonateAttack DETONATE = new DetonateAttack(20, 5, 6, 1f)
            .withInfo(
                    Text.literal("Detonate"),
                    Text.literal("tiny windup, move queueing is disabled while Detonate is active")
            );
    public static final SimpleAttack<AbstractKillerQueenEntity<?, ?>> LIGHT = new SimpleAttack<AbstractKillerQueenEntity<?, ?>>(
            30, 6, 10, 0.75f, 3f, 10, 1.5f, 0.25f, 0.1f)
            .withImpactSound(JSoundRegistry.IMPACT_6)
            .withCrouchingVariant(DETONATE)
            // implemented in class: .withFollowup(LIGHT_FOLLOWUP)
            .withInfo(
                    Text.literal("Punch"),
                    Text.literal("combo starter, decent speed, has two followups")
            );
    public static final MainBarrageAttack<AbstractKillerQueenEntity<?, ?>> BARRAGE = new MainBarrageAttack<AbstractKillerQueenEntity<?, ?>>(
            240, 0, 40, 0.75f, 1f, 20, 2f, 0.1f, 0, 3, Blocks.DEEPSLATE.getHardness())
            .withSound(JSoundRegistry.KQ_BARRAGE)
            .withImpactSound(JSoundRegistry.IMPACT_4)
            .withInfo(
                    Text.literal("Barrage"),
                    Text.literal("fast reliable combo starter/extender, medium stun")
            );
    public static final BombPlantAttack BOMB_PLANT = new BombPlantAttack(280, 12, 20, 1f, 9, 1.5f, 0f)
            .withBlockableType(BlockableType.NON_BLOCKABLE_EFFECTS_ONLY)
            .withBlockStun(8)
            .withInfo(
                    Text.literal("Bomb Plant"),
                    Text.literal("crouch to plant on the ground below you, stealthily")
            );
    public static final ExplosiveDashAttack EXPLOSIVE_DASH = new ExplosiveDashAttack(240)
            .withInfo(
                    Text.literal("Explosive Dash"),
                    Text.literal("instantly boosts the user in the aimed direction")
            );
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
                "interactive offense",
                "below-average speed",
                "limited combo tools"
        );

        freespace = """
                BNBs:
                    -Standard bomb plant confirm and SHA setup
                    M1~M1>Barrage>Bomb plant>Detonate(>Sheer Heart Attack)
                    
                    -Confirm while bomb plant is on cd
                    M1~M1>Barrage>Heavy(>Sheer Heart Attack)""";
    }

    @Override
    protected void registerMoves(MoveMap<E, S> moves) {
        moves.register(MoveType.BARRAGE, BARRAGE, getBarrageState());
        moves.register(MoveType.UTILITY, EXPLOSIVE_DASH); // No special state for this one.
    }

    protected void detonate() {
        setMove(DETONATE, getDetonateState());
        playSound(JSoundRegistry.KQ_DETONATE, 1, 1);
    }

    // Moveset
    @Override
    public boolean initMove(MoveType type) {
        if (!hasUser()) return false;

        LivingEntity user = getUserOrThrow();
        if (user.hasStatusEffect(JStatusRegistry.DAZED)) return false;

        switch (type) {
            case LIGHT -> {
                boolean idling = getMoveStun() <= 0;
                if (curMove == null || curMove.getFollowup() == null) {
                    if (idling) {
                        if (user.isSneaking()) detonate();
                        else return super.initMove(MoveType.LIGHT);
                    }
                } else if (getMoveStun() < curMove.getWindupPoint()) {
                    if (user.isSneaking()) detonate();
                    else {
                        AbstractMove<?, ? super E> followup = curMove.getFollowup();
                        setMove(followup, (S) followup.getAnimation());
                    }
                }

                return true;
            }

            case SPECIAL1 -> {
                CooldownsComponent cooldowns = JComponents.getCooldowns(user);

                if (user.isInSneakingPose() && cooldowns.getCooldown(CooldownType.STAND_SP1) <= 0) {
                    BlockPos standingOn = user.getBlockPos().offset(GravityChangerAPI.getGravityDirection(user));
                    if (!getWorld().getBlockState(standingOn).isAir()) {
                        JComponents.getBombTracker(user).getMainBomb().setBomb(standingOn);
                        cooldowns.setCooldown(CooldownType.STAND_SP1, BOMB_PLANT.getCooldown());
                    }

                    return true;
                } else return handleMove(MoveType.SPECIAL1);
            }

            default -> {
                return super.initMove(type);
            }
        }
    }

    @Override
    public void desummon() {
        if (coin != null) coin.discard();
        super.desummon();
    }

    @Override
    public MoveSelectionResult specificMoveSelectionCriterion(AbstractMove<?, ? super E> attack, LivingEntity mob, LivingEntity target, int stunTicks,
                                                              int enemyMoveStun, double distance, StandEntity<?, ?> enemyStand, AbstractMove<?, ?> enemyAttack) {
        if (enemyStand != null && enemyStand.blocking) return MoveSelectionResult.STOP;
        Vec3d bombPos = JComponents.getBombTracker(mob).getMainBomb().getBombPos();
        return bombPos != null && attack == DETONATE && target.squaredDistanceTo(bombPos) < 9.0D ?
                MoveSelectionResult.USE : MoveSelectionResult.PASS;
    }

    @Override
    public void tick() {
        super.tick();

        if (hasUser())
            if (getCurrentMove() instanceof DetonateAttack) queuedMove = null;
    }

    // Animation code
    protected abstract S getDetonateState();
    protected abstract S getLightState();
    protected abstract S getBarrageState();
}
