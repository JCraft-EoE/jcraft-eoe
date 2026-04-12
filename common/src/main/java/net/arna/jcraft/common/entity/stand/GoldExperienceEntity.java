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
import net.arna.jcraft.common.attack.moves.goldexperience.BerryBushAttack;
import net.arna.jcraft.common.attack.moves.goldexperience.LifeGiverAttack;
import net.arna.jcraft.common.attack.moves.goldexperience.OverclockAttack;
import net.arna.jcraft.common.attack.moves.goldexperience.TreeAttack;
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
            GoldExperienceEntity::registerMoves, State.class);
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
    public static final BerryBushAttack BERRY_BUSH = new BerryBushAttack(40,
            16, 20, 1.25f, 4f, 5, 1.5f, 0.75f, 0.2f)
            .withAnim(State.LIFE_GIVER)
            .withImpactSound(JSoundRegistry.IMPACT_4)
            .withInfo(
                    Component.literal("Place Berry Bush"),
                    Component.literal("places an almost-ripe berry bush on the ground, this move cannot be aimed up or down")
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
                    Component.literal("quick combo finisher")
            );
    public static final SimpleAttack<GoldExperienceEntity> LIGHT = new SimpleAttack<GoldExperienceEntity>(15,
            6, 9, 0.75f, 5f, 7, 1.5f, 0.2f, -0.1f)
            .noLoopPrevention()
            .withFollowup(LIGHT_FOLLOWUP)
            .withCrouchingVariant(BERRY_BUSH)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withInfo(
                    Component.literal("Punch"),
                    Component.literal("quick combo starter, low stun")
            );
    public static final MovementSlowingSimpleAttack<GoldExperienceEntity> HEAVY = new MovementSlowingSimpleAttack<GoldExperienceEntity>(22,
            13, 22, 1f, 9f, 10, 1.5f, 1.5f, 0f)
            .withExtraHitBox(new HitBoxData(0, 0, 1.25))
//            .withSound(JSoundRegistry.GE_HEAVY)
            .withImpactSound(JSoundRegistry.IMPACT_2)
            .withHitSpark(JParticleType.HIT_SPARK_3)
            .withHyperArmor()
            .withLaunch()
            .withInfo(
                    Component.literal("Shoulder Smash"),
                    Component.literal("slow, uninterruptible combo finisher")
            );
    public static final MainBarrageAttack<GoldExperienceEntity> BARRAGE = new MainBarrageAttack<GoldExperienceEntity>(
            280, 0, 30, 0.75f, 1f, 20, 2f, 0.25f, 0f, 3, Blocks.OAK_PLANKS.defaultDestroyTime())
            .withSound(JSoundRegistry.GE_BARRAGE)
            .withInfo(
                    Component.literal("Barrage"),
                    Component.literal("fast reliable combo starter/extender, high stun")
            );
    public static final HealMove<GoldExperienceEntity> HEAL_OTHERS = new HealMove<GoldExperienceEntity>(520, 10,
            16, 1f, 1.25f, 0f, 4f, HealMove.HealTarget.TARGETS, false)
            .withSound(JSoundRegistry.GE_HEAL)
            .withInfo(
                    Component.literal("Healing Hand (others)"),
                    Component.empty()
            );
    public static final HealMove<GoldExperienceEntity> HEAL_SELF = new HealMove<GoldExperienceEntity>(520, 10,
            14, 1f, 0, 0, 4f, HealMove.HealTarget.USER, false)
            .withCrouchingVariant(HEAL_OTHERS)
            .withSound(JSoundRegistry.GE_HEAL)
            .withInfo(
                    Component.literal("Healing Hand"),
                    Component.literal("standing: heals user for 2 hearts, crouching: heals others for 2 hearts, pacifies angered mobs")
            );
    public static final TreeAttack TREE = new TreeAttack(280, 10, 24, 1f, 5f,
            15, 1.75f, 0.2f, -0.1f)
            .withImpactSound(JSoundRegistry.IMPACT_8)
            .withSound(JSoundRegistry.GE_TREE)
            .withInfo(
                    Component.literal("Tree Summon"),
                    Component.literal("two-hitting launch")
            );
    public static final LifeGiverAttack LIFE_GIVER = new LifeGiverAttack(300, 16, 25, 1f)
            .withSound(JSoundRegistry.GE_HEAL)
            .withInfo(
                    Component.literal("Life Giver"),
                    Component.literal("""
                            STANDING: turns any stackable item into a snake, lasts for 25s and stuns for 0.5s on hit
                            CROUCHING: turns any stackable item into a frog, lasts for 15s and reflects damage, follows user
                            AERIAL: turns any item into a butterfly, lasts forever""")
            );
    public static final OverclockAttack OVERCLOCK = new OverclockAttack(920, 22, 31, 1f,
            9f, 60, 2f, 0.9f, 0f)
//            .withSound(JSoundRegistry.GE_ULT)
            .withImpactSound(JSoundRegistry.IMPACT_10)
            .withBlockableType(BlockableType.NON_BLOCKABLE)
            .withInfo(
                    Component.literal("Overclock"),
                    Component.literal("slow, unblockable, devastating stun")
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
                    Component.literal("knockdown, low blockstun")
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
                    Component.literal("links into Light")
            );
    public static final SimpleAttack<GoldExperienceEntity> REKKA1 = new SimpleAttack<GoldExperienceEntity>
            (0, 7, 14, 1f, 5f, 15, 1.5f, 0.5f, 0f)
            .withAnim(State.REKKA1)
            .withSound(JSoundRegistry.GE_REKKA1)
            .withImpactSound(JSoundRegistry.IMPACT_2)
            .withFollowup(REKKA2)
            .withExtraHitBox(1.25)
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.CRUSH)
            .withInfo(
                    Component.literal("Rekka Series"),
                    Component.literal("a set of three attacks, which cancel into each other during recovery")
            );

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

        moves.register(MoveClass.HEAVY, HEAVY, State.HEAVY);
        moves.register(MoveClass.BARRAGE, BARRAGE, State.BARRAGE);

        moves.register(MoveClass.SPECIAL1, HEAL_SELF, State.HEAL_SELF).withCrouchingVariant(State.HEAL);
        moves.register(MoveClass.SPECIAL2, REKKA1, State.REKKA1).withFollowup(State.REKKA2).withFollowup(State.REKKA3);
        moves.register(MoveClass.SPECIAL3, LIFE_GIVER, State.LIFE_GIVER);
        moves.register(MoveClass.ULTIMATE, OVERCLOCK, State.OVERCLOCK);

        moves.register(MoveClass.UTILITY, TREE, State.TREE);
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
        BARRAGE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.ge.barrage", AzPlayBehaviors.LOOP)),
        HEAL_SELF(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.ge.healself", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        HEAL(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.ge.heal", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        TREE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.ge.tree", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        LIFE_GIVER(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.ge.lifegiver", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        REKKA1(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.ge.rekka1", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        REKKA2(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.ge.rekka2", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        REKKA3(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.ge.rekka3", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        OVERCLOCK(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.ge.overclock", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        LIGHT_FOLLOWUP(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.ge.light_followup", AzPlayBehaviors.HOLD_ON_LAST_FRAME));

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
