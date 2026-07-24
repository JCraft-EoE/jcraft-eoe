package net.arna.jcraft.common.entity.stand;

import lombok.NonNull;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.Attacks;
import net.arna.jcraft.api.MoveSelectionResult;
import net.arna.jcraft.api.attack.MoveMap;
import net.arna.jcraft.api.attack.MoveSet;
import net.arna.jcraft.api.attack.MoveSetManager;
import net.arna.jcraft.api.attack.core.HitBoxData;
import net.arna.jcraft.api.attack.enums.BlockableType;
import net.arna.jcraft.api.attack.enums.MoveClass;
import net.arna.jcraft.api.attack.enums.MoveInputType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.arna.jcraft.api.stand.StandData;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.api.stand.StandInfo;
import net.arna.jcraft.api.stand.SummonData;
import net.arna.jcraft.common.attack.actions.HealAction;
import net.arna.jcraft.common.attack.actions.LaunchUpAction;
import net.arna.jcraft.common.attack.moves.goldexperience.*;
import net.arna.jcraft.common.attack.moves.shared.*;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.StandAnimationState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.joml.Vector3f;

/**
 * The {@link StandEntity} for <a href="https://jojowiki.com/Gold_Experience">Gold Experience</a>.
 * @see JStandTypeRegistry#GOLD_EXPERIENCE
 * @see net.arna.jcraft.client.renderer.entity.stands.GoldExperienceRenderer GoldExperienceRenderer
 * @see BerryBushAttack
 * @see LifeGiverAttack
 * @see OverclockAttack
 * @see TreeAttack
 */
public class GoldExperienceEntity extends StandEntity<GoldExperienceEntity, GoldExperienceEntity.State> {
    public static final MoveSet<GoldExperienceEntity, State> MOVE_SET = MoveSetManager.create(JStandTypeRegistry.GOLD_EXPERIENCE,
            GoldExperienceEntity::registerMoves, GoldExperienceEntity.class, State.class);
    public static final StandData DATA = StandData.builder()
            .idleRotation(-30f)
            .info(StandInfo.builder()
                    .name(Component.translatable("entity.jcraft.goldexperience"))
                    .proCount(4)
                    .conCount(3)
                    .freeSpace(Component.literal("""
                BNBs:
                    -the giogio
                    Light>Barrage>Light>Tree>Rekka 1~2~3
                
                    -the superprince of gaming
                    Rekka 1~2>Light>Barrage>Light>Tree>Heavy"""))
                    .skinName(Component.literal("Anime"))
                    .skinName(Component.literal("Spectre"))
                    .skinName(Component.literal("Burning Passion"))
                    .build())
            .summonData(SummonData.of(JSoundRegistry.GE_SUMMON))
            .build();

    // JCraft.lightCooldown -> 0 | 0.5f -> 0.35f
    public static final BerryBushAttack<GoldExperienceEntity> BERRY_BUSH = new BerryBushAttack<GoldExperienceEntity>(40,
            16, 20, 1.25f, 4f, 5, 1.5f, 0.75f, 0.2f)
            .withAnim(State.LIFE_GIVER)
            .withImpactSound(JSoundRegistry.IMPACT_4)
            .withInfo(
                    Component.literal("Place Berry Bush"),
                    Component.literal("Places an almost-ripe berry bush on the ground.")
            );
    public static final SimpleAttack<GoldExperienceEntity> LIGHT_FOLLOWUP = new SimpleAttack<GoldExperienceEntity>(0,
            7, 12, 0.75f, 6, 7, 1.5f, 1f, -0.1f)
            .withAnim(State.LIGHT_FOLLOWUP)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withLaunch()
            .withBlockStun(4)
            .withExtraHitBox(0, 0.25, 1)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Component.literal("Punch"),
                    Component.literal("Quick combo finisher.")
            );
    public static final SimpleAttack<GoldExperienceEntity> LIGHT = new SimpleAttack<GoldExperienceEntity>(15,
            6, 9, 0.75f, 5f, 7, 1.5f, 0.2f, -0.1f)
            .noLoopPrevention()
            .withFollowup(LIGHT_FOLLOWUP)
            .withCrouchingVariant(BERRY_BUSH)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withInfo(
                    Component.literal("Punch"),
                    Component.literal("Quick combo starter, low stun.")
            );
    public static final VineAttack<GoldExperienceEntity> UNDERHAND = new VineAttack<GoldExperienceEntity>(100,
            13, 22, 1.5f, 7f, 10, 0.75f, 1.25f, 0f)
            .withAnim(State.UNDERHAND)
            .withSound(JSoundRegistry.GE_TREE)
            .withImpactSound(JSoundRegistry.IMPACT_2)
            .withHitSpark(JParticleType.HIT_SPARK_3)
            .withLaunch()
            .withInfo(
                    Component.literal("Underhand Carve"),
                    Component.literal("An underhand launching strike, which creates aggressive vines if used near the ground.")
            );
    public static final MovementSlowingSimpleAttack<GoldExperienceEntity> HEAVY = new MovementSlowingSimpleAttack<GoldExperienceEntity>(0,
            13, 22, 1f, 9f, 10, 1.5f, 1.5f, 0f)
            .withExtraHitBox(new HitBoxData(0, 0, 1.25))
//            .withSound(JSoundRegistry.GE_HEAVY)
            .withImpactSound(JSoundRegistry.IMPACT_2)
            .withHitSpark(JParticleType.HIT_SPARK_3)
            .withHyperArmor()
            .withLaunch()
            .withCrouchingVariant(UNDERHAND)
            .withInfo(
                    Component.literal("Shoulder Smash"),
                    Component.literal("Slow, uninterruptible combo finisher.")
            );
    public static final MainBarrageAttack<GoldExperienceEntity> BARRAGE = new MainBarrageAttack<GoldExperienceEntity>(
            280, 0, 30, 0.75f, 1f, 20, 2f, 0.25f, 0f, 3, Blocks.OAK_PLANKS.defaultDestroyTime())
            .withSound(JSoundRegistry.GE_BARRAGE)
            .withInfo(
                    Component.literal("Barrage"),
                    Component.literal("Fast reliable combo starter/extender, high stun.")
            );
    public static final HealMove<GoldExperienceEntity> HEAL_OTHERS = new HealMove<GoldExperienceEntity>(500, 10,
            16, 1f, 1.25f, 0f, 4f, HealMove.HealTarget.TARGETS, false)
            .withSound(JSoundRegistry.GE_HEAL)
            .withInfo(
                    Component.literal("Healing Hand (others)"),
                    Component.literal("Heals others for 2 hearts, pacifies angered mobs.")
            );
    public static final HealMove<GoldExperienceEntity> HEAL_SELF = new HealMove<GoldExperienceEntity>(500, 10,
            14, 1f, 0, 0, 4f, HealMove.HealTarget.USER, false)
            .withCrouchingVariant(HEAL_OTHERS)
            .withSound(JSoundRegistry.GE_HEAL)
            .withInfo(
                    Component.literal("Healing Hand"),
                    Component.literal("Heals user for 2 hearts.")
            );
    public static final TreeAttack<GoldExperienceEntity> TREE = new TreeAttack<GoldExperienceEntity>(280, 10, 24, 1f, 5f,
            15, 1.75f, 0.2f, -0.1f)
            .withImpactSound(JSoundRegistry.IMPACT_8)
            .withSound(JSoundRegistry.GE_TREE)
            .withInfo(
                    Component.literal("Tree Summon"),
                    Component.literal("Ground strike, which creates a tree that launches those it hits in the direction it was summoned in.")
            );
    public static final LifeGiverAttack<GoldExperienceEntity> LIFE_GIVER = new LifeGiverAttack<GoldExperienceEntity>(300, 16, 25, 1f)
            .withSound(JSoundRegistry.GE_HEAL)
            .withInfo(
                    Component.literal("Life Giver"),
                    Component.literal("""
                            STANDING: Turns any stackable item into a snake, lasts for 25s and stuns for 0.5s on hit.
                            CROUCHING: Turns any stackable item into a frog, lasts for 15s and reflects damage, follows user.
                            AERIAL: Turns any item into a butterfly, lasts forever.""")
            );
    public static final OverclockAttack<GoldExperienceEntity> OVERCLOCK = new OverclockAttack<GoldExperienceEntity>(920,
            22, 31, 1f,9f, 60, 2f, 0.9f, 0f, 60)
//            .withSound(JSoundRegistry.GE_ULT)
            .withImpactSound(JSoundRegistry.TW_KICK_HIT)
            .withImpactSound(JSoundRegistry.IMPACT_10)
            .withBlockableType(BlockableType.NON_BLOCKABLE)
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.LAUNCH)
            .withLaunch()
            .withAction(LaunchUpAction.launchUp(0.8f))
            .withHitSpark(JParticleType.HIT_SPARK_3)
            .withInfo(
                    Component.literal("Overclock"),
                    Component.literal("Slow, unblockable, devastating stun. Launches the opponent's soul out of their body.")
            );

    public static final OverclockAttack<GoldExperienceEntity> SOUL_PUNCH = new OverclockAttack<GoldExperienceEntity>(100,
            14, 26, 1f,0f, 80, 1.65f, 0.9f, 0f, 80)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withImpactSound(JSoundRegistry.IMPACT_10)
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.CRUSH)
            .withAction(HealAction.heal(6.0f))
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Component.literal("Soul Punch"),
                    Component.literal("Fills the target with life energy, healing them and knocking their soul out of their body for 3 seconds. Extreme stun.")
            );
    public static final KnockdownAttack<GoldExperienceEntity> REKKA3 = new KnockdownAttack<GoldExperienceEntity>
            (0, 12, 24, 1f, 6f, 15, 2f, 0.75f, 0f, 50)
            .withAnim(State.REKKA3)
            .withSound(JSoundRegistry.GE_REKKA3)
            .withLaunch()
            .withImpactSound(JSoundRegistry.TW_KICK_HIT)
            .withBlockStun(8)
            .withInfo(
                    Component.literal("Rekka (Final Hit)"),
                    Component.literal("Knockdown, low blockstun.")
            );
    public static final SimpleAttack<GoldExperienceEntity> REKKA2 = new SimpleAttack<GoldExperienceEntity>
            (0, 9, 18, 1f, 5f, 16, 1.75f, 0.5f, 0f)
            .withAnim(State.REKKA2)
            .withSound(JSoundRegistry.GE_REKKA2)
            .withImpactSound(JSoundRegistry.IMPACT_2)
            .withFollowup(REKKA3)
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.HIGH)
            .withInfo(
                    Component.literal("Rekka (2nd Hit)"),
                    Component.literal("Links into Light.")
            );
    public static final SimpleAttack<GoldExperienceEntity> REKKA1 = new SimpleAttack<GoldExperienceEntity>
            (0, 7, 14, 1f, 5f, 15, 1.5f, 0.5f, 0f)
            .withCrouchingVariant(SOUL_PUNCH)
            .withAnim(State.REKKA1)
            .withSound(JSoundRegistry.GE_REKKA1)
            .withImpactSound(JSoundRegistry.IMPACT_2)
            .withFollowup(REKKA2)
            .withExtraHitBox(1.25)
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.CRUSH)
            .withInfo(
                    Component.literal("Rekka Series"),
                    Component.literal("A set of three attacks, which cancel into each other during recovery.")
            );
    // TODO add move info x2
    // TODO balance x2
    public static final TossMove<GoldExperienceEntity> TOSS = new TossMove<GoldExperienceEntity>(0, 1, 1, 0.75f,0.13f)
            .withAnim(GoldExperienceEntity.State.ITEM_TOSS);
    public static final TossChargeMove<GoldExperienceEntity> TOSS_CHARGE = new TossChargeMove<GoldExperienceEntity>(70, 1 * 20 + 1, 2 * 20, 1.0f, 10)
            .withFollowup(TOSS);

    public GoldExperienceEntity(Level worldIn) {
        super(JStandTypeRegistry.GOLD_EXPERIENCE.get(), worldIn);

        auraColors = new Vector3f[]{
                new Vector3f(1.0f, 0.7f, 0.2f),
                new Vector3f(0.3f, 0.6f, 1.0f),
                new Vector3f(1.0f, 0.3f, 0.7f),
                new Vector3f(1.0f, 0.0f, 0.0f)
        };
    }

    private static void registerMoves(MoveMap<GoldExperienceEntity, State> moves) {
        moves.registerImmediate(MoveClass.LIGHT, LIGHT, State.LIGHT);

        moves.registerImmediate(MoveClass.HEAVY, HEAVY, State.HEAVY);
        moves.register(MoveClass.BARRAGE, BARRAGE, State.BARRAGE);

        moves.register(MoveClass.SPECIAL1, HEAL_SELF, State.HEAL_SELF).withCrouchingVariant(State.HEAL);

        final var r1 = moves.register(MoveClass.SPECIAL2, REKKA1, State.REKKA1);
        r1.withCrouchingVariant(State.SOUL_PUNCH);
        r1.withFollowup(State.REKKA2).withFollowup(State.REKKA3);

        moves.register(MoveClass.SPECIAL3, LIFE_GIVER, State.LIFE_GIVER);
        moves.register(MoveClass.ULTIMATE, OVERCLOCK, State.OVERCLOCK);

        moves.register(MoveClass.UTILITY, TREE, State.TREE);

        moves.register(MoveClass.TOSS, TOSS_CHARGE, State.ITEM_TOSS_CHARGE).withFollowup(State.ITEM_TOSS);
    }

    // Moveset
    @Override
    public boolean initMove(MoveClass moveClass) {
        switch (moveClass) {
            case SPECIAL2 -> {
                final LivingEntity user = getUserOrThrow();
                if (user.hasEffect(JStatusRegistry.DAZED.get())) {
                    return false;
                }
                boolean idling = this.getMoveStun() <= 0;
                if (getCurrentMove() == null || getCurrentMove().getMoveClass() != MoveClass.SPECIAL2) {
                    if (idling) {
                        return handleMove(MoveClass.SPECIAL2);
                    } else {
                        return false;
                    }
                } else if (getCurrentMove().getFollowup() != null && getCurrentMove().hasWindupPassed(this)) {
                    setMove(getCurrentMove().getFollowup(), (State) getCurrentMove().getFollowup().getAnimation());
                }
            }
            case SPECIAL3 -> {
                if (!canAttack() || !hasUser()) {
                    return false;
                }
                final LivingEntity user = getUserOrThrow();

                LifeGiverAttack.LifeGiverType toSummon = LifeGiverAttack.LifeGiverType.SNAKE;
                if (user.onGround()) {
                    if (user.isShiftKeyDown()) {
                        toSummon = LifeGiverAttack.LifeGiverType.FROG;
                    }
                } else {
                    toSummon = LifeGiverAttack.LifeGiverType.BUTTERFLY;
                }
                final LifeGiverAttack.LifeGiverType finalToSummon = toSummon;
                getMoveMap().findMoveByType(LifeGiverAttack.class)
                        .ifPresent(move -> move.setTypeToSummon(finalToSummon));

                return handleMove(MoveClass.SPECIAL3);
            }
            case LIGHT -> {
                if (!tryFollowUp(moveClass, MoveClass.LIGHT)) {
                    return super.initMove(moveClass);
                }
            }
        }
        return super.initMove(moveClass);
    }

    @Override
    public void queueMove(MoveInputType type) {
        if ( (getState() == State.REKKA2 || getState() == State.REKKA3) && type == MoveInputType.SPECIAL2) return;

        super.queueMove(type);
    }

    @Override
    public MoveSelectionResult specificMoveSelectionCriterion(AbstractMove<?, ? super GoldExperienceEntity> attack,
                                                                                  LivingEntity mob, LivingEntity target, int stunTicks,
                                                                                  int enemyMoveStun, double distance,
                                                                                  StandEntity<?, ?> enemyStand, AbstractMove<?, ?> enemyAttack) {
        return attack == LIFE_GIVER ?
                mob.getMainHandItem().isEmpty() && mob.getOffhandItem().isEmpty() ?
                        MoveSelectionResult.STOP : MoveSelectionResult.USE :
                super.specificMoveSelectionCriterion(attack, mob, target, stunTicks, enemyMoveStun, distance, enemyStand, enemyAttack);
    }

    @Override
    public boolean shouldOffsetHeight() {
        if (getState() == State.LIFE_GIVER) {
            return false;
        }
        return super.shouldOffsetHeight();
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    @NonNull
    public GoldExperienceEntity getThis() {
        return this;
    }

    // Animation code
    public enum State implements StandAnimationState<GoldExperienceEntity> {
        IDLE(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.ge.idle", AzPlayBehaviors.LOOP)),
        LIGHT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.ge.light", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        BLOCK(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.ge.block", AzPlayBehaviors.LOOP)),
        HEAVY(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.ge.heavy", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        UNDERHAND(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.ge.underhand", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        BARRAGE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.ge.barrage", AzPlayBehaviors.LOOP)),
        HEAL_SELF(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.ge.healself", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        HEAL(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.ge.heal", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        TREE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.ge.tree", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        LIFE_GIVER(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.ge.lifegiver", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        REKKA1(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.ge.rekka1", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        REKKA2(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.ge.rekka2", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        REKKA3(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.ge.rekka3", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        SOUL_PUNCH(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.ge.soul_punch", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        OVERCLOCK(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.ge.overclock", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        LIGHT_FOLLOWUP(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.ge.light_followup", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        ITEM_TOSS_CHARGE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "itemthrow_charge", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        ITEM_TOSS(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "itemthrow", AzPlayBehaviors.PLAY_ONCE));

        private final AzCommand animator;

        State(AzCommand animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(GoldExperienceEntity attacker) {
            animator.sendForEntity(attacker);
        }
    }

    @Override
    protected State[] getStateValues() {
        return State.values();
    }

    @Override
    public State getBlockState() {
        return State.BLOCK;
    }
}
