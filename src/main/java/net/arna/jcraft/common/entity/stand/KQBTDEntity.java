package net.arna.jcraft.common.entity.stand;

import it.unimi.dsi.fastutil.ints.IntSet;
import net.arna.jcraft.common.attack.core.BlockableType;
import net.arna.jcraft.common.attack.core.MoveMap;
import net.arna.jcraft.common.attack.core.MoveType;
import net.arna.jcraft.common.attack.core.StunType;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.attack.moves.killerqueen.BombPlantAttack;
import net.arna.jcraft.common.attack.moves.killerqueen.bitesthedust.*;
import net.arna.jcraft.common.attack.moves.shared.BarrageAttack;
import net.arna.jcraft.common.attack.moves.shared.GrabAttack;
import net.arna.jcraft.common.component.CooldownsComponent;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.util.CooldownType;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.registry.JSoundRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib3.core.builder.AnimationBuilder;

import java.util.List;
import java.util.function.Consumer;

public final class KQBTDEntity extends AbstractKillerQueenEntity<KQBTDEntity, KQBTDEntity.State> {
    public static final ElbowAttack ELBOW = new ElbowAttack(240, 5, 9, 0.75f,
            7.5f, 10, 1f, 1.1f, 0f)
            .withSound(JSoundRegistry.KQBTD_ELBOW)
            .withImpactSound(JSoundRegistry.IMPACT_4)
            .withInfo(Text.literal("Elbow"), Text.literal("fast, short-range knockback"));
    public static final BarrageAttack<KQBTDEntity> BARRAGE = new BarrageAttack<KQBTDEntity>(340, 0, 50,
            0.75f, 1f, 20, 1.5f, 0.1f, 0, 3)
            .withSound(JSoundRegistry.KQ_BARRAGE)
            .withImpactSound(JSoundRegistry.IMPACT_4)
            .withInfo(Text.literal("Barrage"), Text.literal("fast reliable combo starter/extender, medium stun"));
    public static final BubbleCounterAttack BUBBLE_COUNTER = new BubbleCounterAttack(540, 5, 20, 1f)
            .withInfo(Text.literal("Stray Cat Counter"), Text.literal("0.25s windup counter, turns opponent into your primary bomb"));
    public static final BubbleAttack BUBBLE = new BubbleAttack(460, 15, 18, 0.75f)
            .withCrouchingVariant(BUBBLE_COUNTER)
            .withSound(JSoundRegistry.KQ_UPPERCUT)
            .withInfo(Text.literal("Stray Cat Bubble"), Text.literal("launches an explosive bubble"));
    public static final BTDDetonateAttack BTD_DETONATE = new BTDDetonateAttack(20, 5, 6, 0.75f)
            .withSound(JSoundRegistry.KQ_DETONATE)
            .withInfo(Text.literal("Detonate"), Text.empty());
    public static final BTDPlantAttack BTD_PLANT = new BTDPlantAttack(1000, 14, 24, 1f, 10, 1.5f, 0f)
            .withBlockableType(BlockableType.NON_BLOCKABLE_EFFECTS_ONLY)
            .withBlockStun(8)
            .withInfo(Text.literal("Bites the Dust Plant"), Text.literal("press the same button to detonate, sending the affected enemy back to their previous location"));
    public static final BTDGrabHitAttack GRAB_HIT = new BTDGrabHitAttack(0, 42, 0.75f,
            5f, 15, 2f, 0f, 0.5f, IntSet.of(8, 22, 32))
            .withStunType(StunType.UNBURSTABLE)
            .withInfo(Text.literal("Takedown (hit)"), Text.empty());
    public static final GrabAttack<KQBTDEntity, State> GRAB = new GrabAttack<>(440, 12, 28,
            0.75f, 0f, 20, 1.75f, 0.1f, 0f, GRAB_HIT, State.GRAB_HIT)
            .withInfo(Text.literal("Takedown"), Text.literal("high damage grab"));

    public KQBTDEntity(World worldIn) {
        super(StandType.KILLER_QUEEN_BITES_THE_DUST, worldIn, JSoundRegistry.KQBTD_SUMMON);

        description = "Ascended Explosive SETPLAY";

        pros = List.of(
                "good stun",
                "excellent setups",
                "easy knockdowns and knockbacks",
                "good zoning"
        );

        cons = List.of(
                "limited pressure tools",
                "no armored moves"
        );

        freespace = """
                    BNBs:
                    the kitty cat
                    M1~Low>Barrage>Bomb Plant/Bites the Dust Plant
                    
                    the ol razzle dazzle
                    (Already bomb planted) M1~Low>Barrage>M1>Elbow>Detonate""";

        super.initialize();
    }

    @Override
    protected void registerMoves(MoveMap<KQBTDEntity, State> moves) {
        super.registerMoves(moves);

        // Light, barrage and util are registered by the super class.
        moves.register(MoveType.HEAVY, ELBOW, State.HEAVY);
        moves.register(MoveType.SPECIAL1, BOMB_PLANT, State.BOMB_PLANT);
        moves.register(MoveType.SPECIAL2, BUBBLE, State.BUBBLE);
        moves.register(MoveType.SPECIAL3, GRAB, State.GRAB);
        moves.register(MoveType.ULT, BTD_PLANT, State.BTD_PLANT);
    }

    @Override
    public void initMove(MoveType type) {
        switch (type) {
            case SPECIAL1 -> {
                LivingEntity user = getUserOrThrow();
                CooldownsComponent cooldowns = JComponents.getCooldowns(user);

                if (user.isInSneakingPose() && cooldowns.getCooldown(CooldownType.STAND_SP1) <= 0) {
                    Block downBlock = world.getBlockState(user.getBlockPos().down()).getBlock();
                    boolean notAir = (downBlock != Blocks.AIR && downBlock != Blocks.CAVE_AIR && downBlock != Blocks.VOID_AIR);
                    if (notAir) {
                        moveContext.set(BombPlantAttack.BOMB_ENTITY, null);
                        moveContext.set(BombPlantAttack.BOMB_POS, user.getPos().add(0, -0.5, 0));
                        cooldowns.setCooldown(CooldownType.STAND_SP1, BOMB_PLANT.getCooldown());
                    }
                } else {
                    handleMove(MoveType.SPECIAL1);
                    moveContext.set(BombPlantAttack.BOMB_POS, null);
                }
            }
            case ULT -> {
                if (moveContext.get(BTDPlantAttack.BTD_ENTITY) != null)
                    handleMove(BTD_DETONATE, CooldownType.ULT, State.DETONATE);
                else handleMove(MoveType.ULT);
            }
            default -> super.initMove(type);
        }
    }

    @Override
    public MoveSelectionResult specificMoveSelectionCriterion(AbstractMove<?, ? super KQBTDEntity> attack, MobEntity mob,
                                                              LivingEntity target, int stunTicks, int enemyMoveStun,
                                                              double distance, StandEntity<?, ?> enemyStand, AbstractMove<?, ?> enemyAttack) {
        if (enemyStand != null && enemyStand.blocking) return MoveSelectionResult.STOP;

        Vec3d bombPos = this.getBombPos();
        if (attack == DETONATE && bombPos != null && target.squaredDistanceTo(bombPos) < 9.0D) {
            return MoveSelectionResult.USE;
        } else if (attack == BTD_PLANT && moveContext.get(BTDPlantAttack.BTD_ENTITY) != null) {
            return MoveSelectionResult.USE;
        }
        return MoveSelectionResult.PASS;
    }

    @Override
    public void tick() {
        super.tick();

        if (!hasUser() || world.isClient) return;

        BUBBLE.tickBubble(this);
        BTD_PLANT.tickBomb(this);

        /*
        if (userData != null && !userData.isEmpty()) {
            if (ticksDataStored++ > 400) {
                ticksDataStored = 0;
                userData = null;
                targetData = null;
            }
        }
         */
    }

    // Animations
    public enum State implements StandAnimationState<KQBTDEntity> {
        IDLE(builder -> builder.loop("animation.kqbtd.idle")),
        LIGHT(builder -> builder.playAndHold("animation.kqbtd.light")),
        BLOCK(builder -> builder.loop("animation.kqbtd.block")),
        HEAVY(builder -> builder.playAndHold("animation.kqbtd.heavy")),
        BARRAGE(builder -> builder.loop("animation.kqbtd.barrage")),
        DETONATE(builder -> builder.playAndHold("animation.kqbtd.detonate")),
        BOMB_PLANT(builder -> builder.playAndHold("animation.kqbtd.bombplant")),
        BUBBLE(builder -> builder.playAndHold("animation.kqbtd.bubble")),
        LOW(builder -> builder.playAndHold("animation.kqbtd.low")),
        BUBBLE_COUNTER(builder -> builder.playAndHold("animation.kqbtd.bubblecounter")),
        COUNTER_MISS(builder -> builder.playAndHold("animation.kqbtd.counter_miss")),
        BTD_PLANT(builder -> builder.playAndHold("animation.kqbtd.btdplant")),
        GRAB(builder -> builder.playAndHold("animation.kqbtd.grab")),
        GRAB_HIT(builder -> builder.playAndHold("animation.kqbtd.grab_hit"));

        private final Consumer<AnimationBuilder> animator;

        State(Consumer<AnimationBuilder> animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(KQBTDEntity stand, AnimationBuilder builder) {
            animator.accept(builder);
        }
    }

    @Override
    protected State[] getStateValues() {
        return State.values();
    }

    @Override
    protected @NotNull String getSummonAnimation() {
        return "animation.kqbtd.summon";
    }

    @Override
    public State getBlockState() {
        return State.BLOCK;
    }

    @Override
    protected State getLightState() {
        return State.LIGHT;
    }

    @Override
    protected State getLowState() {
        return State.LOW;
    }

    @Override
    protected State getBarrageState() {
        return State.BARRAGE;
    }

    @Override
    protected State getDetonateState() {
        return State.DETONATE;
    }
}
