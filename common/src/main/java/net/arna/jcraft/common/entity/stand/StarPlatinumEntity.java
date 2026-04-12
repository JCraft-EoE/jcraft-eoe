package net.arna.jcraft.common.entity.stand;

import lombok.NonNull;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.Attacks;
import net.arna.jcraft.api.attack.MoveMap;
import net.arna.jcraft.api.attack.MoveSet;
import net.arna.jcraft.api.attack.MoveSetManager;
import net.arna.jcraft.api.attack.StateContainer;
import net.arna.jcraft.api.attack.enums.BlockableType;
import net.arna.jcraft.api.attack.enums.MoveClass;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.api.pose.ModifierCondition;
import net.arna.jcraft.api.pose.PoseModifiers;
import net.arna.jcraft.api.pose.modifier.IPoseModifier;
import net.arna.jcraft.api.pose.modifier.PoseModifierGroup;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.api.stand.StandData;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.api.stand.StandInfo;
import net.arna.jcraft.api.stand.SummonData;
import net.arna.jcraft.common.attack.moves.shared.*;
import net.arna.jcraft.common.attack.moves.starplatinum.InhaleAttack;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.StandAnimationState;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.joml.Vector3f;

import java.util.function.Supplier;

/**
 * The {@link StandEntity} for <a href="https://jojowiki.com/Star_Platinum">Star Platinum</a>.
 * @see JStandTypeRegistry#STAR_PLATINUM
 * @see net.arna.jcraft.client.renderer.entity.stands.StarPlatinumRenderer StarPlatinumRenderer
 * @see net.arna.jcraft.common.attack.moves.starplatinum.BlockBreakingAttack BlockBreakingAttack
 * @see InhaleAttack
 */
public final class StarPlatinumEntity extends AbstractStarPlatinumEntity<StarPlatinumEntity, StarPlatinumEntity.State> {
    public static final MoveSet<StarPlatinumEntity, State> MOVE_SET = MoveSetManager.create(JStandTypeRegistry.STAR_PLATINUM,
            StarPlatinumEntity::registerMoves, State.class);
    public static final StandData DATA = StandData.builder()
            .idleRotation(225f)
            .info(StandInfo.builder()
                    .name(Component.translatable("entity.jcraft.starplatinum"))
                    .proCount(3)
                    .conCount(3)
                    .freeSpace(Component.literal("""
                        BNBs:
                        ~ represents a queued attack
                        
                            -the classic
                            Punch>Barrage>Punch>Knee>Advancing Barrage~Star Finger~Star Breaker
                        
                            -the rushdown
                            Punch~Punch>dash Barrage>cr.Punch>Star Finger>Knee>Punch>Advancing Barrage>Punch~Punch
                        
                            -the blowback
                            Inhale>...>Star Finger>Star Breaker>Barrage>...
                        
                            -the poke
                            Star Finger>Knee>Punch>Advancing Barrage~Punch>Barrage>Punch>Star Breaker"""))
                    .skinName(Component.literal("Manga"))
                    .skinName(Component.literal("Arcade"))
                    .skinName(Component.literal("OVA"))
                    .build())
            .summonData(SummonData.of(JSoundRegistry.STAR_PLATINUM_SUMMON))
            .build();
    public static final Supplier<IPoseModifier> POSE = () -> PoseModifierGroup.builder()
            .modifier(PoseModifiers.parse("""
                    leftArm.xRot = 0;
                    leftArm.yRot = -15deg;
                    leftArm.zRot = 5deg;
                    """, ModifierCondition.LEFT_ARM_EMPTY))
            .modifier(PoseModifiers.parse("""
                    rightArm.zRot = 15deg;
                    rightArm.xRot *= 0.5;
                    """, ModifierCondition.RIGHT_ARM_EMPTY))
            .build();

    public static final SimpleUppercutAttack<StarPlatinumEntity> UPPERCUT = new SimpleUppercutAttack<StarPlatinumEntity>(JCraft.LIGHT_COOLDOWN,
            8, 14, 0.75f, 6f, 20, 1.5f, 0.25f, -0.6f, 0.75f)
            .withAnim(State.UPPERCUT)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withExtraHitBox(0, 0.35, 1.25)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Component.translatable("jcraft.starplatinum.crm1"),
                    Component.literal("Slower combo starter, launches vertically, good anti-air.")
            );
    public static final SimpleAttack<StarPlatinumEntity> LIGHT_FOLLOWUP = new SimpleAttack<StarPlatinumEntity>(0,
            6, 10, 0.75f, 6f, 8, 1.5f, 1f, -0.25f)
            .withAnim(State.LIGHT_FOLLOWUP)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withLaunch()
            .withBlockStun(4)
            .withExtraHitBox(0, 0, 1)
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Component.translatable("jcraft.starplatinum.m1m1"),
                    Component.literal("Quick combo finisher.")
            );
    public static final SimpleAttack<StarPlatinumEntity> LIGHT = SimpleAttack.<StarPlatinumEntity>lightAttack(
                    5, 7, 0.75f, 5f, 10, 0.2f, -0.1f)
            .withFollowup(LIGHT_FOLLOWUP)
            .withCrouchingVariant(UPPERCUT)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withInfo(
                    Component.translatable("jcraft.starplatinum.m1"),
                    Component.literal("Quick combo starter.")
            );
    public static final MainBarrageAttack<StarPlatinumEntity> BARRAGE = new MainBarrageAttack<StarPlatinumEntity>(280,
            0, 40, 0.75f, 1f, 30, 2f, 0.25f, 0f, 3, Blocks.OBSIDIAN.defaultDestroyTime())
            .withSound(JSoundRegistry.STAR_PLATINUM_BARRAGE)
            .withInfo(
                    Component.translatable("jcraft.generic.barrage"),
                    Component.literal("Fast, reliable combo starter/extender, high stun.")
            );
    public static final KnockdownAttack<StarPlatinumEntity> GRAB_HIT = new KnockdownAttack<StarPlatinumEntity>(0,
            10, 20, 1f, 6f, 15, 1.75f, 0.4f, 0f, 35)
            .withSound(JSoundRegistry.SPTW_UPPERCUT)
            .withImpactSound(JSoundRegistry.IMPACT_6)
            .withHyperArmor()
            .withHitSpark(JParticleType.HIT_SPARK_2)
            .withInfo(
                    Component.translatable("jcraft.starplatinum.crsp1hit"),
                    Component.empty()
            );
    public static final GrabAttack<StarPlatinumEntity, State> GRAB = new GrabAttack<>(0,
            8, 20,1f, 2f, 20, 1.5f, 0.1f, 0f, GRAB_HIT,
            StateContainer.of(State.GRAB_HIT), 11, 0.8)
            .withSound(JSoundRegistry.SPTW_GRAB)
            .withImpactSound(JSoundRegistry.SPTW_GRABHIT)
            .withHitAnimation(null)
            .withBlockableType(BlockableType.BLOCKABLE)
            .withInfo(
                    Component.translatable("jcraft.starplatinum.crsp1"),
                    Component.literal("Blockable grab, knocks down.")
            );
    public static final SimpleAttack<StarPlatinumEntity> STAR_FINGER = new SimpleAttack<StarPlatinumEntity>(0,
            12, 20, 0.75f, 5f, 30, 1.75f, -0.4f, -0.25f)
            .withCrouchingVariant(GRAB)
            .withSound(JSoundRegistry.STAR_FINGER)
            .withBlockStun(5)
            .withExtraHitBox(2, 0.1, 1)
            .withInfo(
                    Component.translatable("jcraft.starplatinum.sp1"),
                    Component.literal("Medium windup combo starter/extender, vacuums on hit, unsafe on block.")
            );
    public static final SimpleUppercutAttack<StarPlatinumEntity> KNEE_UP = new SimpleUppercutAttack<StarPlatinumEntity>(0,
            8, 14, 0.75f, 4f, 13, 1.6f, 0.2f, -0.4f, 0.5f)
            .withSound(JSoundRegistry.STAR_PLATINUM_KNEE)
            .withImpactSound(JSoundRegistry.IMPACT_6)
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.HIGH)
            .withInfo(
                    Component.translatable("jcraft.starplatinum.airsp2"),
                    Component.literal("Launches upward, larger and higher hitbox, higher stun, less damage.")
            );
    public static final SimpleAttack<StarPlatinumEntity> KNEE = new SimpleAttack<StarPlatinumEntity>(0,
            7, 12, 0.9f, 6f, 9, 1.5f, 0.3f, 0f)
            .withAerialVariant(KNEE_UP)
            .withSound(JSoundRegistry.STAR_PLATINUM_KNEE)
            .withImpactSound(JSoundRegistry.IMPACT_6)
            .withHitAnimation(CommonHitPropertyComponent.HitAnimation.CRUSH)
            .withInfo(
                    Component.translatable("jcraft.starplatinum.sp2"),
                    Component.literal("Fast poke, low stun.")
            );
    public static final ChargeBarrageAttack<StarPlatinumEntity> SHORT_CHARGE_BARRAGE = new ChargeBarrageAttack<StarPlatinumEntity>(140, 5, 25,
            6f, 0.6f, 15, 1.5f, 0.1f, 0f, 3, true)
            .withSound(JSoundRegistry.STAR_PLATINUM_LUNGING_BARRAGE)
            .withShockwaves()
            .withBackstab(false)
            .withInfo(
                    Component.translatable("jcraft.starplatinum.crsp3"),
                    Component.literal("Lasts shorter. Less punishable on whiff.")
            );
    public static final ChargeBarrageAttack<StarPlatinumEntity> CHARGE_BARRAGE = new ChargeBarrageAttack<StarPlatinumEntity>(140, 5, 55,
            7f, 0.6f, 15, 1.5f, 0.1f, 0f, 3, false)
            .withSound(JSoundRegistry.STAR_PLATINUM_ADVANCING_BARRAGE)
            .withShockwaves()
            .withBackstab(false)
            .withCrouchingVariant(SHORT_CHARGE_BARRAGE)
            .withInfo(
                    Component.translatable("jcraft.starplatinum.sp3"),
                    Component.literal("Fast combo starter/extender, medium stun, extremely punishable on whiff.")
            );
    // TODO add move info x2
    // TODO balance x2
    public static final StandbyActivationMove<StarPlatinumEntity> STANDBY_ON = new StandbyActivationMove<>(0, 1, 1, 0.75f)
            ;
    public static final StandbyDeactivationMove<StarPlatinumEntity> STANDBY_OFF = new StandbyDeactivationMove<>(0, 1, 1, -0.75f)
            ;
    public static final JumpMove<StarPlatinumEntity> JUMP = new JumpMove<StarPlatinumEntity>(300, 5,
            14, 1f, 1.5f)
            //.withCrouchingVariant(STANDBY_ON)
            .withInfo(
                    Component.translatable("jcraft.starplatinum.util"),
                    Component.literal("Jumps in looked direction with slight upward bias, you must stay on the ground until Star Platinum jumps.")
            );
    // TODO add move info x2
    // TODO balance x2
    public static final TossMove<StarPlatinumEntity> TOSS = new TossMove<StarPlatinumEntity>(0, 1, 1, 0.75f)
            .withAnim(State.ITEM_TOSS);
    public static final TossChargeMove<StarPlatinumEntity> TOSS_CHARGE = new TossChargeMove<StarPlatinumEntity>(70, 3 * 20 + 1, 3 * 20, 1.0f, 10)
            .withFollowup(TOSS);
    public static final InhaleAttack INHALE = new InhaleAttack(800, 5, 5, 1f, 80)
            .withInfo(
                    Component.translatable("jcraft.starplatinum.ult"),
                    Component.literal("Vacuums looked entities for 4 seconds.")
            );
    private static final EntityDataAccessor<Integer> INHALE_TIME = SynchedEntityData.defineId(StarPlatinumEntity.class, EntityDataSerializers.INT);

    public StarPlatinumEntity(Level worldIn) {
        super(JStandTypeRegistry.STAR_PLATINUM.get(), worldIn);
        auraColors = new Vector3f[]{
                new Vector3f(0.8f, 0.5f, 1.0f),
                new Vector3f(0.6f, 0.2f, 1.0f),
                new Vector3f(0.2f, 0.8f, 0.6f),
                new Vector3f(0.1f, 0.3f, 1.0f)
        };
    }

    private static void registerMoves(MoveMap<StarPlatinumEntity, State> moves) {
        moves.registerImmediate(MoveClass.LIGHT, LIGHT, State.PUNCH);

        moves.register(MoveClass.HEAVY, STAR_BREAKER, State.HEAVY).withCrouchingVariant(State.GROUND_BREAKER);
        moves.register(MoveClass.BARRAGE, BARRAGE, State.BARRAGE);

        moves.register(MoveClass.SPECIAL1, STAR_FINGER, State.STAR_FINGER).withCrouchingVariant(State.GRAB);
        moves.register(MoveClass.SPECIAL2, KNEE, State.KNEE).withAerialVariant(State.KNEE_UP);
        moves.register(MoveClass.SPECIAL3, CHARGE_BARRAGE, State.BARRAGE).withCrouchingVariant(State.BARRAGE);
        moves.register(MoveClass.ULTIMATE, INHALE, State.INHALE);

        moves.register(MoveClass.UTILITY, JUMP, State.JUMP);//.withCrouchingVariant(State.IDLE);

        moves.register(MoveClass.STANDBY_OFF, STANDBY_OFF, State.IDLE);
        moves.register(MoveClass.TOSS, TOSS_CHARGE, State.ITEM_TOSS_CHARGE).withFollowup(State.ITEM_TOSS);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(INHALE_TIME, 0);
    }

    public void setInhaleTime(int time) {
        entityData.set(INHALE_TIME, time);
    }

    public int getInhaleTime() {
        return entityData.get(INHALE_TIME);
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) {
            INHALE.tick(this);
        }
    }

    @Override
    public boolean initMove(MoveClass moveClass) {
        if (tryFollowUp(moveClass, MoveClass.LIGHT)) return true;
        return super.initMove(moveClass);
    }

    @Override
    @NonNull
    public StarPlatinumEntity getThis() {
        return this;
    }

    // Animation code
    public enum State implements StandAnimationState<StarPlatinumEntity> {
        IDLE(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.star_platinum.idle", AzPlayBehaviors.LOOP)),
        INHALE_IDLE(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.star_platinum.inhaleidle", AzPlayBehaviors.LOOP)),
        PUNCH(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.star_platinum.light", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        BLOCK(AzCommand.create(JCraft.BASE_CONTROLLER, "animation.star_platinum.block", AzPlayBehaviors.LOOP)),
        HEAVY(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.star_platinum.heavy", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        GROUND_BREAKER(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.star_platinum.ground_slam", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        BARRAGE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.star_platinum.barrage", AzPlayBehaviors.LOOP)),
        STAR_FINGER(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.star_platinum.star_finger", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        INHALE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.star_platinum.inhale", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        KNEE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.star_platinum.knee", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        KNEE_UP(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.star_platinum.knee_up", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        JUMP(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.star_platinum.jump", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        GRAB(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.star_platinum.grab", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        GRAB_HIT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.star_platinum.grabhit", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        UPPERCUT(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.star_platinum.uppercut", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        LIGHT_FOLLOWUP(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.star_platinum.light_followup", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        ITEM_TOSS_CHARGE(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.star_platinum.itemthrow_charge", AzPlayBehaviors.HOLD_ON_LAST_FRAME)),
        ITEM_TOSS(Attacks.createAnimationCommand(JCraft.BASE_CONTROLLER, "animation.star_platinum.itemthrow", AzPlayBehaviors.PLAY_ONCE));

        private final AzCommand animator;

        State(AzCommand animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(StarPlatinumEntity attacker) {
            if (this == IDLE && attacker.getInhaleTime() > 0) {
                INHALE_IDLE.animator.sendForEntity(attacker);
                return;
            }

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
