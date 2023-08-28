package net.arna.jcraft.common.entity.stand;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.BlockableType;
import net.arna.jcraft.common.attack.core.HitBoxData;
import net.arna.jcraft.common.attack.core.MoveMap;
import net.arna.jcraft.common.attack.core.MoveType;
import net.arna.jcraft.common.attack.core.MoveQueue;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.attack.moves.goldexperience.*;
import net.arna.jcraft.common.attack.moves.shared.BarrageAttack;
import net.arna.jcraft.common.attack.moves.shared.HealMove;
import net.arna.jcraft.common.attack.moves.shared.SimpleAttack;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.registry.JSoundRegistry;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.builder.AnimationBuilder;

import java.util.List;
import java.util.function.Consumer;

public class GoldExperienceEntity extends StandEntity<GoldExperienceEntity, GoldExperienceEntity.State> {
    // JCraft.lightCooldown -> 0 | 0.5f -> 0.35f
    public static final BerryBushAttack BERRY_BUSH = new BerryBushAttack(120, 16, 20,
            1.25f, 4f, 5, 1.5f, 0.75f, 0.2f)
            .withImpactSound(JSoundRegistry.IMPACT_4)
            .withInfo(Text.literal("Place Berry Bush"), Text.literal("places an almost-ripe berry bush on the ground, this move cannot be aimed up or down"));
    public static final SimpleAttack<GoldExperienceEntity> LIGHT = new SimpleAttack<GoldExperienceEntity>(
            15, 6, 9, 0.75f, 5f, 7, 1.5f, 0.75f, -0.1f)
            .withCrouchingVariant(BERRY_BUSH)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withInfo(Text.literal("Punch"), Text.literal("quick combo starter"));
    public static final SimpleAttack<GoldExperienceEntity> HEAVY = new SimpleAttack<GoldExperienceEntity>(
            280, 13, 22, 1f, 9f, 10, 1.5f, 1.5f, 0f)
            .withExtraHitBox(new HitBoxData(0, 0, 1.25))
//            .withSound(JSoundRegistry.GE_HEAVY)
            .withImpactSound(JSoundRegistry.IMPACT_2)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withHyperArmor()
            .withLaunch()
            .withInfo(Text.literal("Shoulder Smash"), Text.literal("slow, uninterruptible combo finisher"));
    public static BarrageAttack<GoldExperienceEntity> BARRAGE = new BarrageAttack<GoldExperienceEntity>(
            280, 0, 30, 0.75f, 1f, 30, 2f, 0.25f, 0f, 3)
            .withSound(JSoundRegistry.GE_BARRAGE)
            .withInfo(Text.literal("Barrage"), Text.literal("fast reliable combo starter/extender, high stun"));
    public static HealMove<GoldExperienceEntity> HEAL_OTHERS = new HealMove<GoldExperienceEntity>(520, 10,
            16, 1f, 1.25f,
            0f, 4f, HealMove.HealTarget.TARGETS)
            .withSound(JSoundRegistry.GE_HEAL)
            .withInfo(Text.literal("Healing Hand (others)"), Text.empty());
    public static HealMove<GoldExperienceEntity> HEAL_SELF = new HealMove<GoldExperienceEntity>(520, 10,
            14, 1f, 0,
            0, 4f, HealMove.HealTarget.USER)
            .withCrouchingVariant(HEAL_OTHERS)
            .withSound(JSoundRegistry.GE_HEAL)
            .withInfo(Text.literal("Healing Hand"), Text.literal("standing: heals user for 2 hearts, crouching: heals others for 2 hearts, pacifies angered mobs"));
    public static final TreeAttack TREE = new TreeAttack(100, 14, 24, 1f, 5f,
            15, 1.75f, 0.2f, -0.1f)
            .withImpactSound(JSoundRegistry.IMPACT_8)
            .withSound(JSoundRegistry.GE_TREE)
            .withInfo(Text.literal("Tree Summon"), Text.literal("two-hitting launch"));
    public static final LifeGiverAttack LIFE_GIVER = new LifeGiverAttack(720, 16, 25, 1f)
            .withSound(JSoundRegistry.GE_HEAL)
            .withInfo(Text.literal("Life Giver"), Text.literal("""
                            STANDING: turns any stackable item into a snake, lasts for 25s and stuns for 0.5s on hit
                            CROUCHING: turns any stackable item into a frog, lasts for 15s and reflects damage, follows user
                            AERIAL: turns any item into a butterfly, lasts forever"""));
    public static final OverclockAttack OVERCLOCK = new OverclockAttack(920, 22, 31, 1f,
            9f, 60, 2f, 0.9f, 0f)
//            .withSound(JSoundRegistry.GE_ULT)
            .withImpactSound(JSoundRegistry.IMPACT_10)
            .withBlockableType(BlockableType.NON_BLOCKABLE)
            .withInfo(Text.literal("Overclock"), Text.literal("slow, unblockable, devastating stun"));
    public static final RekkaAttack REKKA3 = new RekkaAttack(560, 12, 24, 1f, 7f,
            15, 2f, 0.5f, 0f, 3, 0, null, null)
            .withSound(JSoundRegistry.GE_REKKA3)
            .withImpactSound(JSoundRegistry.TW_KICK_HIT)
            .withInfo(Text.literal("Rekka (Final Hit)"), Text.literal("knockdown"));
    public static final RekkaAttack REKKA2 = new RekkaAttack(560, 10, 18, 1f, 5f,
            15, 1.75f, 0.5f, 0f, 2, 8, REKKA3, State.REKKA3)
            .withSound(JSoundRegistry.GE_REKKA2)
            .withImpactSound(JSoundRegistry.IMPACT_2)
            .withFollowUp(REKKA3)
            .withInfo(Text.literal("Rekka (2nd Hit)"), Text.literal("links into Light"));
    public static final RekkaAttack REKKA1 = new RekkaAttack(560, 8, 20, 1f, 5f,
            15, 1.5f, 0.5f, 0f, 1, 12, REKKA2, State.REKKA2)
            .withSound(JSoundRegistry.GE_REKKA1)
            .withImpactSound(JSoundRegistry.IMPACT_2)
            .withFollowUp(REKKA2)
            .withExtraHitBox(1.25)
            .withInfo(Text.literal("Rekka Series"), Text.literal("a set of three attacks, which cancel into each other during recovery"));

    public GoldExperienceEntity(World worldIn) {
        super(StandType.GOLD_EXPERIENCE, worldIn, JSoundRegistry.GE_SUMMON);

        idleRotation = 0f;

        description = "Impenetrable Regenerative DEFENSE";

        pros = List.of(
                "good pressure",
                "above average speed",
                "excellent defense (tree, heal, snake, heavy)",
                "excellent setups"
        );

        cons = List.of(
                "requires setup to become threatening",
                "no horizontal movement tools",
                "snake is unreliable"
        );

        freespace = """
                BNBs:
                    -the giogio
                    M1>Barrage>M1>Tree>Rekka 1~2~3
                    
                    -the superprince of gaming
                    Rekka 1~2>M1>Barrage>M1>Tree>Heavy""";
    }

    @Override
    protected void registerMoves(MoveMap<GoldExperienceEntity, State> moves) {
        moves.register(MoveType.LIGHT, LIGHT, State.LIGHT).withCrouchingVariant(State.LIFE_GIVER);
        moves.register(MoveType.HEAVY, HEAVY, State.HEAVY);
        moves.register(MoveType.BARRAGE, BARRAGE, State.BARRAGE);

        moves.register(MoveType.SPECIAL1, HEAL_SELF).withCrouchingVariant(State.HEAL);
        moves.register(MoveType.SPECIAL2, REKKA1, State.REKKA1);
        moves.register(MoveType.SPECIAL3, LIFE_GIVER, State.LIFE_GIVER);
        moves.register(MoveType.ULTIMATE, OVERCLOCK, State.OVERCLOCK);

        moves.register(MoveType.UTILITY, TREE, State.TREE);
    }

    // Moveset
    @Override
    public void initMove(MoveType type) {
        switch (type) {
            case SPECIAL2 -> {
                if (!hasUser()) return;
                LivingEntity user = getUserOrThrow();
                if (user.hasStatusEffect(JStatusRegistry.DAZED)) return;
                boolean idling = this.getMoveStun() <= 0;
                if (!(curMove instanceof RekkaAttack rekka)) {
                    if (idling) handleMove(MoveType.SPECIAL2);
                } else if (rekka.getNext() != null && rekka.mayAdvance(this))
                    setMove(rekka.getNext(), rekka.getNextState());
            }
            case SPECIAL3 -> {
                if (!canAttack() || !hasUser()) return;
                LivingEntity user = getUserOrThrow();

                LifeGiverAttack.LifeGiverType toSummon = LifeGiverAttack.LifeGiverType.SNAKE;
                if (user.isOnGround()) {
                    if (user.isSneaking()) toSummon = LifeGiverAttack.LifeGiverType.FROG;
                } else toSummon = LifeGiverAttack.LifeGiverType.BUTTERFLY;
                moveContext.set(LifeGiverAttack.TYPE_TO_SUMMON, toSummon);

                handleMove(MoveType.SPECIAL3);
            }
            default -> super.initMove(type);
        }
    }

    /*
    @Override
    public boolean allowUtilityUse() { // Disables using the utility while sneaking, allowing menu control
        if (getUser().isSneaking()) return false;
        return super.allowUtilityUse();
    }
    @Environment(EnvType.CLIENT)
    boolean inMenu = false;
    @Override
    public void initClientUtility() {
        inMenu = true;
    }
     */

    @Override
    public MoveSelectionResult specificMoveSelectionCriterion(AbstractMove<?, ? super GoldExperienceEntity> attack,
                                                              MobEntity mob, LivingEntity target, int stunTicks,
                                                              int enemyMoveStun, double distance,
                                                              StandEntity<?, ?> enemyStand, AbstractMove<?, ?> enemyAttack) {
        return attack == LIFE_GIVER ?
                mob.getMainHandStack().isEmpty() && mob.getOffHandStack().isEmpty() ?
                        MoveSelectionResult.STOP : MoveSelectionResult.USE :
                MoveSelectionResult.PASS;
    }

    @Override
    public boolean shouldOffsetHeight() {
        if (getState() == State.LIFE_GIVER) return false;
        return super.shouldOffsetHeight();
    }

    @Override
    public void tick() {
        super.tick();
        if (!hasUser()) return;

        if (!world.isClient && curMove == REKKA2 && queuedAttack == MoveQueue.SPECIAL2)
            queuedAttack = null;
    }

    @Override
    protected @NonNull GoldExperienceEntity getThis() {
        return this;
    }

    // Animation code
    public enum State implements StandAnimationState<GoldExperienceEntity> {
        IDLE(builder -> builder.loop("animation.ge.idle")),
        LIGHT(builder -> builder.playAndHold("animation.ge.light")),
        BLOCK(builder -> builder.loop("animation.ge.block")),
        HEAVY(builder -> builder.playAndHold("animation.ge.heavy")),
        BARRAGE(builder -> builder.loop("animation.ge.barrage")),
        HEAL_SELF(builder -> builder.playAndHold("animation.ge.healself")),
        HEAL(builder -> builder.playAndHold("animation.ge.heal")),
        TREE(builder -> builder.playAndHold("animation.ge.tree")),
        LIFE_GIVER(builder -> builder.playAndHold("animation.ge.lifegiver")),
        REKKA1(builder -> builder.playAndHold("animation.ge.rekka1")),
        REKKA2(builder -> builder.playAndHold("animation.ge.rekka2")),
        REKKA3(builder -> builder.playAndHold("animation.ge.rekka3")),
        OVERCLOCK(builder -> builder.playAndHold("animation.ge.overclock"));

        private final Consumer<AnimationBuilder> animator;

        State(Consumer<AnimationBuilder> animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(GoldExperienceEntity attacker, AnimationBuilder builder) {
            animator.accept(builder);
        }
    }

    @Override
    protected State[] getStateValues() {
        return State.values();
    }

    @Nullable
    @Override
    protected String getSummonAnimation() {
        return "animation.ge.summon";
    }

    @Override
    public State getBlockState() {
        return State.BLOCK;
    }
}
